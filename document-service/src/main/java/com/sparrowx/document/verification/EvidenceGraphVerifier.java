package com.sparrowx.document.verification;

import com.sparrowx.document.domain.models.DocumentEvidenceEdge;
import com.sparrowx.document.domain.models.DocumentEvidenceGraph;
import com.sparrowx.document.domain.models.DocumentEvidenceNode;
import com.sparrowx.document.domain.models.RetrievalEvidence;
import com.sparrowx.document.domain.models.SourceSpan;
import com.sparrowx.document.domain.valueobjects.ClaimText;
import com.sparrowx.document.domain.valueobjects.VerificationStatus;
import com.sparrowx.document.exceptions.CitationVerificationException;
import com.sparrowx.document.exceptions.InvalidDocumentException;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class EvidenceGraphVerifier {

    private final CitationVerifier citationVerifier;

    public EvidenceGraphVerifier(CitationVerifier citationVerifier) {
        this.citationVerifier = citationVerifier;
    }

    public EvidenceGraphVerificationResult verify(
            DocumentEvidenceGraph graph,
            boolean requireAllNodesSupported,
            boolean requireAllEdgesSupported
    ) {
        validate(graph);

        Map<String, SourceSpan> sourcePool = indexSourcePool(graph.sourcePool());

        List<DocumentEvidenceNode> verifiedNodes = new ArrayList<>();
        List<String> unsupportedNodeIds = new ArrayList<>();
        List<String> unsupportedEdgeIds = new ArrayList<>();
        List<String> warnings = new ArrayList<>(graph.warnings());

        Map<String, VerificationStatus> nodeStatuses = new HashMap<>();
        Set<String> unsupportedNodeIdSet = new HashSet<>();

        double confidenceSum = 0.0;
        int confidenceCount = 0;
        int supportedNodeCount = 0;

        for (DocumentEvidenceNode node : graph.nodes()) {
            if (node == null) {
                warnings.add("Null evidence node encountered.");
                unsupportedNodeIds.add("unknown-node");
                unsupportedNodeIdSet.add("unknown-node");
                confidenceCount++;
                continue;
            }

            NodeVerification verification =
                    verifyNode(node, sourcePool);

            DocumentEvidenceNode verifiedNode = copyNodeWithVerification(
                    node,
                    verification.status(),
                    verification.confidence(),
                    verification.coverageScore(),
                    verification.warnings()
            );

            verifiedNodes.add(verifiedNode);
            warnings.addAll(verification.warnings());

            String nodeId = safeId(node.nodeId(), "unknown-node");
            nodeStatuses.put(nodeId, verification.status());

            confidenceSum += verification.confidence();
            confidenceCount++;

            if (isSupportedStatus(verification.status())) {
                supportedNodeCount++;
            } else {
                unsupportedNodeIds.add(nodeId);
                unsupportedNodeIdSet.add(nodeId);
            }
        }

        for (DocumentEvidenceEdge edge : graph.edges()) {
            if (edge == null) {
                warnings.add("Null evidence edge encountered.");
                unsupportedEdgeIds.add("unknown-edge");
                continue;
            }

            EdgeVerification edgeVerification = verifyEdge(
                    edge,
                    sourcePool,
                    nodeStatuses,
                    unsupportedNodeIdSet
            );

            warnings.addAll(edgeVerification.warnings());

            if (!edgeVerification.supported()) {
                unsupportedEdgeIds.add(safeId(edge.edgeId(), "unknown-edge"));
            }
        }

        double confidence = confidenceCount == 0
                ? 0.0
                : confidenceSum / confidenceCount;

        double coverageScore = graph.nodes().isEmpty()
                ? 0.0
                : (double) supportedNodeCount / graph.nodes().size();

        VerificationStatus graphStatus = graphStatus(
                graph.nodes().size(),
                supportedNodeCount,
                unsupportedNodeIds,
                unsupportedEdgeIds
        );

        boolean supported =
                isSupportedStatus(graphStatus)
                        && (!requireAllNodesSupported
                        || unsupportedNodeIds.isEmpty())
                        && (!requireAllEdgesSupported
                        || unsupportedEdgeIds.isEmpty());

        DocumentEvidenceGraph verifiedGraph = new DocumentEvidenceGraph(
                graph.graphId(),
                graph.goal(),
                graph.customGoal(),
                verifiedNodes,
                graph.edges(),
                graph.sourcePool(),
                graphStatus,
                confidence,
                coverageScore,
                warnings,
                graph.missingNodeTypes(),
                graph.outputSchemaRef(),
                graph.outputSchemaVersion(),
                graph.createdAt()
        );

        return new EvidenceGraphVerificationResult(
                supported,
                graphStatus,
                confidence,
                coverageScore,
                verifiedGraph,
                unsupportedNodeIds,
                unsupportedEdgeIds,
                warnings,
                explanation(supported, graphStatus, unsupportedNodeIds, unsupportedEdgeIds)
        );
    }

    private NodeVerification verifyNode(
            DocumentEvidenceNode node,
            Map<String, SourceSpan> sourcePool
    ) {
        if (node.sourceSpanIds() == null || node.sourceSpanIds().isEmpty()) {
            return new NodeVerification(
                    VerificationStatus.NEEDS_SOURCE_CONTEXT,
                    0.0,
                    0.0,
                    List.of("Node %s has no source span references."
                            .formatted(safeId(node.nodeId(), "unknown-node")))
            );
        }

        List<RetrievalEvidence> evidence = node.sourceSpanIds()
                .stream()
                .map(sourcePool::get)
                .filter(span -> span != null)
                .map(this::toRetrievalEvidence)
                .filter(item -> item.text() != null && !item.text().isBlank())
                .toList();

        if (evidence.isEmpty()) {
            return new NodeVerification(
                    VerificationStatus.NEEDS_SOURCE_CONTEXT,
                    0.0,
                    0.0,
                    List.of("Node %s references source spans missing from source_pool or without excerpts."
                            .formatted(safeId(node.nodeId(), "unknown-node")))
            );
        }

        String claimText = firstNonBlank(
                node.normalizedText(),
                node.summary(),
                node.title()
        );

        if (claimText.isBlank()) {
            return new NodeVerification(
                    VerificationStatus.UNSUPPORTED,
                    0.0,
                    0.0,
                    List.of("Node %s has no verifiable text."
                            .formatted(safeId(node.nodeId(), "unknown-node")))
            );
        }

        CitationVerifier.CitationVerificationResult result =
                citationVerifier.verify(
                        ClaimText.of(claimText),
                        evidence
                );

        VerificationStatus status = result.status();

        double coverageScore = switch (status) {
            case SUPPORTED -> 1.0;
            case PARTIALLY_SUPPORTED -> Math.max(0.1, Math.min(0.99, result.confidence()));
            case CONTRADICTED -> 0.0;
            case UNSUPPORTED, NEEDS_SOURCE_CONTEXT, UNVERIFIED, UNSPECIFIED -> 0.0;
        };

        return new NodeVerification(
                status,
                result.confidence(),
                coverageScore,
                List.of(result.explanation())
        );
    }

    private EdgeVerification verifyEdge(
            DocumentEvidenceEdge edge,
            Map<String, SourceSpan> sourcePool,
            Map<String, VerificationStatus> nodeStatuses,
            Set<String> unsupportedNodeIds
    ) {
        List<String> warnings = new ArrayList<>();

        String edgeId = safeId(edge.edgeId(), "unknown-edge");

        boolean endpointsPresent =
                edge.fromNodeId() != null && !edge.fromNodeId().isBlank()
                        && edge.toNodeId() != null && !edge.toNodeId().isBlank();

        VerificationStatus fromStatus = nodeStatuses.get(edge.fromNodeId());
        VerificationStatus toStatus = nodeStatuses.get(edge.toNodeId());

        boolean endpointsKnown =
                fromStatus != null && toStatus != null;

        boolean endpointsSupported =
                endpointsKnown
                        && isSupportedStatus(fromStatus)
                        && isSupportedStatus(toStatus)
                        && !unsupportedNodeIds.contains(
                        edge.fromNodeId()
                )
                        && !unsupportedNodeIds.contains(
                        edge.toNodeId()
                );

        boolean hasSourceSupport =
                edge.sourceSpanIds() != null
                        && edge.sourceSpanIds()
                        .stream()
                        .anyMatch(sourcePool::containsKey);

        if (!endpointsPresent) {
            warnings.add("Edge %s has missing endpoint ids.".formatted(edgeId));
        }

        if (!endpointsKnown) {
            warnings.add("Edge %s references unknown node id(s).".formatted(edgeId));
        }

        if (!endpointsSupported) {
            warnings.add("Edge %s references unsupported or incompatible node(s).".formatted(edgeId));
        }

        if (!hasSourceSupport) {
            warnings.add("Edge %s has no source support in source_pool.".formatted(edgeId));
        }

        return new EdgeVerification(
                endpointsPresent && endpointsKnown && endpointsSupported && hasSourceSupport,
                warnings
        );
    }

    private RetrievalEvidence toRetrievalEvidence(SourceSpan span) {
        return new RetrievalEvidence(
                span.sourceSpanId(),
                span.documentId(),
                span.chunkId(),
                span.title(),
                span.fileName(),
                span.excerpt(),
                span.pageStart(),
                span.pageEnd(),
                span.relevanceScore(),
                span.citation()
        );
    }

    private Map<String, SourceSpan> indexSourcePool(List<SourceSpan> sourcePool) {
        Map<String, SourceSpan> indexed = new HashMap<>();

        if (sourcePool == null) {
            return indexed;
        }

        for (SourceSpan span : sourcePool) {
            if (span == null || span.sourceSpanId() == null || span.sourceSpanId().isBlank()) {
                continue;
            }

            indexed.put(span.sourceSpanId(), span);
        }

        return indexed;
    }

    private DocumentEvidenceNode copyNodeWithVerification(
            DocumentEvidenceNode node,
            VerificationStatus status,
            double confidence,
            double coverageScore,
            List<String> verificationWarnings
    ) {
        List<String> warnings = new ArrayList<>(node.warnings());
        warnings.addAll(verificationWarnings);

        return new DocumentEvidenceNode(
                node.nodeId(),
                node.nodeType(),
                node.customNodeType(),
                node.title(),
                node.summary(),
                node.normalizedText(),
                node.sourceSpanIds(),
                status,
                confidence,
                coverageScore,
                status == VerificationStatus.NEEDS_SOURCE_CONTEXT || node.requiresSourceContext(),
                node.tags(),
                warnings,
                node.attributes()
        );
    }

    private VerificationStatus graphStatus(
            int totalNodes,
            int supportedNodeCount,
            List<String> unsupportedNodeIds,
            List<String> unsupportedEdgeIds
    ) {
        if (totalNodes == 0) {
            return VerificationStatus.UNSUPPORTED;
        }

        if (unsupportedNodeIds.isEmpty()
                && unsupportedEdgeIds.isEmpty()) {
            return VerificationStatus.SUPPORTED;
        }

        if (supportedNodeCount > 0) {
            return VerificationStatus.PARTIALLY_SUPPORTED;
        }

        return VerificationStatus.UNSUPPORTED;
    }

    private boolean isSupportedStatus(VerificationStatus status) {
        return status == VerificationStatus.SUPPORTED
                || status == VerificationStatus.PARTIALLY_SUPPORTED;
    }

    private String explanation(
            boolean supported,
            VerificationStatus status,
            List<String> unsupportedNodeIds,
            List<String> unsupportedEdgeIds
    ) {

        if (supported) {
            return "Evidence graph is supported by the supplied source pool.";
        }

        return "Evidence graph verification completed with status=%s unsupportedNodes=%s unsupportedEdges=%s"
                .formatted(status, unsupportedNodeIds, unsupportedEdgeIds);
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return "";
        }

        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }

        return "";
    }

    private String safeId(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }

        return value;
    }

    private void validate(DocumentEvidenceGraph graph) {
        if (graph == null) {
            throw InvalidDocumentException.blankField("graph");
        }

        if (graph.nodes() == null || graph.nodes().isEmpty()) {
            throw new CitationVerificationException("graph nodes must not be empty");
        }
    }

    public record EvidenceGraphVerificationResult(
            boolean supported,
            VerificationStatus verificationStatus,
            double confidence,
            double coverageScore,
            DocumentEvidenceGraph verifiedGraph,
            List<String> unsupportedNodeIds,
            List<String> unsupportedEdgeIds,
            List<String> warnings,
            String explanation
    ) {
        public EvidenceGraphVerificationResult {
            unsupportedNodeIds = unsupportedNodeIds == null ? List.of() : List.copyOf(unsupportedNodeIds);
            unsupportedEdgeIds = unsupportedEdgeIds == null ? List.of() : List.copyOf(unsupportedEdgeIds);
            warnings = warnings == null ? List.of() : List.copyOf(warnings);
            explanation = explanation == null ? "" : explanation;
        }
    }

    private record NodeVerification(
            VerificationStatus status,
            double confidence,
            double coverageScore,
            List<String> warnings
    ) {
    }

    private record EdgeVerification(
            boolean supported,
            List<String> warnings
    ) {
    }
}