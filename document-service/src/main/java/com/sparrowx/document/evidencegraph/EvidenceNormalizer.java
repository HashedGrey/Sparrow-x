package com.sparrowx.document.evidencegraph;

import com.sparrowx.document.domain.models.DocumentEvidenceGraph;
import com.sparrowx.document.domain.models.DocumentEvidenceNode;
import com.sparrowx.document.domain.models.SourceSpan;
import com.sparrowx.document.domain.valueobjects.VerificationStatus;
import com.sparrowx.document.features.builddocumentevidence.BuildDocumentEvidenceCommand;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class EvidenceNormalizer {

    private static final int DEFAULT_MAX_NODES = 8;

    private final EvidenceProjectionPort projectionPort;

    public EvidenceNormalizer(EvidenceProjectionPort projectionPort) {
        this.projectionPort = projectionPort;
    }

    public NormalizationResult normalize(
            BuildDocumentEvidenceCommand command,
            List<SourceSpan> sourceSpans
    ) {
        List<String> warnings = new ArrayList<>();

        if (sourceSpans == null || sourceSpans.isEmpty()) {
            return new NormalizationResult(
                    List.of(),
                    List.of("Evidence normalization skipped because sourceSpans is empty.")
            );
        }

        EvidenceProjectionPort.ProjectionResult projectionResult =
                projectionPort.project(command, sourceSpans);

        warnings.addAll(projectionResult.warnings());

        List<DocumentEvidenceNode> nodes = new ArrayList<>();

        if (projectionResult.nodes() != null && !projectionResult.nodes().isEmpty()) {
            nodes.addAll(projectionResult.nodes());
        }

        List<DocumentEvidenceNode.EvidenceNodeType> missingTypes =
                missingRequestedTypes(command, nodes);

        if (!missingTypes.isEmpty()) {
            List<DocumentEvidenceNode> deterministicNodes =
                    deterministicNodesForMissingTypes(command, sourceSpans, missingTypes, remainingNodeBudget(command, nodes));

            if (!deterministicNodes.isEmpty()) {
                nodes.addAll(deterministicNodes);
                warnings.add("Added deterministic fallback nodes for missing requested node types: " + missingTypes);
            }
        }

        if (nodes.isEmpty()) {
            nodes.addAll(sourceSpans.stream()
                    .limit(normalizeLimit(command))
                    .map(span -> toNode(command, span, defaultNodeType(command)))
                    .toList());

            warnings.add("Used fallback span-to-node normalization.");
        }

        nodes = deduplicateNodes(nodes);
        nodes = nodes.stream()
                .limit(normalizeLimit(command))
                .toList();

        return new NormalizationResult(nodes, warnings);
    }

    private List<DocumentEvidenceNode> deterministicNodesForMissingTypes(
            BuildDocumentEvidenceCommand command,
            List<SourceSpan> sourceSpans,
            List<DocumentEvidenceNode.EvidenceNodeType> missingTypes,
            int budget
    ) {
        if (budget <= 0 || missingTypes.isEmpty()) {
            return List.of();
        }

        List<DocumentEvidenceNode> nodes = new ArrayList<>();

        for (DocumentEvidenceNode.EvidenceNodeType type : missingTypes) {
            if (nodes.size() >= budget) {
                break;
            }

            SourceSpan sourceSpan = bestSpanForType(type, sourceSpans);

            if (sourceSpan == null) {
                continue;
            }

            nodes.add(toNode(command, sourceSpan, type));
        }

        return nodes;
    }

    private SourceSpan bestSpanForType(
            DocumentEvidenceNode.EvidenceNodeType type,
            List<SourceSpan> sourceSpans
    ) {
        if (sourceSpans == null || sourceSpans.isEmpty()) {
            return null;
        }

        return sourceSpans.stream()
                .filter(span -> span != null)
                .filter(span -> span.excerpt() != null && !span.excerpt().isBlank())
                .filter(span -> spanMatchesType(type, span.excerpt()))
                .findFirst()
                .orElseGet(() -> sourceSpans.stream()
                        .filter(span -> span != null)
                        .filter(span -> span.excerpt() != null && !span.excerpt().isBlank())
                        .findFirst()
                        .orElse(null));
    }

    private boolean spanMatchesType(
            DocumentEvidenceNode.EvidenceNodeType type,
            String excerpt
    ) {
        String text = excerpt == null ? "" : excerpt.toLowerCase();

        return switch (type) {
            case METRIC -> containsAny(
                    text,
                    "%",
                    " p ",
                    "p <",
                    "p >",
                    "r2",
                    "adj r2",
                    "variance",
                    "coefficient",
                    "model",
                    "estimate"
            );
            case FRAMEWORK -> containsAny(
                    text,
                    "regression",
                    "analysis",
                    "analyses",
                    "method",
                    "procedure",
                    "sampling",
                    "model",
                    "ols",
                    "multilevel"
            );
            case CLAIM -> containsAny(
                    text,
                    "we stated",
                    "suggesting",
                    "suggested",
                    "in other words",
                    "evidence for this statement",
                    "independent influences",
                    "cause",
                    "caused",
                    "relationship"
            );
            case ENTITY -> containsAny(text, "mind-wandering", "happiness", "activity", "participants");
            case OBLIGATION, CUSTOM, UNSPECIFIED -> false;
        };
    }

    private DocumentEvidenceNode toNode(
            BuildDocumentEvidenceCommand command,
            SourceSpan span,
            DocumentEvidenceNode.EvidenceNodeType nodeType
    ) {
        String excerpt = span.excerpt();

        return new DocumentEvidenceNode(
                UUID.randomUUID().toString(),
                nodeType,
                nodeType == DocumentEvidenceNode.EvidenceNodeType.CUSTOM ? "custom_evidence" : "",
                titleFor(nodeType, span),
                summarize(excerpt),
                normalizedTextFor(nodeType, excerpt),
                List.of(span.sourceSpanId()),
                VerificationStatus.UNVERIFIED,
                bounded(span.relevanceScore()),
                excerpt.isBlank() ? 0.0 : 1.0,
                excerpt.isBlank(),
                buildTags(command, nodeType),
                List.of("Deterministic fallback evidence node."),
                Map.of(
                        "document_id", span.documentId() == null ? "" : span.documentId().value(),
                        "chunk_id", span.chunkId() == null ? "" : span.chunkId().value(),
                        "citation", span.citation(),
                        "normalization_source", "deterministic_fallback"
                )
        );
    }

    private String titleFor(
            DocumentEvidenceNode.EvidenceNodeType nodeType,
            SourceSpan span
    ) {
        String base = firstNonBlank(span.title(), span.fileName(), "Document evidence");

        return switch (nodeType) {
            case CLAIM -> "Source Claim - " + truncate(base, 70);
            case METRIC -> "Source Metric - " + truncate(base, 70);
            case FRAMEWORK -> "Source Framework - " + truncate(base, 70);
            case ENTITY -> "Source Entity - " + truncate(base, 70);
            case OBLIGATION -> "Source Obligation - " + truncate(base, 70);
            case CUSTOM -> "Custom Evidence - " + truncate(base, 70);
            case UNSPECIFIED -> truncate(base, 90);
        };
    }

    private String normalizedTextFor(
            DocumentEvidenceNode.EvidenceNodeType nodeType,
            String excerpt
    ) {
        String normalized = normalizeWhitespace(excerpt);

        if (normalized.isBlank()) {
            return "";
        }

        if (nodeType == DocumentEvidenceNode.EvidenceNodeType.METRIC) {
            String metricSentence = firstSentenceContaining(
                    normalized,
                    "%",
                    "variance",
                    "p <",
                    "p >",
                    "r2",
                    "coefficient"
            );

            if (!metricSentence.isBlank()) {
                return truncate(metricSentence, 260);
            }
        }

        if (nodeType == DocumentEvidenceNode.EvidenceNodeType.FRAMEWORK) {
            String frameworkSentence = firstSentenceContaining(
                    normalized,
                    "regression",
                    "analysis",
                    "analyses",
                    "method",
                    "sampling",
                    "model",
                    "ols",
                    "multilevel"
            );

            if (!frameworkSentence.isBlank()) {
                return truncate(frameworkSentence, 260);
            }
        }

        if (nodeType == DocumentEvidenceNode.EvidenceNodeType.CLAIM) {
            String claimSentence = firstSentenceContaining(
                    normalized,
                    "we stated",
                    "suggest",
                    "relationship",
                    "cause",
                    "independent influences",
                    "in other words"
            );

            if (!claimSentence.isBlank()) {
                return truncate(claimSentence, 260);
            }
        }

        return truncate(normalized, 260);
    }

    private String firstSentenceContaining(String text, String... terms) {
        if (text == null || text.isBlank()) {
            return "";
        }

        String[] sentences = text.split("(?<=[.!?])\\s+");

        for (String sentence : sentences) {
            String lower = sentence.toLowerCase();

            for (String term : terms) {
                if (term != null && !term.isBlank() && lower.contains(term.toLowerCase())) {
                    return sentence.trim();
                }
            }
        }

        return "";
    }

    private List<DocumentEvidenceNode.EvidenceNodeType> missingRequestedTypes(
            BuildDocumentEvidenceCommand command,
            List<DocumentEvidenceNode> nodes
    ) {
        if (command == null || command.spec() == null || command.spec().requestedNodeTypes().isEmpty()) {
            return List.of();
        }

        EnumSet<DocumentEvidenceNode.EvidenceNodeType> requested =
                EnumSet.noneOf(DocumentEvidenceNode.EvidenceNodeType.class);

        command.spec().requestedNodeTypes()
                .stream()
                .filter(type -> type != null && type != DocumentEvidenceNode.EvidenceNodeType.UNSPECIFIED)
                .forEach(requested::add);

        if (requested.isEmpty()) {
            return List.of();
        }

        EnumSet<DocumentEvidenceNode.EvidenceNodeType> present =
                EnumSet.noneOf(DocumentEvidenceNode.EvidenceNodeType.class);

        if (nodes != null) {
            nodes.stream()
                    .filter(node -> node != null && node.nodeType() != null)
                    .map(DocumentEvidenceNode::nodeType)
                    .forEach(present::add);
        }

        requested.removeAll(present);

        return requested.stream().toList();
    }

    private int remainingNodeBudget(
            BuildDocumentEvidenceCommand command,
            List<DocumentEvidenceNode> nodes
    ) {
        int limit = normalizeLimit(command);
        int current = nodes == null ? 0 : nodes.size();

        return Math.max(0, limit - current);
    }

    private int normalizeLimit(BuildDocumentEvidenceCommand command) {
        if (command == null || command.limit() <= 0) {
            return DEFAULT_MAX_NODES;
        }

        return Math.max(1, command.limit());
    }

    private List<DocumentEvidenceNode> deduplicateNodes(List<DocumentEvidenceNode> nodes) {
        if (nodes == null || nodes.isEmpty()) {
            return List.of();
        }

        Map<String, DocumentEvidenceNode> deduplicated = new LinkedHashMap<>();

        for (DocumentEvidenceNode node : nodes) {
            if (node == null) {
                continue;
            }

            String key = node.nodeType() + "::" + normalizeWhitespace(node.normalizedText()).toLowerCase();

            if (key.isBlank() || key.equals(node.nodeType() + "::")) {
                key = node.nodeType() + "::" + normalizeWhitespace(node.title()).toLowerCase();
            }

            deduplicated.putIfAbsent(key, node);
        }

        return new ArrayList<>(deduplicated.values());
    }

    private DocumentEvidenceNode.EvidenceNodeType defaultNodeType(
            BuildDocumentEvidenceCommand command
    ) {
        if (command == null || command.spec() == null) {
            return DocumentEvidenceNode.EvidenceNodeType.CLAIM;
        }

        return command.spec().requestedNodeTypes()
                .stream()
                .filter(type -> type != null && type != DocumentEvidenceNode.EvidenceNodeType.UNSPECIFIED)
                .findFirst()
                .orElse(DocumentEvidenceNode.EvidenceNodeType.CLAIM);
    }

    private List<String> buildTags(
            BuildDocumentEvidenceCommand command,
            DocumentEvidenceNode.EvidenceNodeType nodeType
    ) {
        List<String> tags = new ArrayList<>();

        if (command != null && command.buildContext() != null) {
            tags.addAll(command.buildContext().topics());
            tags.addAll(command.buildContext().keywords());
        }

        tags.add(nodeType.name().toLowerCase());

        return tags.stream()
                .filter(value -> value != null && !value.isBlank())
                .distinct()
                .limit(12)
                .toList();
    }

    private String summarize(String excerpt) {
        String normalized = normalizeWhitespace(excerpt);

        if (normalized.length() <= 220) {
            return normalized;
        }

        return normalized.substring(0, 220) + "...";
    }

    private String truncate(String value, int maxChars) {
        String normalized = normalizeWhitespace(value);

        if (normalized.length() <= maxChars) {
            return normalized;
        }

        return normalized.substring(0, Math.max(0, maxChars - 3)) + "...";
    }

    private String normalizeWhitespace(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }

        return value.replaceAll("\\s+", " ").trim();
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

    private boolean containsAny(String value, String... terms) {
        if (value == null || terms == null) {
            return false;
        }

        for (String term : terms) {
            if (term != null && !term.isBlank() && value.contains(term.toLowerCase())) {
                return true;
            }
        }

        return false;
    }

    private double bounded(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value) || value < 0.0) {
            return 0.0;
        }

        return Math.min(1.0, value);
    }

    public record NormalizationResult(
            List<DocumentEvidenceNode> nodes,
            List<String> warnings
    ) {
        public NormalizationResult {
            nodes = nodes == null ? List.of() : List.copyOf(nodes);
            warnings = warnings == null ? List.of() : List.copyOf(warnings);
        }
    }
}