package com.sparrowx.document.dice.gemini;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparrowx.document.domain.models.SourceSpan;
import com.sparrowx.document.features.builddocumentevidence.BuildDocumentEvidenceCommand;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GeminiProjectionPromptBuilder {

    private final ObjectMapper objectMapper;
    private final GeminiLlmProperties properties;

    public GeminiProjectionPromptBuilder(
            ObjectMapper objectMapper,
            GeminiLlmProperties properties
    ) {
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    public String build(
            BuildDocumentEvidenceCommand command,
            List<SourceSpan> sourceSpans,
            boolean retry
    ) throws Exception {
        List<PromptSourceSpan> promptSpans = toPromptSpans(sourceSpans);

        String retryInstruction = retry
                ? """

        Previous response was incomplete or invalid.
        Retry with STRICT valid JSON only.
        Do not include markdown.
        Do not leave strings unterminated.
        Keep every string short.

        Critical repair instruction:
        - You omitted at least one requested node type.
        - Produce at least one node for every requested node type when source evidence supports it.
        - If requested node types include CLAIM, extract CLAIM nodes from explicit source assertions such as:
          "we stated that...", "evidence for this statement...", "suggesting that...", "explained only...", "had only a modest impact...", "independent influences..."
        - CLAIM nodes are not metrics. A CLAIM is the source-backed assertion that the metrics support.
        - METRIC nodes should contain numbers, coefficients, percentages, p-values, R2, or estimates.
        - FRAMEWORK nodes should contain methods, procedures, models, or analysis approaches.
        """
                : "";

        return """
                Build compact typed document evidence nodes from the supplied source spans.

                Return JSON only with this exact shape:
                {
                  "nodes": [
                    {
                      "nodeId": "",
                      "nodeType": "CLAIM|METRIC|FRAMEWORK|ENTITY|OBLIGATION|CUSTOM",
                      "customNodeType": "",
                      "title": "",
                      "summary": "",
                      "normalizedText": "",
                      "sourceSpanIds": [],
                      "confidence": 0.0,
                      "coverageScore": 0.0,
                      "requiresSourceContext": false,
                      "tags": [],
                      "warnings": [],
                      "attributes": {}
                    }
                  ],
                  "warnings": []
                }

                Hard rules:
                - JSON only.
                - No markdown fences.
                - Use only supplied sourceSpanId values.
                - Every node must reference at least one sourceSpanId.
                - normalizedText must be short: maximum 220 characters.
                - summary must be short: maximum 180 characters.
                - title must be short: maximum 90 characters.
                - Do not copy full excerpts into normalizedText.
                - Prefer exact source wording over polished paraphrase.
                - Create METRIC nodes for coefficients, percentages, p-values, R2, model estimates, counts, rates, or numeric findings.
                - Create FRAMEWORK nodes for methods, analyses, procedures, models, or conceptual frameworks.
                - Create CLAIM nodes for source-backed assertions.
                - If requested node types are supplied, stay within them when possible.
                - If support is weak, include a warning instead of inventing.
                - Do not create more nodes than source spans.
                - You must produce at least one node for each requested node type when source evidence supports it.
                - If requested node types include METRIC and numeric evidence exists, produce a METRIC node.
                - If requested node types include FRAMEWORK and method, procedure, analysis, or model evidence exists, produce a FRAMEWORK node.

                Goal: %s
                Custom goal: %s
                Requested node types: %s
                Requested relation types: %s
                Topics: %s
                Entities: %s
                Keywords: %s

                Source spans:
                %s
                %s
                """.formatted(
                command.spec().goal(),
                command.spec().customGoal(),
                command.spec().requestedNodeTypes(),
                command.spec().requestedRelationTypes(),
                command.buildContext().topics(),
                command.buildContext().entityNames(),
                command.buildContext().keywords(),
                objectMapper.writeValueAsString(promptSpans),
                retryInstruction
        );
    }

    private List<PromptSourceSpan> toPromptSpans(List<SourceSpan> sourceSpans) {
        if (sourceSpans == null || sourceSpans.isEmpty()) {
            return List.of();
        }

        return sourceSpans.stream()
                .filter(span -> span != null)
                .filter(span -> span.excerpt() != null && !span.excerpt().isBlank())
                .limit(properties.maxProjectionSpans())
                .map(span -> new PromptSourceSpan(
                        span.sourceSpanId(),
                        span.documentId() == null ? "" : span.documentId().value(),
                        span.chunkId() == null ? "" : span.chunkId().value(),
                        span.title(),
                        span.fileName(),
                        span.pageStart(),
                        span.pageEnd(),
                        span.citation(),
                        truncate(span.excerpt(), properties.maxExcerptChars())
                ))
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

        return normalized.substring(0, maxChars) + "...";
    }

    private record PromptSourceSpan(
            String sourceSpanId,
            String documentId,
            String chunkId,
            String title,
            String fileName,
            int pageStart,
            int pageEnd,
            String citation,
            String excerpt
    ) {
    }
}