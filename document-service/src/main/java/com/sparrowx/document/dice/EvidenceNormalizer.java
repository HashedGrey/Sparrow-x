package com.sparrowx.document.dice;

import com.sparrowx.document.domain.models.DocumentEvidenceNode;
import com.sparrowx.document.domain.models.SourceSpan;
import com.sparrowx.document.domain.valueobjects.VerificationStatus;
import com.sparrowx.document.features.builddocumentevidence.BuildDocumentEvidenceCommand;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class EvidenceNormalizer {

    private final DocumentDiceProjectionPort projectionPort;

    public EvidenceNormalizer(DocumentDiceProjectionPort projectionPort) {
        this.projectionPort = projectionPort;
    }

    public NormalizationResult normalize(
            BuildDocumentEvidenceCommand command,
            List<SourceSpan> sourceSpans
    ) {
        List<String> warnings = new ArrayList<>();

        DocumentDiceProjectionPort.ProjectionResult projectionResult =
                projectionPort.project(command, sourceSpans);

        warnings.addAll(projectionResult.warnings());

        if (!projectionResult.nodes().isEmpty()) {
            return new NormalizationResult(projectionResult.nodes(), warnings);
        }

        List<DocumentEvidenceNode> fallbackNodes = sourceSpans.stream()
                .map(span -> toNode(command, span))
                .toList();

        warnings.add("Used fallback span-to-node normalization.");

        return new NormalizationResult(fallbackNodes, warnings);
    }

    private DocumentEvidenceNode toNode(
            BuildDocumentEvidenceCommand command,
            SourceSpan span
    ) {
        DocumentEvidenceNode.EvidenceNodeType nodeType = defaultNodeType(command);
        String excerpt = span.excerpt();

        return new DocumentEvidenceNode(
                UUID.randomUUID().toString(),
                nodeType,
                nodeType == DocumentEvidenceNode.EvidenceNodeType.CUSTOM ? "custom_evidence" : "",
                firstNonBlank(span.title(), span.fileName(), "Document evidence"),
                summarize(excerpt),
                excerpt,
                List.of(span.sourceSpanId()),
                VerificationStatus.UNVERIFIED,
                bounded(span.relevanceScore()),
                excerpt.isBlank() ? 0.0 : 1.0,
                excerpt.isBlank(),
                buildTags(command),
                List.of(),
                Map.of(
                        "document_id", span.documentId() == null ? "" : span.documentId().value(),
                        "chunk_id", span.chunkId() == null ? "" : span.chunkId().value(),
                        "citation", span.citation()
                )
        );
    }

    private DocumentEvidenceNode.EvidenceNodeType defaultNodeType(
            BuildDocumentEvidenceCommand command
    ) {
        return command.spec().requestedNodeTypes()
                .stream()
                .filter(type -> type != null && type != DocumentEvidenceNode.EvidenceNodeType.UNSPECIFIED)
                .findFirst()
                .orElse(DocumentEvidenceNode.EvidenceNodeType.CLAIM);
    }

    private List<String> buildTags(BuildDocumentEvidenceCommand command) {
        List<String> tags = new ArrayList<>();
        tags.addAll(command.buildContext().topics());
        tags.addAll(command.buildContext().keywords());

        return tags.stream()
                .filter(value -> value != null && !value.isBlank())
                .distinct()
                .toList();
    }

    private String summarize(String excerpt) {
        if (excerpt == null || excerpt.isBlank()) {
            return "";
        }

        String normalized = excerpt.replaceAll("\\s+", " ").trim();

        if (normalized.length() <= 280) {
            return normalized;
        }

        return normalized.substring(0, 280) + "...";
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

    private double bounded(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value) || value < 0.0) {
            return 0.0;
        }

        return Math.min(1.0, value);
    }

    public record NormalizationResult(
            List<DocumentEvidenceNode> nodes,
            List<String> warnings
    ) {
        public NormalizationResult {
            nodes = nodes == null ? List.of() : List.copyOf(nodes);
            warnings = warnings == null ? List.of() : List.copyOf(warnings);
        }
    }
}