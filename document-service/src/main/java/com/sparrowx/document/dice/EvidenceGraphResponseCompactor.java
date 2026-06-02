package com.sparrowx.document.dice;

import com.sparrowx.document.domain.models.DocumentEvidenceEdge;
import com.sparrowx.document.domain.models.DocumentEvidenceGraph;
import com.sparrowx.document.domain.models.DocumentEvidenceNode;
import com.sparrowx.document.domain.models.SourceSpan;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Component
public class EvidenceGraphResponseCompactor {

    private static final int MAX_TITLE_CHARS = 100;
    private static final int MAX_SUMMARY_CHARS = 220;
    private static final int MAX_NORMALIZED_TEXT_CHARS = 260;
    private static final int MAX_EXCERPT_CHARS = 1_200;
    private static final int MAX_WARNINGS = 8;

    public DocumentEvidenceGraph compact(DocumentEvidenceGraph graph) {
        if (graph == null) {
            return null;
        }

        List<DocumentEvidenceNode> compactNodes = graph.nodes()
                .stream()
                .map(this::compactNode)
                .toList();

        List<DocumentEvidenceEdge> compactEdges = graph.edges()
                .stream()
                .map(this::compactEdge)
                .toList();

        List<SourceSpan> compactSourcePool = graph.sourcePool()
                .stream()
                .map(this::compactSourceSpan)
                .toList();

        return new DocumentEvidenceGraph(
                graph.graphId(),
                graph.goal(),
                graph.customGoal(),
                compactNodes,
                compactEdges,
                compactSourcePool,
                graph.verificationStatus(),
                graph.confidence(),
                graph.coverageScore(),
                compactWarnings(graph.warnings()),
                graph.missingNodeTypes(),
                graph.outputSchemaRef(),
                graph.outputSchemaVersion(),
                graph.createdAt()
        );
    }

    private DocumentEvidenceNode compactNode(DocumentEvidenceNode node) {
        if (node == null) {
            return null;
        }

        return new DocumentEvidenceNode(
                node.nodeId(),
                node.nodeType(),
                node.customNodeType(),
                truncate(node.title(), MAX_TITLE_CHARS),
                truncate(node.summary(), MAX_SUMMARY_CHARS),
                truncate(node.normalizedText(), MAX_NORMALIZED_TEXT_CHARS),
                node.sourceSpanIds(),
                node.verificationStatus(),
                node.confidence(),
                node.coverageScore(),
                node.requiresSourceContext(),
                node.tags(),
                compactWarnings(node.warnings()),
                node.attributes()
        );
    }

    private DocumentEvidenceEdge compactEdge(DocumentEvidenceEdge edge) {
        if (edge == null) {
            return null;
        }

        return new DocumentEvidenceEdge(
                edge.edgeId(),
                edge.fromNodeId(),
                edge.toNodeId(),
                edge.relationType(),
                edge.customRelationType(),
                truncate(edge.rationale(), 180),
                edge.sourceSpanIds(),
                edge.confidence(),
                compactWarnings(edge.warnings()),
                edge.attributes()
        );
    }

    private SourceSpan compactSourceSpan(SourceSpan span) {
        if (span == null) {
            return null;
        }

        return new SourceSpan(
                span.sourceSpanId(),
                span.sourceKind(),
                span.documentId(),
                span.chunkId(),
                span.claimId(),
                span.title(),
                span.fileName(),
                span.pageStart(),
                span.pageEnd(),
                span.citation(),
                truncate(span.excerpt(), MAX_EXCERPT_CHARS),
                span.relevanceScore(),
                span.metadata()
        );
    }

    private List<String> compactWarnings(List<String> warnings) {
        if (warnings == null || warnings.isEmpty()) {
            return List.of();
        }

        List<String> unique = warnings.stream()
                .filter(value -> value != null && !value.isBlank())
                .distinct()
                .toList();

        List<String> critical = unique.stream()
                .filter(this::isCriticalDiceWarning)
                .toList();

        List<String> remaining = unique.stream()
                .filter(value -> !isCriticalDiceWarning(value))
                .limit(Math.max(0, 8 - critical.size()))
                .toList();

        List<String> compact = new java.util.ArrayList<>();
        compact.addAll(critical);
        compact.addAll(remaining);

        return compact.stream()
                .distinct()
                .limit(8)
                .toList();
    }

    private boolean isCriticalDiceWarning(String warning) {
        String value = warning.toLowerCase();

        return value.contains("gemini")
                || value.contains("projection")
                || value.contains("fallback")
                || value.contains("rejected")
                || value.contains("json")
                || value.contains("retry");
    }

    private String truncate(String value, int maxChars) {
        if (value == null || value.isBlank()) {
            return "";
        }

        String normalized = value.replaceAll("\\s+", " ").trim();

        if (normalized.length() <= maxChars) {
            return normalized;
        }

        return normalized.substring(0, Math.max(0, maxChars - 3)) + "...";
    }
}