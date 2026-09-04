package com.sparrowx.document.evidencegraph;

import com.sparrowx.document.domain.models.DocumentEvidenceEdge;
import com.sparrowx.document.domain.models.DocumentEvidenceNode;
import com.sparrowx.document.features.builddocumentevidence.BuildDocumentEvidenceCommand;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class EvidenceRelationLinker {

    private static final int MAX_EDGES = 8;
    private static final double MIN_SIMILARITY_OVERLAP = 0.50;

    public LinkResult link(
            BuildDocumentEvidenceCommand command,
            List<DocumentEvidenceNode> nodes
    ) {
        List<String> warnings = new ArrayList<>();

        if (nodes == null || nodes.size() < 2) {
            return new LinkResult(List.of(), warnings);
        }

        List<DocumentEvidenceEdge> edges = new ArrayList<>();

        /*
         * document-service may establish document-local similarity
         * deterministically.
         *
         * Semantic assertions such as SUPPORTS, CONTRADICTS,
         * MODIFIES and DEPENDS_ON are not inferred here.
         * Those judgments belong to agentic-service.
         */
        if (relationAllowed(
                command,
                DocumentEvidenceEdge.EvidenceRelationType.SIMILAR_TO
        )) {
            edges.addAll(buildSimilarityEdges(nodes));
        }

        List<DocumentEvidenceEdge> deduplicated =
                deduplicate(edges)
                        .stream()
                        .limit(MAX_EDGES)
                        .toList();

        List<DocumentEvidenceEdge.EvidenceRelationType> delegatedRelations =
                delegatedRequestedRelations(command);

        if (!delegatedRelations.isEmpty()) {
            warnings.add(
                    "Semantic relation types are not inferred by document-service and must be "
                            + "resolved by agentic-service: "
                            + delegatedRelations
            );
        }

        return new LinkResult(
                deduplicated,
                warnings
        );
    }

    private List<DocumentEvidenceEdge> buildSimilarityEdges(
            List<DocumentEvidenceNode> nodes
    ) {
        List<DocumentEvidenceEdge> edges = new ArrayList<>();

        for (int leftIndex = 0;
             leftIndex < nodes.size() - 1 && edges.size() < MAX_EDGES;
             leftIndex++) {

            DocumentEvidenceNode left = nodes.get(leftIndex);

            if (left == null) {
                continue;
            }

            for (int rightIndex = leftIndex + 1;
                 rightIndex < nodes.size() && edges.size() < MAX_EDGES;
                 rightIndex++) {

                DocumentEvidenceNode right = nodes.get(rightIndex);

                if (right == null) {
                    continue;
                }

                double overlap = tokenOverlap(left, right);

                boolean sameSource =
                        sharesSourceSpan(left, right);

                if (!sameSource
                        && overlap < MIN_SIMILARITY_OVERLAP) {
                    continue;
                }

                double confidence =
                        sameSource
                                ? 1.0
                                : bounded(overlap);

                edges.add(
                        edge(
                                left,
                                right,
                                DocumentEvidenceEdge.EvidenceRelationType.SIMILAR_TO,
                                sameSource
                                        ? "Evidence nodes reference the same grounded source span."
                                        : "Evidence nodes have high deterministic lexical overlap.",
                                confidence
                        )
                );
            }
        }

        return edges;
    }

    private List<DocumentEvidenceEdge.EvidenceRelationType>
    delegatedRequestedRelations(
            BuildDocumentEvidenceCommand command
    ) {
        if (command == null
                || command.spec() == null
                || command.spec().requestedRelationTypes() == null
                || command.spec().requestedRelationTypes().isEmpty()) {
            return List.of();
        }

        return command.spec()
                .requestedRelationTypes()
                .stream()
                .filter(type -> type != null)
                .filter(type ->
                        type
                                != DocumentEvidenceEdge.EvidenceRelationType.UNSPECIFIED
                )
                .filter(type ->
                        type
                                != DocumentEvidenceEdge.EvidenceRelationType.SIMILAR_TO
                )
                .distinct()
                .toList();
    }

    private boolean relationAllowed(
            BuildDocumentEvidenceCommand command,
            DocumentEvidenceEdge.EvidenceRelationType relationType
    ) {
        if (relationType == null
                || relationType
                == DocumentEvidenceEdge.EvidenceRelationType.UNSPECIFIED) {
            return false;
        }

        if (command == null
                || command.spec() == null
                || command.spec().requestedRelationTypes() == null) {
            return true;
        }

        List<DocumentEvidenceEdge.EvidenceRelationType> requested =
                command.spec().requestedRelationTypes();

        if (requested.isEmpty()) {
            return true;
        }

        return requested.contains(relationType);
    }

    private DocumentEvidenceEdge edge(
            DocumentEvidenceNode from,
            DocumentEvidenceNode to,
            DocumentEvidenceEdge.EvidenceRelationType relationType,
            String rationale,
            double confidence
    ) {
        return new DocumentEvidenceEdge(
                UUID.randomUUID().toString(),
                from.nodeId(),
                to.nodeId(),
                relationType,
                "",
                rationale,
                mergeSourceSpanIds(from, to),
                bounded(confidence),
                List.of(),
                Map.of(
                        "from_node_type", from.nodeType().name(),
                        "to_node_type", to.nodeType().name(),
                        "linker", "deterministic_relation_linker"
                )
        );
    }

    private boolean sharesSourceSpan(
            DocumentEvidenceNode left,
            DocumentEvidenceNode right
    ) {
        Set<String> leftIds =
                left.sourceSpanIds()
                        .stream()
                        .filter(value ->
                                value != null && !value.isBlank()
                        )
                        .collect(Collectors.toSet());

        return right.sourceSpanIds()
                .stream()
                .anyMatch(leftIds::contains);
    }

    private double tokenOverlap(
            DocumentEvidenceNode left,
            DocumentEvidenceNode right
    ) {
        Set<String> leftTokens =
                usefulTokens(textOf(left));

        Set<String> rightTokens =
                usefulTokens(textOf(right));

        if (leftTokens.isEmpty()
                || rightTokens.isEmpty()) {
            return 0.0;
        }

        long matches =
                leftTokens.stream()
                        .filter(rightTokens::contains)
                        .count();

        return (double) matches
                / Math.max(
                leftTokens.size(),
                rightTokens.size()
        );
    }

    private Set<String> usefulTokens(String value) {
        if (value == null || value.isBlank()) {
            return Set.of();
        }

        return List.of(
                        normalize(value)
                                .split("[^a-z0-9]+")
                )
                .stream()
                .filter(token -> token.length() >= 4)
                .filter(token ->
                        !Set.of(
                                "that",
                                "this",
                                "with",
                                "from",
                                "were",
                                "they",
                                "their",
                                "claim",
                                "metric",
                                "source",
                                "evidence",
                                "study"
                        ).contains(token)
                )
                .collect(Collectors.toSet());
    }

    private List<DocumentEvidenceEdge> deduplicate(
            List<DocumentEvidenceEdge> edges
    ) {
        if (edges == null || edges.isEmpty()) {
            return List.of();
        }

        Map<String, DocumentEvidenceEdge> deduplicated =
                new LinkedHashMap<>();

        for (DocumentEvidenceEdge edge : edges) {
            if (edge == null) {
                continue;
            }

            String key =
                    edge.fromNodeId()
                            + "::"
                            + edge.relationType()
                            + "::"
                            + edge.toNodeId();

            deduplicated.putIfAbsent(key, edge);
        }

        return new ArrayList<>(
                deduplicated.values()
        );
    }

    private List<String> mergeSourceSpanIds(
            DocumentEvidenceNode from,
            DocumentEvidenceNode to
    ) {
        List<String> ids = new ArrayList<>();

        ids.addAll(from.sourceSpanIds());
        ids.addAll(to.sourceSpanIds());

        return ids.stream()
                .filter(value ->
                        value != null && !value.isBlank()
                )
                .distinct()
                .toList();
    }

    private String textOf(
            DocumentEvidenceNode node
    ) {
        if (node == null) {
            return "";
        }

        return firstNonBlank(
                node.normalizedText(),
                node.summary(),
                node.title()
        );
    }

    private String firstNonBlank(
            String... values
    ) {
        if (values == null) {
            return "";
        }

        for (String value : values) {
            if (value != null
                    && !value.isBlank()) {
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

    private double bounded(double value) {
        if (Double.isNaN(value)
                || Double.isInfinite(value)
                || value < 0.0) {
            return 0.0;
        }

        return Math.min(1.0, value);
    }

    public record LinkResult(
            List<DocumentEvidenceEdge> edges,
            List<String> warnings
    ) {
        public LinkResult {
            edges = edges == null
                    ? List.of()
                    : List.copyOf(edges);

            warnings = warnings == null
                    ? List.of()
                    : List.copyOf(warnings);
        }
    }
}