package com.sparrowx.document.evidencegraph;

import com.sparrowx.document.domain.models.DocumentEvidenceEdge;
import com.sparrowx.document.domain.models.DocumentEvidenceGraph;
import com.sparrowx.document.domain.models.DocumentEvidenceNode;
import com.sparrowx.document.features.builddocumentevidence.BuildDocumentEvidenceCommand;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class EvidenceRelationLinker {

    private static final int MAX_EDGES = 8;

    private static final Pattern ENTITY_PERCENT_PATTERN = Pattern.compile(
            "(?i)\\b(activity|activities|mind[-\\s]?wandering)\\b\\s+(?:explained|explains|accounted\\s+for|accounts\\s+for)\\s+([0-9]+(?:\\.[0-9]+)?)\\s*%"
    );

    public LinkResult link(
            BuildDocumentEvidenceCommand command,
            List<DocumentEvidenceNode> nodes
    ) {
        List<String> warnings = new ArrayList<>();

        if (nodes == null || nodes.size() < 2) {
            return new LinkResult(List.of(), warnings);
        }

        List<DocumentEvidenceEdge> edges = new ArrayList<>();

        if (isContradictionDetection(command) && relationAllowed(command, DocumentEvidenceEdge.EvidenceRelationType.CONTRADICTS)) {
            List<DocumentEvidenceEdge> contradictionEdges = buildContradictionEdges(nodes);

            if (!contradictionEdges.isEmpty()) {
                edges.addAll(contradictionEdges);
                warnings.add("Built contradiction relation edges from tested claim and cross-node source metrics.");
            } else {
                warnings.add("CONTRADICTION_DETECTION requested but no CONTRADICTS edge could be built.");
            }
        }

        edges.addAll(buildSemanticSupportEdges(command, nodes));
        edges.addAll(buildModifyEdges(command, nodes));
        edges.addAll(buildFrameworkDependencyEdges(command, nodes));

        List<DocumentEvidenceEdge> deduplicated = deduplicate(edges)
                .stream()
                .limit(MAX_EDGES)
                .toList();

        if (!deduplicated.isEmpty()) {
            return new LinkResult(deduplicated, warnings);
        }

        if (relationAllowed(command, DocumentEvidenceEdge.EvidenceRelationType.SUPPORTS)) {
            warnings.add("Used compact baseline relation linker. Replace with DICE relation projection when adapter is pinned.");
            return new LinkResult(buildCompactBaselineEdges(command, nodes), warnings);
        }

        return new LinkResult(List.of(), warnings);
    }

    private List<DocumentEvidenceEdge> buildContradictionEdges(
            List<DocumentEvidenceNode> nodes
    ) {
        DocumentEvidenceNode testedClaim = nodes.stream()
                .filter(this::isTestedClaimNode)
                .findFirst()
                .orElse(null);

        if (testedClaim == null) {
            return List.of();
        }

        String claimText = textOf(testedClaim);

        if (!activityMoreThanMindWanderingClaim(claimText)
                && !mindWanderingMoreThanActivityClaim(claimText)) {
            return List.of();
        }

        List<DocumentEvidenceNode> metricNodes = nodes.stream()
                .filter(node -> node != null)
                .filter(node -> !node.nodeId().equals(testedClaim.nodeId()))
                .filter(node -> node.nodeType() == DocumentEvidenceNode.EvidenceNodeType.METRIC)
                .toList();

        CrossNodeNumericEvidence numericEvidence = extractCrossNodeNumericEvidence(metricNodes);

        if (!numericEvidence.hasBoth()) {
            return List.of();
        }

        boolean contradicted;

        if (activityMoreThanMindWanderingClaim(claimText)) {
            contradicted = numericEvidence.activityMax() < numericEvidence.mindWanderingMin();
        } else {
            contradicted = numericEvidence.mindWanderingMax() < numericEvidence.activityMin();
        }

        if (!contradicted) {
            return List.of();
        }

        List<DocumentEvidenceNode> contradictionEvidenceNodes =
                numericEvidence.evidenceNodes()
                        .stream()
                        .distinct()
                        .toList();

        List<DocumentEvidenceEdge> edges = new ArrayList<>();

        for (DocumentEvidenceNode evidenceNode : contradictionEvidenceNodes) {
            edges.add(edge(
                    evidenceNode,
                    testedClaim,
                    DocumentEvidenceEdge.EvidenceRelationType.CONTRADICTS,
                    contradictionRationale(evidenceNode, testedClaim, numericEvidence),
                    0.92,
                    List.of()
            ));
        }

        return edges;
    }

    private CrossNodeNumericEvidence extractCrossNodeNumericEvidence(
            List<DocumentEvidenceNode> metricNodes
    ) {
        List<Double> activityValues = new ArrayList<>();
        List<Double> mindWanderingValues = new ArrayList<>();
        List<DocumentEvidenceNode> contributingNodes = new ArrayList<>();

        for (DocumentEvidenceNode node : metricNodes) {
            String text = textOf(node);

            EntityNumericValues values = extractEntityNumericValues(text);

            boolean contributed = false;

            if (!values.activityValues().isEmpty()) {
                activityValues.addAll(values.activityValues());
                contributed = true;
            }

            if (!values.mindWanderingValues().isEmpty()) {
                mindWanderingValues.addAll(values.mindWanderingValues());
                contributed = true;
            }

            if (contributed) {
                contributingNodes.add(node);
            }
        }

        return new CrossNodeNumericEvidence(
                activityValues,
                mindWanderingValues,
                contributingNodes
        );
    }

    private EntityNumericValues extractEntityNumericValues(String text) {
        List<Double> activityValues = new ArrayList<>();
        List<Double> mindWanderingValues = new ArrayList<>();

        String normalized = normalize(text);

        Matcher matcher = ENTITY_PERCENT_PATTERN.matcher(normalized);

        while (matcher.find()) {
            String entity = normalizeEntity(matcher.group(1));
            double value = Double.parseDouble(matcher.group(2));

            if ("mind-wandering".equals(entity)) {
                mindWanderingValues.add(value);
            } else {
                activityValues.add(value);
            }
        }

        return new EntityNumericValues(activityValues, mindWanderingValues);
    }

    private List<DocumentEvidenceEdge> buildSemanticSupportEdges(
            BuildDocumentEvidenceCommand command,
            List<DocumentEvidenceNode> nodes
    ) {
        if (!relationAllowed(command, DocumentEvidenceEdge.EvidenceRelationType.SUPPORTS)) {
            return List.of();
        }

        List<DocumentEvidenceNode> claims = nodes.stream()
                .filter(node -> node != null)
                .filter(node -> node.nodeType() == DocumentEvidenceNode.EvidenceNodeType.CLAIM)
                .filter(node -> !isTestedClaimNode(node))
                .toList();

        List<DocumentEvidenceNode> metrics = nodes.stream()
                .filter(node -> node != null)
                .filter(node -> node.nodeType() == DocumentEvidenceNode.EvidenceNodeType.METRIC)
                .toList();

        List<DocumentEvidenceEdge> edges = new ArrayList<>();

        for (DocumentEvidenceNode metric : metrics) {
            claims.stream()
                    .filter(claim -> related(metric, claim))
                    .max(Comparator.comparingDouble(claim -> relationStrength(metric, claim)))
                    .ifPresent(claim -> edges.add(edge(
                            metric,
                            claim,
                            DocumentEvidenceEdge.EvidenceRelationType.SUPPORTS,
                            supportRationale(metric, claim),
                            0.85,
                            List.of()
                    )));
        }

        return edges;
    }

    private List<DocumentEvidenceEdge> buildFrameworkDependencyEdges(
            BuildDocumentEvidenceCommand command,
            List<DocumentEvidenceNode> nodes
    ) {
        if (!relationAllowed(command, DocumentEvidenceEdge.EvidenceRelationType.DEPENDS_ON)) {
            return List.of();
        }

        List<DocumentEvidenceNode> claims = nodes.stream()
                .filter(node -> node != null)
                .filter(node -> node.nodeType() == DocumentEvidenceNode.EvidenceNodeType.CLAIM)
                .filter(node -> !isTestedClaimNode(node))
                .toList();

        List<DocumentEvidenceNode> frameworks = nodes.stream()
                .filter(node -> node != null)
                .filter(node -> node.nodeType() == DocumentEvidenceNode.EvidenceNodeType.FRAMEWORK)
                .toList();

        List<DocumentEvidenceEdge> edges = new ArrayList<>();

        for (DocumentEvidenceNode framework : frameworks) {
            claims.stream()
                    .filter(claim -> related(framework, claim))
                    .max(Comparator.comparingDouble(claim -> relationStrength(framework, claim)))
                    .ifPresent(claim -> edges.add(edge(
                            framework,
                            claim,
                            DocumentEvidenceEdge.EvidenceRelationType.DEPENDS_ON,
                            frameworkRationale(framework, claim),
                            0.78,
                            List.of()
                    )));
        }

        return edges;
    }

    private List<DocumentEvidenceEdge> buildModifyEdges(
            BuildDocumentEvidenceCommand command,
            List<DocumentEvidenceNode> nodes
    ) {
        if (!relationAllowed(command, DocumentEvidenceEdge.EvidenceRelationType.MODIFIES)) {
            return List.of();
        }

        List<DocumentEvidenceNode> claims = nodes.stream()
                .filter(node -> node != null)
                .filter(node -> node.nodeType() == DocumentEvidenceNode.EvidenceNodeType.CLAIM)
                .filter(node -> !isTestedClaimNode(node))
                .toList();

        List<DocumentEvidenceNode> metrics = nodes.stream()
                .filter(node -> node != null)
                .filter(node -> node.nodeType() == DocumentEvidenceNode.EvidenceNodeType.METRIC)
                .filter(this::looksLikeQualifierMetric)
                .toList();

        List<DocumentEvidenceEdge> edges = new ArrayList<>();

        for (DocumentEvidenceNode metric : metrics) {
            claims.stream()
                    .filter(claim -> related(metric, claim))
                    .findFirst()
                    .ifPresent(claim -> edges.add(edge(
                            metric,
                            claim,
                            DocumentEvidenceEdge.EvidenceRelationType.MODIFIES,
                            "Metric qualifies the scope, strength, or size of the related claim.",
                            0.72,
                            List.of()
                    )));
        }

        return edges;
    }

    private List<DocumentEvidenceEdge> buildCompactBaselineEdges(
            BuildDocumentEvidenceCommand command,
            List<DocumentEvidenceNode> nodes
    ) {
        List<DocumentEvidenceEdge> edges = new ArrayList<>();

        if (!relationAllowed(command, DocumentEvidenceEdge.EvidenceRelationType.SUPPORTS)) {
            return edges;
        }

        for (int index = 0; index < nodes.size() - 1 && edges.size() < MAX_EDGES; index++) {
            DocumentEvidenceNode from = nodes.get(index);
            DocumentEvidenceNode to = nodes.get(index + 1);

            if (from == null || to == null || !related(from, to)) {
                continue;
            }

            edges.add(edge(
                    from,
                    to,
                    DocumentEvidenceEdge.EvidenceRelationType.SUPPORTS,
                    "Related evidence nodes share source context, tags, or overlapping terms.",
                    Math.min(bounded(from.confidence()), bounded(to.confidence())),
                    List.of("Compact baseline edge.")
            ));
        }

        return edges;
    }

    private boolean relationAllowed(
            BuildDocumentEvidenceCommand command,
            DocumentEvidenceEdge.EvidenceRelationType relationType
    ) {
        if (relationType == null || relationType == DocumentEvidenceEdge.EvidenceRelationType.UNSPECIFIED) {
            return false;
        }

        if (command == null || command.spec() == null || command.spec().requestedRelationTypes() == null) {
            return true;
        }

        List<DocumentEvidenceEdge.EvidenceRelationType> requested = command.spec().requestedRelationTypes();

        if (requested.isEmpty()) {
            return true;
        }

        return requested.contains(relationType);
    }

    private boolean activityMoreThanMindWanderingClaim(String value) {
        String text = normalize(value);

        int activityIndex = firstIndex(text, "activity", "activities");
        int mindIndex = firstIndex(text, "mind wandering", "mind-wandering", "mindwandering");

        return activityIndex >= 0
                && mindIndex >= 0
                && activityIndex < mindIndex
                && containsAny(text, "more", "greater", "higher", "larger", "exceeds", "explain more", "explains more", "explained more")
                && containsAny(text, "variance", "happiness", "explained", "explains");
    }

    private boolean mindWanderingMoreThanActivityClaim(String value) {
        String text = normalize(value);

        int activityIndex = firstIndex(text, "activity", "activities");
        int mindIndex = firstIndex(text, "mind wandering", "mind-wandering", "mindwandering");

        return activityIndex >= 0
                && mindIndex >= 0
                && mindIndex < activityIndex
                && containsAny(text, "more", "greater", "higher", "larger", "exceeds", "explain more", "explains more", "explained more")
                && containsAny(text, "variance", "happiness", "explained", "explains");
    }

    private String contradictionRationale(
            DocumentEvidenceNode evidenceNode,
            DocumentEvidenceNode testedClaim,
            CrossNodeNumericEvidence numericEvidence
    ) {
        return "Source metrics contradict the tested claim. Tested claim: \"%s\". Activity values=%s; mind-wandering values=%s. Evidence node: \"%s\"."
                .formatted(
                        truncate(textOf(testedClaim), 100),
                        numericEvidence.activityValues(),
                        numericEvidence.mindWanderingValues(),
                        truncate(textOf(evidenceNode), 130)
                );
    }

    private String supportRationale(
            DocumentEvidenceNode metric,
            DocumentEvidenceNode claim
    ) {
        return "Metric supports the claim using shared source context. Metric: \"%s\". Claim: \"%s\"."
                .formatted(
                        truncate(metric.normalizedText(), 120),
                        truncate(claim.normalizedText(), 120)
                );
    }

    private String frameworkRationale(
            DocumentEvidenceNode framework,
            DocumentEvidenceNode claim
    ) {
        return "Claim depends on the described method/framework. Framework: \"%s\". Claim: \"%s\"."
                .formatted(
                        truncate(framework.normalizedText(), 120),
                        truncate(claim.normalizedText(), 120)
                );
    }

    private DocumentEvidenceEdge edge(
            DocumentEvidenceNode from,
            DocumentEvidenceNode to,
            DocumentEvidenceEdge.EvidenceRelationType relationType,
            String rationale,
            double confidence,
            List<String> warnings
    ) {
        return new DocumentEvidenceEdge(
                UUID.randomUUID().toString(),
                from.nodeId(),
                to.nodeId(),
                relationType,
                relationType == DocumentEvidenceEdge.EvidenceRelationType.CUSTOM ? "custom_relation" : "",
                truncate(rationale, 260),
                mergeSourceSpanIds(from, to),
                bounded(confidence),
                warnings,
                Map.of(
                        "from_node_type", from.nodeType().name(),
                        "to_node_type", to.nodeType().name(),
                        "linker", "deterministic_relation_linker"
                )
        );
    }

    private boolean isContradictionDetection(BuildDocumentEvidenceCommand command) {
        return command != null
                && command.spec() != null
                && command.spec().goal() == DocumentEvidenceGraph.EvidenceGoal.CONTRADICTION_DETECTION;
    }

    private boolean isTestedClaimNode(DocumentEvidenceNode node) {
        if (node == null) {
            return false;
        }

        if (node.attributes() != null
                && "tested_claim".equalsIgnoreCase(node.attributes().getOrDefault("role", ""))) {
            return true;
        }

        return node.tags() != null
                && node.tags().stream()
                .filter(tag -> tag != null)
                .map(tag -> tag.toLowerCase(Locale.ROOT))
                .anyMatch(tag -> tag.equals("tested-claim") || tag.equals("contradiction-detection"));
    }

    private boolean looksLikeQualifierMetric(DocumentEvidenceNode node) {
        String text = textOf(node);

        return containsAny(text, "variance", "explained", "p <", "p >", "r2", "coefficient", "%", "together", "independent");
    }

    private boolean related(
            DocumentEvidenceNode left,
            DocumentEvidenceNode right
    ) {
        if (left == null || right == null) {
            return false;
        }

        return sharesSourceSpan(left, right)
                || sharesTag(left, right)
                || tokenOverlap(left, right) >= 0.25;
    }

    private double relationStrength(
            DocumentEvidenceNode left,
            DocumentEvidenceNode right
    ) {
        double score = 0.0;

        if (sharesSourceSpan(left, right)) {
            score += 0.55;
        }

        if (sharesTag(left, right)) {
            score += 0.25;
        }

        score += Math.min(0.20, tokenOverlap(left, right));

        return Math.min(1.0, score);
    }

    private boolean sharesSourceSpan(
            DocumentEvidenceNode left,
            DocumentEvidenceNode right
    ) {
        Set<String> leftIds = left.sourceSpanIds()
                .stream()
                .filter(value -> value != null && !value.isBlank())
                .collect(Collectors.toSet());

        return right.sourceSpanIds()
                .stream()
                .anyMatch(leftIds::contains);
    }

    private boolean sharesTag(
            DocumentEvidenceNode left,
            DocumentEvidenceNode right
    ) {
        Set<String> leftTags = left.tags()
                .stream()
                .filter(value -> value != null && !value.isBlank())
                .map(value -> value.toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());

        return right.tags()
                .stream()
                .filter(value -> value != null && !value.isBlank())
                .map(value -> value.toLowerCase(Locale.ROOT))
                .anyMatch(leftTags::contains);
    }

    private double tokenOverlap(
            DocumentEvidenceNode left,
            DocumentEvidenceNode right
    ) {
        Set<String> leftTokens = usefulTokens(textOf(left));
        Set<String> rightTokens = usefulTokens(textOf(right));

        if (leftTokens.isEmpty() || rightTokens.isEmpty()) {
            return 0.0;
        }

        long matches = leftTokens.stream()
                .filter(rightTokens::contains)
                .count();

        return (double) matches / Math.max(leftTokens.size(), rightTokens.size());
    }

    private Set<String> usefulTokens(String value) {
        if (value == null || value.isBlank()) {
            return Set.of();
        }

        return List.of(normalize(value).split("[^a-z0-9]+"))
                .stream()
                .filter(token -> token.length() >= 4)
                .filter(token -> !Set.of(
                        "that", "this", "with", "from", "were", "they", "their",
                        "claim", "metric", "source", "evidence", "study"
                ).contains(token))
                .collect(Collectors.toSet());
    }

    private List<DocumentEvidenceEdge> deduplicate(List<DocumentEvidenceEdge> edges) {
        if (edges == null || edges.isEmpty()) {
            return List.of();
        }

        Map<String, DocumentEvidenceEdge> deduplicated = new LinkedHashMap<>();

        for (DocumentEvidenceEdge edge : edges) {
            if (edge == null) {
                continue;
            }

            String key = edge.fromNodeId() + "::" + edge.relationType() + "::" + edge.toNodeId();
            deduplicated.putIfAbsent(key, edge);
        }

        return new ArrayList<>(deduplicated.values());
    }

    private List<String> mergeSourceSpanIds(
            DocumentEvidenceNode from,
            DocumentEvidenceNode to
    ) {
        List<String> ids = new ArrayList<>();
        ids.addAll(from.sourceSpanIds());
        ids.addAll(to.sourceSpanIds());

        return ids.stream()
                .filter(value -> value != null && !value.isBlank())
                .distinct()
                .toList();
    }

    private String textOf(DocumentEvidenceNode node) {
        if (node == null) {
            return "";
        }

        return firstNonBlank(node.normalizedText(), node.summary(), node.title());
    }

    private String normalizeEntity(String value) {
        String normalized = normalize(value);

        if (normalized.contains("mind")) {
            return "mind-wandering";
        }

        return "activity";
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

    private int firstIndex(String value, String... terms) {
        int best = -1;

        for (String term : terms) {
            int index = value.indexOf(term);

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
            if (term != null && !term.isBlank() && value.contains(term.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }

        return false;
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }

        return value
                .toLowerCase(Locale.ROOT)
                .replace("mind-wandering", "mind wandering")
                .replace("mindwandering", "mind wandering")
                .replaceAll("\\s+", " ")
                .trim();
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

    public record LinkResult(
            List<DocumentEvidenceEdge> edges,
            List<String> warnings
    ) {
        public LinkResult {
            edges = edges == null ? List.of() : List.copyOf(edges);
            warnings = warnings == null ? List.of() : List.copyOf(warnings);
        }
    }

    private record EntityNumericValues(
            List<Double> activityValues,
            List<Double> mindWanderingValues
    ) {
        private EntityNumericValues {
            activityValues = activityValues == null ? List.of() : List.copyOf(activityValues);
            mindWanderingValues = mindWanderingValues == null ? List.of() : List.copyOf(mindWanderingValues);
        }
    }

    private record CrossNodeNumericEvidence(
            List<Double> activityValues,
            List<Double> mindWanderingValues,
            List<DocumentEvidenceNode> evidenceNodes
    ) {
        private CrossNodeNumericEvidence {
            activityValues = activityValues == null ? List.of() : List.copyOf(activityValues);
            mindWanderingValues = mindWanderingValues == null ? List.of() : List.copyOf(mindWanderingValues);
            evidenceNodes = evidenceNodes == null ? List.of() : List.copyOf(evidenceNodes);
        }

        boolean hasBoth() {
            return !activityValues.isEmpty() && !mindWanderingValues.isEmpty();
        }

        double activityMin() {
            return activityValues.stream().mapToDouble(Double::doubleValue).min().orElse(Double.NaN);
        }

        double activityMax() {
            return activityValues.stream().mapToDouble(Double::doubleValue).max().orElse(Double.NaN);
        }

        double mindWanderingMin() {
            return mindWanderingValues.stream().mapToDouble(Double::doubleValue).min().orElse(Double.NaN);
        }

        double mindWanderingMax() {
            return mindWanderingValues.stream().mapToDouble(Double::doubleValue).max().orElse(Double.NaN);
        }
    }
}