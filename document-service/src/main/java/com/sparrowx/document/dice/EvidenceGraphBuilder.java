package com.sparrowx.document.dice;

import com.sparrowx.document.domain.models.DocumentEvidenceEdge;
import com.sparrowx.document.domain.models.DocumentEvidenceGraph;
import com.sparrowx.document.domain.models.DocumentEvidenceNode;
import com.sparrowx.document.domain.models.SourceSpan;
import com.sparrowx.document.domain.valueobjects.VerificationStatus;
import com.sparrowx.document.features.builddocumentevidence.BuildDocumentEvidenceCommand;
import org.springframework.stereotype.Component;

import java.time.Instant;
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
        double confidence = nodes == null || nodes.isEmpty()
                ? 0.0
                : nodes.stream()
                .mapToDouble(DocumentEvidenceNode::confidence)
                .average()
                .orElse(0.0);

        double coverageScore = coverageScore(command, nodes);

        return new DocumentEvidenceGraph(
                UUID.randomUUID().toString(),
                command.spec().goal(),
                command.spec().customGoal(),
                nodes,
                edges,
                sourcePool,
                VerificationStatus.UNVERIFIED,
                confidence,
                coverageScore,
                warnings,
                missingNodeTypes(command, nodes),
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

        int denominator = command.limit() <= 0 ? 10 : command.limit();

        return Math.min(1.0, (double) nodes.size() / denominator);
    }

    private List<String> missingNodeTypes(
            BuildDocumentEvidenceCommand command,
            List<DocumentEvidenceNode> nodes
    ) {
        if (command.spec().requestedNodeTypes().isEmpty()) {
            return List.of();
        }

        List<DocumentEvidenceNode.EvidenceNodeType> foundTypes = nodes.stream()
                .map(DocumentEvidenceNode::nodeType)
                .distinct()
                .toList();

        return command.spec().requestedNodeTypes()
                .stream()
                .filter(type -> type != null
                        && type != DocumentEvidenceNode.EvidenceNodeType.UNSPECIFIED
                        && !foundTypes.contains(type))
                .map(Enum::name)
                .toList();
    }
}