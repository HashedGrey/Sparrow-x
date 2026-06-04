package com.sparrowx.document.evidencegraph;

import com.sparrowx.document.domain.models.DocumentEvidenceEdge;
import com.sparrowx.document.domain.models.DocumentEvidenceGraph;
import com.sparrowx.document.domain.models.DocumentEvidenceNode;
import com.sparrowx.document.domain.models.SourceSpan;
import com.sparrowx.document.domain.valueobjects.VerificationStatus;
import com.sparrowx.document.features.builddocumentevidence.BuildDocumentEvidenceCommand;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

@Component
public class EvidenceGraphBuilder {

    public DocumentEvidenceGraph build(
            BuildDocumentEvidenceCommand command,
            List<DocumentEvidenceNode> nodes,
            List<DocumentEvidenceEdge> edges,
            List<SourceSpan> sourcePool,
            List<String> warnings
    ) {
        List<DocumentEvidenceNode> safeNodes = nodes == null ? List.of() : List.copyOf(nodes);
        List<DocumentEvidenceEdge> safeEdges = edges == null ? List.of() : List.copyOf(edges);

        double confidence = safeNodes.isEmpty()
                ? 0.0
                : safeNodes.stream()
                .mapToDouble(DocumentEvidenceNode::confidence)
                .average()
                .orElse(0.0);

        double coverageScore = coverageScore(command, safeNodes);

        return new DocumentEvidenceGraph(
                UUID.randomUUID().toString(),
                command.spec().goal(),
                command.spec().customGoal(),
                safeNodes,
                safeEdges,
                sourcePool,
                VerificationStatus.UNVERIFIED,
                bounded(confidence),
                coverageScore,
                warnings,
                missingNodeTypes(command, safeNodes),
                command.spec().outputSchemaRef(),
                command.spec().outputSchemaVersion(),
                Instant.now()
        );
    }

    private double coverageScore(
            BuildDocumentEvidenceCommand command,
            List<DocumentEvidenceNode> nodes
    ) {
        if (nodes == null || nodes.isEmpty()) {
            return 0.0;
        }

        if (command == null || command.spec() == null || command.spec().requestedNodeTypes().isEmpty()) {
            return 1.0;
        }

        EnumSet<DocumentEvidenceNode.EvidenceNodeType> requested =
                EnumSet.noneOf(DocumentEvidenceNode.EvidenceNodeType.class);

        command.spec().requestedNodeTypes()
                .stream()
                .filter(type -> type != null && type != DocumentEvidenceNode.EvidenceNodeType.UNSPECIFIED)
                .forEach(requested::add);

        if (requested.isEmpty()) {
            return 1.0;
        }

        EnumSet<DocumentEvidenceNode.EvidenceNodeType> found =
                EnumSet.noneOf(DocumentEvidenceNode.EvidenceNodeType.class);

        nodes.stream()
                .filter(node -> node != null && node.nodeType() != null)
                .map(DocumentEvidenceNode::nodeType)
                .filter(type -> type != DocumentEvidenceNode.EvidenceNodeType.UNSPECIFIED)
                .forEach(found::add);

        int foundRequested = 0;

        for (DocumentEvidenceNode.EvidenceNodeType type : requested) {
            if (found.contains(type)) {
                foundRequested++;
            }
        }

        return bounded((double) foundRequested / requested.size());
    }

    private List<String> missingNodeTypes(
            BuildDocumentEvidenceCommand command,
            List<DocumentEvidenceNode> nodes
    ) {
        if (command == null || command.spec() == null || command.spec().requestedNodeTypes().isEmpty()) {
            return List.of();
        }

        EnumSet<DocumentEvidenceNode.EvidenceNodeType> found =
                EnumSet.noneOf(DocumentEvidenceNode.EvidenceNodeType.class);

        if (nodes != null) {
            nodes.stream()
                    .filter(node -> node != null && node.nodeType() != null)
                    .map(DocumentEvidenceNode::nodeType)
                    .forEach(found::add);
        }

        return command.spec().requestedNodeTypes()
                .stream()
                .filter(type -> type != null
                        && type != DocumentEvidenceNode.EvidenceNodeType.UNSPECIFIED
                        && !found.contains(type))
                .map(Enum::name)
                .distinct()
                .toList();
    }

    private double bounded(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value) || value < 0.0) {
            return 0.0;
        }

        return Math.min(1.0, value);
    }
}