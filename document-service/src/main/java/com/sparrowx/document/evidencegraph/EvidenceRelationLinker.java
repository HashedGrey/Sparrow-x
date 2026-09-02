package com.sparrowx.document.evidencegraph;

import com.sparrowx.document.domain.models.DocumentEvidenceEdge;
import com.sparrowx.document.domain.models.DocumentEvidenceGraph;
import com.sparrowx.document.domain.models.DocumentEvidenceNode;
import com.sparrowx.document.domain.valueobjects.VerificationStatus;
import com.sparrowx.document.features.builddocumentevidence.BuildDocumentEvidenceCommand;
import com.sparrowx.document.verification.NumericComparisonEvaluator;
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


    private final NumericComparisonEvaluator numericComparisonEvaluator;

    public EvidenceRelationLinker(
            NumericComparisonEvaluator numericComparisonEvaluator
    ) {
        this.numericComparisonEvaluator = numericComparisonEvaluator;
    }

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
                warnings.add("Built source-backed contradiction relation edges from numeric comparison evidence.");
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
            warnings.add("Used deterministic baseline relation linking.");
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

        List<DocumentEvidenceNode> evidenceNodes = nodes.stream()
                .filter(node -> node != null)
                .filter(node -> !node.nodeId().equals(testedClaim.nodeId()))
                .toList();

        if (evidenceNodes.isEmpty()) {
            return List.of();
        }

        NumericComparisonEvaluator.Result result =
                numericComparisonEvaluator.evaluate(
                                textOf(testedClaim),
                                evidenceNodes.stream()
                                        .map(this::textOf)
                                        .toList()
                        )
                        .orElse(null);

        if (result == null
                || result.status() != VerificationStatus.CONTRADICTED) {
            return List.of();
        }

        List<DocumentEvidenceEdge> edges = new ArrayList<>();

        for (Integer index : result.evidenceIndexes()) {
            if (index == null
                    || index < 0
                    || index >= evidenceNodes.size()) {
                continue;
            }

            DocumentEvidenceNode evidenceNode =
                    evidenceNodes.get(index);

            edges.add(edge(
                    evidenceNode,
                    testedClaim,
                    DocumentEvidenceEdge.EvidenceRelationType.CONTRADICTS,
                    result.explanation(),
                    result.confidence(),
                    List.of()
            ));
        }

        return edges;
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

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }

        return value
                .toLowerCase(Locale.ROOT)
                .replace('-', ' ')
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

}