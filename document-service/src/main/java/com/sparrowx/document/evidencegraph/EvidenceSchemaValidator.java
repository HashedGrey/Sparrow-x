package com.sparrowx.document.evidencegraph;

import com.sparrowx.document.domain.models.DocumentEvidenceEdge;
import com.sparrowx.document.domain.models.DocumentEvidenceGraph;
import com.sparrowx.document.domain.models.DocumentEvidenceNode;
import com.sparrowx.document.domain.models.SourceSpan;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class EvidenceSchemaValidator {

    public ValidationResult validate(DocumentEvidenceGraph graph) {
        if (graph == null) {
            return new ValidationResult(false, List.of("DocumentEvidenceGraph is null."));
        }

        Set<String> errors = new HashSet<>();
        Set<String> nodeIds = new HashSet<>();
        Set<String> sourceSpanIds = new HashSet<>();

        validateGraphShell(graph, errors);
        indexSourceSpanIds(graph, sourceSpanIds, errors);
        validateNodes(graph, nodeIds, sourceSpanIds, errors);
        validateEdges(graph, nodeIds, sourceSpanIds, errors);

        return new ValidationResult(errors.isEmpty(), List.copyOf(errors));
    }

    private void validateGraphShell(
            DocumentEvidenceGraph graph,
            Set<String> errors
    ) {
        if (graph.graphId().isBlank()) {
            errors.add("graphId is blank.");
        }

        if (graph.nodes().isEmpty()) {
            errors.add("nodes must not be empty.");
        }

        if (graph.sourcePool().isEmpty()) {
            errors.add("sourcePool must not be empty.");
        }
    }

    private void indexSourceSpanIds(
            DocumentEvidenceGraph graph,
            Set<String> sourceSpanIds,
            Set<String> errors
    ) {
        for (SourceSpan span : graph.sourcePool()) {
            if (span == null) {
                errors.add("null source span found.");
                continue;
            }

            if (span.sourceSpanId() == null || span.sourceSpanId().isBlank()) {
                errors.add("source span has blank sourceSpanId.");
                continue;
            }

            if (!sourceSpanIds.add(span.sourceSpanId())) {
                errors.add("duplicate sourceSpanId: " + span.sourceSpanId());
            }

            if (span.excerpt() == null || span.excerpt().isBlank()) {
                errors.add("source span has blank excerpt: " + span.sourceSpanId());
            }
        }
    }

    private void validateNodes(
            DocumentEvidenceGraph graph,
            Set<String> nodeIds,
            Set<String> sourceSpanIds,
            Set<String> errors
    ) {
        for (DocumentEvidenceNode node : graph.nodes()) {
            if (node == null) {
                errors.add("null node found.");
                continue;
            }

            if (node.nodeId().isBlank()) {
                errors.add("nodeId is blank.");
            } else if (!nodeIds.add(node.nodeId())) {
                errors.add("duplicate nodeId: " + node.nodeId());
            }

            if (node.nodeType() == DocumentEvidenceNode.EvidenceNodeType.CUSTOM
                    && node.customNodeType().isBlank()) {
                errors.add("custom node type selected but customNodeType is blank for nodeId=" + node.nodeId());
            }

            if (node.normalizedText().isBlank() && node.summary().isBlank() && node.title().isBlank()) {
                errors.add("node has no verifiable text for nodeId=" + node.nodeId());
            }

            if (node.sourceSpanIds().isEmpty()) {
                errors.add("node has no sourceSpanIds for nodeId=" + node.nodeId());
            }

            for (String sourceSpanId : node.sourceSpanIds()) {
                if (sourceSpanId == null || sourceSpanId.isBlank()) {
                    errors.add("node references blank sourceSpanId for nodeId=" + node.nodeId());
                    continue;
                }

                if (!sourceSpanIds.contains(sourceSpanId)) {
                    errors.add("node references missing sourceSpanId=" + sourceSpanId + " nodeId=" + node.nodeId());
                }
            }
        }
    }

    private void validateEdges(
            DocumentEvidenceGraph graph,
            Set<String> nodeIds,
            Set<String> sourceSpanIds,
            Set<String> errors
    ) {
        for (DocumentEvidenceEdge edge : graph.edges()) {
            if (edge == null) {
                errors.add("null edge found.");
                continue;
            }

            if (edge.edgeId().isBlank()) {
                errors.add("edgeId is blank.");
            }

            if (edge.fromNodeId().isBlank()) {
                errors.add("edge has blank fromNodeId for edgeId=" + edge.edgeId());
            } else if (!nodeIds.contains(edge.fromNodeId())) {
                errors.add("edge references missing fromNodeId=" + edge.fromNodeId());
            }

            if (edge.toNodeId().isBlank()) {
                errors.add("edge has blank toNodeId for edgeId=" + edge.edgeId());
            } else if (!nodeIds.contains(edge.toNodeId())) {
                errors.add("edge references missing toNodeId=" + edge.toNodeId());
            }

            if (edge.relationType() == DocumentEvidenceEdge.EvidenceRelationType.CUSTOM
                    && edge.customRelationType().isBlank()) {
                errors.add("custom relation selected but customRelationType is blank for edgeId=" + edge.edgeId());
            }

            if (edge.sourceSpanIds().isEmpty()) {
                errors.add("edge has no sourceSpanIds for edgeId=" + edge.edgeId());
            }

            for (String sourceSpanId : edge.sourceSpanIds()) {
                if (sourceSpanId == null || sourceSpanId.isBlank()) {
                    errors.add("edge references blank sourceSpanId for edgeId=" + edge.edgeId());
                    continue;
                }

                if (!sourceSpanIds.contains(sourceSpanId)) {
                    errors.add("edge references missing sourceSpanId=" + sourceSpanId + " edgeId=" + edge.edgeId());
                }
            }
        }
    }

    public record ValidationResult(
            boolean valid,
            List<String> errors
    ) {
        public ValidationResult {
            errors = errors == null ? List.of() : List.copyOf(errors);
        }
    }
}