package com.sparrowx.document.evidencegraph.gemini;

import com.sparrowx.document.domain.models.DocumentEvidenceGraph;
import com.sparrowx.document.domain.models.DocumentEvidenceNode;
import com.sparrowx.document.domain.models.SourceSpan;
import com.sparrowx.document.features.builddocumentevidence.BuildDocumentEvidenceCommand;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

        Set<String> validSourceSpanIds = validSourceSpanIds(sourceSpans);

        int maxNodes = maxNodes(command, sourceSpans);

        List<DocumentEvidenceNode> validNodes = nodes.stream()
                .filter(node -> node != null)
                .filter(node -> hasStrictValidSourceSpans(node, validSourceSpanIds, warnings))
                .filter(node -> hasVerifiableText(node, warnings))
                .filter(node -> isAllowedForGoal(command, node, warnings))
                .map(node -> compactNode(node, validSourceSpanIds))
                .limit(maxNodes)
                .toList();

        if (nodes.size() > validNodes.size()) {
            warnings.add("Gemini projection rejected or capped nodes from " + nodes.size() + " to " + validNodes.size() + ".");
        }

        if (validNodes.isEmpty()) {
            warnings.add("Gemini projection nodes were rejected because none were valid and source-grounded.");
        }

        Set<DocumentEvidenceNode.EvidenceNodeType> missingRequestedTypes =
                missingRequestedTypes(command, validNodes);

        if (!missingRequestedTypes.isEmpty()) {
            warnings.add("Gemini projection missing requested node types: " + missingRequestedTypes);
        }

        return new ValidationResult(validNodes, warnings);
    }

    private boolean isAllowedForGoal(
            BuildDocumentEvidenceCommand command,
            DocumentEvidenceNode node,
            List<String> warnings
    ) {
        if (command == null || command.spec() == null) {
            return true;
        }

        if (command.spec().goal() != DocumentEvidenceGraph.EvidenceGoal.CONTRADICTION_DETECTION) {
            return true;
        }

        if (looksLikeUserTestedClaim(command, node)) {
            warnings.add("Rejected Gemini node that appeared to restate the tested contradiction claim: " + safe(node.title()));
            return false;
        }

        return true;
    }

    private boolean looksLikeUserTestedClaim(
            BuildDocumentEvidenceCommand command,
            DocumentEvidenceNode node
    ) {
        String targetClaim = targetClaim(command);

        if (targetClaim.isBlank()) {
            return false;
        }

        String nodeText = (safe(node.title()) + " " + safe(node.summary()) + " " + safe(node.normalizedText()))
                .toLowerCase();

        String normalizedTarget = targetClaim.toLowerCase();

        if (nodeText.contains(normalizedTarget)) {
            return true;
        }

        return containsAny(normalizedTarget, "activity explains more", "activity explained more")
                && containsAny(nodeText, "activity explains more", "activity explained more");
    }

    private String targetClaim(BuildDocumentEvidenceCommand command) {
        if (command == null || command.spec() == null || command.buildContext() == null) {
            return "";
        }

        String explicit = firstNonBlank(
                option(command.spec().options(), "target_claim"),
                option(command.spec().options(), "tested_claim"),
                option(command.spec().options(), "claim"),
                option(command.spec().options(), "proposition")
        );

        if (!explicit.isBlank()) {
            return explicit;
        }

        String fromHint = extractAfterMarker(command.buildContext().retrievalHint(), "claim:");
        if (!fromHint.isBlank()) {
            return fromHint;
        }

        String fromFocus = extractAfterMarker(option(command.spec().options(), "focus"), "claim that");
        if (!fromFocus.isBlank()) {
            return fromFocus;
        }

        return "";
    }

    private Set<String> validSourceSpanIds(List<SourceSpan> sourceSpans) {
        Set<String> validSourceSpanIds = new HashSet<>();

        if (sourceSpans != null) {
            sourceSpans.stream()
                    .filter(span -> span != null)
                    .map(SourceSpan::sourceSpanId)
                    .filter(value -> value != null && !value.isBlank())
                    .forEach(validSourceSpanIds::add);
        }

        return validSourceSpanIds;
    }

    private int maxNodes(
            BuildDocumentEvidenceCommand command,
            List<SourceSpan> sourceSpans
    ) {
        int requestLimit = command == null || command.limit() <= 0 ? 8 : command.limit();
        int spanLimit = sourceSpans == null || sourceSpans.isEmpty() ? requestLimit : sourceSpans.size();

        return Math.max(1, Math.min(requestLimit, spanLimit));
    }

    private boolean hasStrictValidSourceSpans(
            DocumentEvidenceNode node,
            Set<String> validSourceSpanIds,
            List<String> warnings
    ) {
        if (node.sourceSpanIds() == null || node.sourceSpanIds().isEmpty()) {
            warnings.add("Rejected Gemini node without sourceSpanIds: " + safe(node.title()));
            return false;
        }

        boolean allValid = node.sourceSpanIds()
                .stream()
                .allMatch(validSourceSpanIds::contains);

        if (!allValid) {
            warnings.add("Rejected Gemini node with one or more unknown sourceSpanIds: " + safe(node.title()));
            return false;
        }

        return true;
    }

    private boolean hasVerifiableText(
            DocumentEvidenceNode node,
            List<String> warnings
    ) {
        boolean valid = !safe(node.normalizedText()).isBlank()
                || !safe(node.summary()).isBlank()
                || !safe(node.title()).isBlank();

        if (!valid) {
            warnings.add("Rejected Gemini node without verifiable text.");
        }

        return valid;
    }

    private DocumentEvidenceNode compactNode(
            DocumentEvidenceNode node,
            Set<String> validSourceSpanIds
    ) {
        List<String> cleanSourceSpanIds = node.sourceSpanIds()
                .stream()
                .filter(validSourceSpanIds::contains)
                .distinct()
                .toList();

        return new DocumentEvidenceNode(
                node.nodeId(),
                node.nodeType(),
                node.customNodeType(),
                truncate(node.title(), 100),
                truncate(node.summary(), 220),
                truncate(node.normalizedText(), 260),
                cleanSourceSpanIds,
                node.verificationStatus(),
                bounded(node.confidence()),
                bounded(node.coverageScore()),
                node.requiresSourceContext(),
                compactStrings(node.tags(), 12),
                compactStrings(node.warnings(), 3),
                node.attributes()
        );
    }

    private List<String> compactStrings(List<String> values, int limit) {
        if (values == null || values.isEmpty()) {
            return List.of();
        }

        return values.stream()
                .filter(value -> value != null && !value.isBlank())
                .map(value -> truncate(value, 180))
                .distinct()
                .limit(limit)
                .toList();
    }

    private Set<DocumentEvidenceNode.EvidenceNodeType> missingRequestedTypes(
            BuildDocumentEvidenceCommand command,
            List<DocumentEvidenceNode> validNodes
    ) {
        if (command == null || command.spec() == null || command.spec().requestedNodeTypes() == null) {
            return Set.of();
        }

        EnumSet<DocumentEvidenceNode.EvidenceNodeType> requested =
                EnumSet.noneOf(DocumentEvidenceNode.EvidenceNodeType.class);

        command.spec().requestedNodeTypes()
                .stream()
                .filter(type -> type != null && type != DocumentEvidenceNode.EvidenceNodeType.UNSPECIFIED)
                .forEach(requested::add);

        if (requested.isEmpty()) {
            return Set.of();
        }

        EnumSet<DocumentEvidenceNode.EvidenceNodeType> present =
                EnumSet.noneOf(DocumentEvidenceNode.EvidenceNodeType.class);

        validNodes.stream()
                .filter(node -> node != null && node.nodeType() != null)
                .map(DocumentEvidenceNode::nodeType)
                .forEach(present::add);

        requested.removeAll(present);

        return requested;
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

    private String option(java.util.Map<String, String> options, String key) {
        if (options == null || key == null) {
            return "";
        }

        return options.getOrDefault(key, "");
    }

    private String extractAfterMarker(String value, String marker) {
        if (value == null || value.isBlank() || marker == null || marker.isBlank()) {
            return "";
        }

        String lowerValue = value.toLowerCase();
        String lowerMarker = marker.toLowerCase();

        int index = lowerValue.indexOf(lowerMarker);

        if (index < 0) {
            return "";
        }

        String remainder = value.substring(index + marker.length()).trim();

        int boundary = firstSentenceBoundary(remainder);

        if (boundary > 0) {
            return remainder.substring(0, boundary).trim();
        }

        return remainder;
    }

    private int firstSentenceBoundary(String value) {
        if (value == null || value.isBlank()) {
            return -1;
        }

        int best = -1;

        for (char boundary : new char[]{'.', '?', '!'}) {
            int index = value.indexOf(boundary);
            if (index >= 0 && (best < 0 || index < best)) {
                best = index;
            }
        }

        return best;
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
}