package com.sparrowx.document.dice;

import com.sparrowx.document.domain.models.DocumentEvidenceEdge;
import com.sparrowx.document.domain.models.DocumentEvidenceGraph;
import com.sparrowx.document.domain.models.DocumentEvidenceNode;
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

        if (graph.graphId().isBlank()) {
            errors.add("graphId is blank.");
        }

        if (graph.nodes().isEmpty()) {
            errors.add("nodes must not be empty.");
        }

        Set<String> nodeIds = new HashSet<>();

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
        }

        for (DocumentEvidenceEdge edge : graph.edges()) {
            if (edge == null) {
                errors.add("null edge found.");
                continue;
            }

            if (edge.edgeId().isBlank()) {
                errors.add("edgeId is blank.");
            }

            if (!edge.fromNodeId().isBlank() && !nodeIds.contains(edge.fromNodeId())) {
                errors.add("edge references missing fromNodeId=" + edge.fromNodeId());
            }

            if (!edge.toNodeId().isBlank() && !nodeIds.contains(edge.toNodeId())) {
                errors.add("edge references missing toNodeId=" + edge.toNodeId());
            }

            if (edge.relationType() == DocumentEvidenceEdge.EvidenceRelationType.CUSTOM
                    && edge.customRelationType().isBlank()) {
                errors.add("custom relation selected but customRelationType is blank for edgeId=" + edge.edgeId());
            }
        }

        return new ValidationResult(errors.isEmpty(), List.copyOf(errors));
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