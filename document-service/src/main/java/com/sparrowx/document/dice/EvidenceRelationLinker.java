package com.sparrowx.document.dice;

import com.sparrowx.document.domain.models.DocumentEvidenceEdge;
import com.sparrowx.document.domain.models.DocumentEvidenceNode;
import com.sparrowx.document.features.builddocumentevidence.BuildDocumentEvidenceCommand;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class EvidenceRelationLinker {

    private static final int MAX_BASELINE_EDGES = 4;

    public LinkResult link(
            BuildDocumentEvidenceCommand command,
            List<DocumentEvidenceNode> nodes
    ) {
        List<String> warnings = new ArrayList<>();

        if (nodes == null || nodes.size() < 2) {
            return new LinkResult(List.of(), warnings);
        }

        List<DocumentEvidenceEdge> semanticEdges = buildSemanticEdges(command, nodes);

        if (!semanticEdges.isEmpty()) {
            return new LinkResult(semanticEdges, warnings);
        }

        warnings.add("Used compact baseline relation linker. Replace with DICE relation projection when adapter is pinned.");

        return new LinkResult(buildCompactBaselineEdges(command, nodes), warnings);
    }

    private List<DocumentEvidenceEdge> buildSemanticEdges(
            BuildDocumentEvidenceCommand command,
            List<DocumentEvidenceNode> nodes
    ) {
        List<DocumentEvidenceNode> claims = nodes.stream()
                .filter(node -> node.nodeType() == DocumentEvidenceNode.EvidenceNodeType.CLAIM)
                .toList();

        List<DocumentEvidenceNode> metrics = nodes.stream()
                .filter(node -> node.nodeType() == DocumentEvidenceNode.EvidenceNodeType.METRIC)
                .toList();

        List<DocumentEvidenceNode> frameworks = nodes.stream()
                .filter(node -> node.nodeType() == DocumentEvidenceNode.EvidenceNodeType.FRAMEWORK)
                .toList();

        List<DocumentEvidenceEdge> edges = new ArrayList<>();

        for (DocumentEvidenceNode metric : metrics) {
            claims.stream()
                    .filter(claim -> related(metric, claim))
                    .findFirst()
                    .ifPresent(claim -> edges.add(edge(
                            metric,
                            claim,
                            relationType(command, DocumentEvidenceEdge.EvidenceRelationType.SUPPORTS),
                            "Metric evidence supports the related claim.",
                            0.85,
                            List.of()
                    )));
        }

        for (DocumentEvidenceNode framework : frameworks) {
            claims.stream()
                    .filter(claim -> related(framework, claim))
                    .findFirst()
                    .ifPresent(claim -> edges.add(edge(
                            framework,
                            claim,
                            relationType(command, DocumentEvidenceEdge.EvidenceRelationType.DEPENDS_ON),
                            "Claim depends on the described framework or method.",
                            0.75,
                            List.of()
                    )));
        }

        return edges.stream()
                .limit(MAX_BASELINE_EDGES)
                .toList();
    }

    private List<DocumentEvidenceEdge> buildCompactBaselineEdges(
            BuildDocumentEvidenceCommand command,
            List<DocumentEvidenceNode> nodes
    ) {
        List<DocumentEvidenceEdge> edges = new ArrayList<>();

        for (int index = 0; index < nodes.size() - 1 && edges.size() < MAX_BASELINE_EDGES; index++) {
            DocumentEvidenceNode from = nodes.get(index);
            DocumentEvidenceNode to = nodes.get(index + 1);

            if (!related(from, to)) {
                continue;
            }

            edges.add(edge(
                    from,
                    to,
                    relationType(command, DocumentEvidenceEdge.EvidenceRelationType.SUPPORTS),
                    "Related evidence nodes share source context or topic tags.",
                    Math.min(from.confidence(), to.confidence()),
                    List.of("Compact baseline edge.")
            ));
        }

        return edges;
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
                rationale,
                mergeSourceSpanIds(from, to),
                bounded(confidence),
                warnings,
                java.util.Map.of()
        );
    }

    private DocumentEvidenceEdge.EvidenceRelationType relationType(
            BuildDocumentEvidenceCommand command,
            DocumentEvidenceEdge.EvidenceRelationType preferred
    ) {
        if (command == null
                || command.spec() == null
                || command.spec().requestedRelationTypes().isEmpty()) {
            return preferred;
        }

        if (command.spec().requestedRelationTypes().contains(preferred)) {
            return preferred;
        }

        return command.spec().requestedRelationTypes()
                .stream()
                .filter(type -> type != null
                        && type != DocumentEvidenceEdge.EvidenceRelationType.UNSPECIFIED)
                .findFirst()
                .orElse(preferred);
    }

    private boolean related(
            DocumentEvidenceNode left,
            DocumentEvidenceNode right
    ) {
        return sharesSourceSpan(left, right) || sharesTag(left, right);
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
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        return right.tags()
                .stream()
                .filter(value -> value != null && !value.isBlank())
                .map(String::toLowerCase)
                .anyMatch(leftTags::contains);
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