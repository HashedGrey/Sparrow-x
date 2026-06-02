package com.sparrowx.document.dice.gemini;

import com.sparrowx.document.domain.models.DocumentEvidenceNode;
import com.sparrowx.document.domain.models.SourceSpan;
import com.sparrowx.document.features.builddocumentevidence.BuildDocumentEvidenceCommand;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class GeminiProjectionResponseValidator {

    public ValidationResult validate(
            BuildDocumentEvidenceCommand command,
            List<DocumentEvidenceNode> nodes,
            List<SourceSpan> sourceSpans
    ) {
        List<String> warnings = new ArrayList<>();

        if (nodes == null || nodes.isEmpty()) {
            return new ValidationResult(List.of(), List.of("Gemini projection produced no nodes."));
        }

        Set<String> validSourceSpanIds = new HashSet<>();

        if (sourceSpans != null) {
            sourceSpans.stream()
                    .filter(span -> span != null)
                    .map(SourceSpan::sourceSpanId)
                    .filter(value -> value != null && !value.isBlank())
                    .forEach(validSourceSpanIds::add);
        }

        int maxNodes = maxNodes(command, sourceSpans);

        List<DocumentEvidenceNode> validNodes = nodes.stream()
                .filter(node -> node != null)
                .filter(node -> hasValidSourceSpan(node, validSourceSpanIds, warnings))
                .map(this::compactNode)
                .limit(maxNodes)
                .toList();

        if (nodes.size() > validNodes.size()) {
            warnings.add("Gemini projection nodes were capped to " + validNodes.size() + ".");
        }

        if (validNodes.isEmpty()) {
            warnings.add("Gemini projection nodes were rejected because none referenced valid source spans.");
        }

        Set<DocumentEvidenceNode.EvidenceNodeType> missingRequestedTypes =
                missingRequestedTypes(command, validNodes);

        if (!missingRequestedTypes.isEmpty()) {
            warnings.add("Gemini projection missing requested node types: " + missingRequestedTypes);
        }

        return new ValidationResult(validNodes, warnings);
    }

    private int maxNodes(
            BuildDocumentEvidenceCommand command,
            List<SourceSpan> sourceSpans
    ) {
        int requestLimit = command == null || command.limit() <= 0 ? 8 : command.limit();
        int spanLimit = sourceSpans == null || sourceSpans.isEmpty() ? requestLimit : sourceSpans.size();

        return Math.max(1, Math.min(requestLimit, spanLimit));
    }

    private boolean hasValidSourceSpan(
            DocumentEvidenceNode node,
            Set<String> validSourceSpanIds,
            List<String> warnings
    ) {
        if (node.sourceSpanIds() == null || node.sourceSpanIds().isEmpty()) {
            warnings.add("Rejected Gemini node without sourceSpanIds: " + safe(node.title()));
            return false;
        }

        boolean valid = node.sourceSpanIds()
                .stream()
                .anyMatch(validSourceSpanIds::contains);

        if (!valid) {
            warnings.add("Rejected Gemini node with unknown sourceSpanIds: " + safe(node.title()));
        }

        return valid;
    }

    private DocumentEvidenceNode compactNode(DocumentEvidenceNode node) {
        return new DocumentEvidenceNode(
                node.nodeId(),
                node.nodeType(),
                node.customNodeType(),
                truncate(node.title(), 100),
                truncate(node.summary(), 220),
                truncate(node.normalizedText(), 260),
                node.sourceSpanIds(),
                node.verificationStatus(),
                bounded(node.confidence()),
                bounded(node.coverageScore()),
                node.requiresSourceContext(),
                node.tags(),
                compactWarnings(node.warnings()),
                node.attributes()
        );
    }

    private List<String> compactWarnings(List<String> warnings) {
        if (warnings == null || warnings.isEmpty()) {
            return List.of();
        }

        return warnings.stream()
                .filter(value -> value != null && !value.isBlank())
                .distinct()
                .limit(3)
                .toList();
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

    private double bounded(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value) || value < 0.0) {
            return 0.0;
        }

        return Math.min(1.0, value);
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    public record ValidationResult(
            List<DocumentEvidenceNode> nodes,
            List<String> warnings
    ) {
        public ValidationResult {
            nodes = nodes == null ? List.of() : List.copyOf(nodes);
            warnings = warnings == null ? List.of() : List.copyOf(warnings);
        }
    }

    private Set<DocumentEvidenceNode.EvidenceNodeType> missingRequestedTypes(
            BuildDocumentEvidenceCommand command,
            List<DocumentEvidenceNode> validNodes
    ) {
        if (command == null || command.spec() == null || command.spec().requestedNodeTypes() == null) {
            return Set.of();
        }

        Set<DocumentEvidenceNode.EvidenceNodeType> requested =
                command.spec().requestedNodeTypes()
                        .stream()
                        .filter(type -> type != null)
                        .map(Enum::name)
                        .map(name -> name.replace("EVIDENCE_NODE_TYPE_", ""))
                        .map(this::toDomainNodeType)
                        .filter(type -> type != null)
                        .collect(Collectors.toCollection(() ->
                                EnumSet.noneOf(DocumentEvidenceNode.EvidenceNodeType.class)
                        ));

        if (requested.isEmpty()) {
            return Set.of();
        }

        Set<DocumentEvidenceNode.EvidenceNodeType> present =
                validNodes.stream()
                        .map(DocumentEvidenceNode::nodeType)
                        .filter(type -> type != null)
                        .collect(Collectors.toCollection(() ->
                                EnumSet.noneOf(DocumentEvidenceNode.EvidenceNodeType.class)
                        ));

        requested.removeAll(present);
        return requested;
    }

    private DocumentEvidenceNode.EvidenceNodeType toDomainNodeType(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return DocumentEvidenceNode.EvidenceNodeType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }
}