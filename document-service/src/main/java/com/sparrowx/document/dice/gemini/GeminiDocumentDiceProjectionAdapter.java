package com.sparrowx.document.dice.gemini;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;
import com.google.genai.types.Schema;
import com.google.genai.types.Type;
import com.sparrowx.document.dice.DocumentDiceProjectionPort;
import com.sparrowx.document.domain.models.DocumentEvidenceNode;
import com.sparrowx.document.domain.models.SourceSpan;
import com.sparrowx.document.domain.valueobjects.VerificationStatus;
import com.sparrowx.document.features.builddocumentevidence.BuildDocumentEvidenceCommand;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Primary
@Component
public class GeminiDocumentDiceProjectionAdapter implements DocumentDiceProjectionPort {

    private final Client geminiClient;
    private final GeminiLlmProperties properties;
    private final ObjectMapper objectMapper;
    private final GeminiJsonExtractor jsonExtractor;
    private final GeminiProjectionPromptBuilder promptBuilder;
    private final GeminiProjectionResponseValidator responseValidator;

    public GeminiDocumentDiceProjectionAdapter(
            Client geminiClient,
            GeminiLlmProperties properties,
            ObjectMapper objectMapper,
            GeminiJsonExtractor jsonExtractor,
            GeminiProjectionPromptBuilder promptBuilder,
            GeminiProjectionResponseValidator responseValidator
    ) {
        this.geminiClient = geminiClient;
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.jsonExtractor = jsonExtractor;
        this.promptBuilder = promptBuilder;
        this.responseValidator = responseValidator;
    }

    @Override
    public ProjectionResult project(
            BuildDocumentEvidenceCommand command,
            List<SourceSpan> sourceSpans
    ) {
        if (sourceSpans == null || sourceSpans.isEmpty()) {
            return new ProjectionResult(
                    List.of(),
                    List.of("Gemini projection skipped because sourceSpans is empty.")
            );
        }

        List<String> warnings = new java.util.ArrayList<>();

        int attempts = Math.max(1, properties.projectionRetryCount() + 1);

        for (int attempt = 1; attempt <= attempts; attempt++) {
            try {
                ProjectionResult result = tryProject(command, sourceSpans, attempt > 1);
                warnings.addAll(result.warnings());

                if (!result.nodes().isEmpty() && !hasMissingRequestedNodeTypeWarning(result.warnings())) {
                    return new ProjectionResult(result.nodes(), warnings);
                }

            } catch (Exception exception) {
                warnings.add("Gemini projection attempt %d failed: %s"
                        .formatted(attempt, exception.getMessage()));
            }
        }

        return new ProjectionResult(List.of(), warnings);
    }

    private ProjectionResult tryProject(
            BuildDocumentEvidenceCommand command,
            List<SourceSpan> sourceSpans,
            boolean retry
    ) throws Exception {
        String prompt = promptBuilder.build(command, sourceSpans, retry);

        GenerateContentConfig config = GenerateContentConfig.builder()
                .responseMimeType("application/json")
                .responseSchema(responseSchema())
                .candidateCount(1)
                .maxOutputTokens(properties.maxOutputTokens())
                .temperature((float) properties.temperature())
                .topP((float) properties.topP())
                .systemInstruction(Content.fromParts(Part.fromText(systemInstruction())))
                .build();

        GenerateContentResponse response = geminiClient.models.generateContent(
                properties.model(),
                prompt,
                config
        );

        String text = response.text();

        if (text == null || text.isBlank()) {
            return new ProjectionResult(
                    List.of(),
                    List.of("Gemini returned blank projection response.")
            );
        }

        GeminiProjectionResponse parsed = parseResponse(text);

        List<DocumentEvidenceNode> nodes = parsed.nodes()
                .stream()
                .map(this::toDomainNode)
                .toList();

        GeminiProjectionResponseValidator.ValidationResult validation =
                responseValidator.validate(command, nodes, sourceSpans);

        List<String> warnings = new java.util.ArrayList<>();
        warnings.addAll(parsed.warnings());
        warnings.addAll(validation.warnings());

        return new ProjectionResult(validation.nodes(), warnings);
    }

    private GeminiProjectionResponse parseResponse(String text) throws Exception {
        String json = jsonExtractor.extractJsonObject(text);

        try {
            return objectMapper.readValue(json, GeminiProjectionResponse.class);
        } catch (Exception firstFailure) {
            if (!properties.jsonRepairEnabled()) {
                throw firstFailure;
            }

            String repaired = jsonExtractor.repairTruncatedJsonObject(text);
            return objectMapper.readValue(repaired, GeminiProjectionResponse.class);
        }
    }

    private DocumentEvidenceNode toDomainNode(GeminiEvidenceNode node) {
        DocumentEvidenceNode.EvidenceNodeType nodeType = toNodeType(node.nodeType());

        return new DocumentEvidenceNode(
                blankToGenerated(node.nodeId()),
                nodeType,
                nodeType == DocumentEvidenceNode.EvidenceNodeType.CUSTOM
                        ? nullToEmpty(node.customNodeType())
                        : "",
                nullToEmpty(node.title()),
                nullToEmpty(node.summary()),
                nullToEmpty(node.normalizedText()),
                node.sourceSpanIds(),
                VerificationStatus.UNVERIFIED,
                bounded(node.confidence()),
                bounded(node.coverageScore()),
                node.requiresSourceContext(),
                node.tags(),
                node.warnings(),
                node.attributes()
        );
    }

    private Schema responseSchema() {
        Schema stringSchema = Schema.builder()
                .type(Type.Known.STRING)
                .build();

        Schema numberSchema = Schema.builder()
                .type(Type.Known.NUMBER)
                .build();

        Schema booleanSchema = Schema.builder()
                .type(Type.Known.BOOLEAN)
                .build();

        Schema stringArraySchema = Schema.builder()
                .type(Type.Known.ARRAY)
                .items(stringSchema)
                .build();

        Schema attributesSchema = Schema.builder()
                .type(Type.Known.OBJECT)
                .build();

        Schema nodeSchema = Schema.builder()
                .type(Type.Known.OBJECT)
                .properties(Map.ofEntries(
                        Map.entry("nodeId", stringSchema),
                        Map.entry("nodeType", stringSchema),
                        Map.entry("customNodeType", stringSchema),
                        Map.entry("title", stringSchema),
                        Map.entry("summary", stringSchema),
                        Map.entry("normalizedText", stringSchema),
                        Map.entry("sourceSpanIds", stringArraySchema),
                        Map.entry("confidence", numberSchema),
                        Map.entry("coverageScore", numberSchema),
                        Map.entry("requiresSourceContext", booleanSchema),
                        Map.entry("tags", stringArraySchema),
                        Map.entry("warnings", stringArraySchema),
                        Map.entry("attributes", attributesSchema)
                ))
                .required(
                        "nodeType",
                        "title",
                        "summary",
                        "normalizedText",
                        "sourceSpanIds",
                        "confidence",
                        "coverageScore",
                        "requiresSourceContext",
                        "tags",
                        "warnings",
                        "attributes"
                )
                .build();

        Schema nodesArraySchema = Schema.builder()
                .type(Type.Known.ARRAY)
                .items(nodeSchema)
                .build();

        return Schema.builder()
                .type(Type.Known.OBJECT)
                .properties(Map.of(
                        "nodes", nodesArraySchema,
                        "warnings", stringArraySchema
                ))
                .required("nodes", "warnings")
                .build();
    }

    private String systemInstruction() {
        return """
                You are the Document DICE projection layer inside SparrowX Document Service.
                You only normalize retrieved document spans into compact typed evidence nodes.
                You must return valid JSON only.
                Every claim must be grounded in supplied source spans.
                Keep normalizedText short and source-near.
                Do not copy full excerpts.
                """;
    }

    private DocumentEvidenceNode.EvidenceNodeType toNodeType(String value) {
        if (value == null || value.isBlank()) {
            return DocumentEvidenceNode.EvidenceNodeType.CLAIM;
        }

        try {
            return DocumentEvidenceNode.EvidenceNodeType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ignored) {
            return DocumentEvidenceNode.EvidenceNodeType.CUSTOM;
        }
    }

    private String blankToGenerated(String value) {
        if (value == null || value.isBlank()) {
            return UUID.randomUUID().toString();
        }

        return value;
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private double bounded(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value) || value < 0.0) {
            return 0.0;
        }

        return Math.min(1.0, value);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record GeminiProjectionResponse(
            List<GeminiEvidenceNode> nodes,
            List<String> warnings
    ) {
        private GeminiProjectionResponse {
            nodes = nodes == null ? List.of() : List.copyOf(nodes);
            warnings = warnings == null ? List.of() : List.copyOf(warnings);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record GeminiEvidenceNode(
            String nodeId,
            String nodeType,
            String customNodeType,
            String title,
            String summary,
            String normalizedText,
            List<String> sourceSpanIds,
            double confidence,
            double coverageScore,
            boolean requiresSourceContext,
            List<String> tags,
            List<String> warnings,
            Map<String, String> attributes
    ) {
        private GeminiEvidenceNode {
            sourceSpanIds = sourceSpanIds == null ? List.of() : List.copyOf(sourceSpanIds);
            tags = tags == null ? List.of() : List.copyOf(tags);
            warnings = warnings == null ? List.of() : List.copyOf(warnings);
            attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
        }
    }

    private boolean hasMissingRequestedNodeTypeWarning(List<String> warnings) {
        if (warnings == null || warnings.isEmpty()) {
            return false;
        }

        return warnings.stream()
                .filter(value -> value != null)
                .anyMatch(value -> value.contains("missing requested node types"));
    }
}