package com.sparrowx.document.evidencegraph.gemini;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparrowx.document.domain.models.DocumentEvidenceGraph;
import com.sparrowx.document.domain.models.SourceSpan;
import com.sparrowx.document.features.builddocumentevidence.BuildDocumentEvidenceCommand;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

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

        String retryInstruction = retryInstruction(retry);
        String contradictionInstruction = contradictionInstruction(command);

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
                - Every node must reference at least one supplied sourceSpanId.
                - Do not invent sourceSpanIds.
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
                - If requested node types include CLAIM and source assertions exist, produce a CLAIM node.
                - Do not turn the user's task wording into a supported claim unless it appears as a claim in the source spans.
                - Extract what the document says, not what the user hopes the document says.

                %s

                Goal: %s
                Custom goal: %s
                Options: %s
                Requested node types: %s
                Requested relation types: %s
                Topics: %s
                Entities: %s
                Keywords: %s

                Source spans:
                %s
                %s
                """.formatted(
                contradictionInstruction,
                command.spec().goal(),
                command.spec().customGoal(),
                command.spec().options(),
                command.spec().requestedNodeTypes(),
                command.spec().requestedRelationTypes(),
                command.buildContext().topics(),
                command.buildContext().entityNames(),
                command.buildContext().keywords(),
                objectMapper.writeValueAsString(promptSpans),
                retryInstruction
        );
    }

    private String contradictionInstruction(BuildDocumentEvidenceCommand command) {
        if (command == null || command.spec() == null) {
            return "";
        }

        if (command.spec().goal() != DocumentEvidenceGraph.EvidenceGoal.CONTRADICTION_DETECTION) {
            return "";
        }

        String targetClaim = targetClaim(command);

        return """
                CONTRADICTION_DETECTION rules:
                - The tested claim is: "%s"
                - Do not rewrite the tested claim into a true source claim.
                - Do not mark the tested claim as supported.
                - Extract only source-backed facts, metrics, methods, and claims from the source spans.
                - The tested claim will be inserted separately by the deterministic orchestrator.
                - Your job is to extract source evidence that can later support or contradict that tested claim.
                - If the source says activity explains 4.6%% / 3.2%% and mind-wandering explains 10.8%% / 17.7%%, extract those as METRIC nodes exactly.
                - If the source evidence conflicts with the tested claim, extract the conflicting source metrics without softening them.
                """.formatted(targetClaim);
    }

    private String targetClaim(BuildDocumentEvidenceCommand command) {
        if (command == null || command.spec() == null || command.buildContext() == null) {
            return "";
        }

        Map<String, String> options = command.spec().options();

        String explicit = firstNonBlank(
                option(options, "target_claim"),
                option(options, "tested_claim"),
                option(options, "claim"),
                option(options, "proposition")
        );

        if (!explicit.isBlank()) {
            return explicit;
        }

        String retrievalHint = command.buildContext().retrievalHint();
        String fromClaimMarker = extractAfterMarker(retrievalHint, "claim:");

        if (!fromClaimMarker.isBlank()) {
            return fromClaimMarker;
        }

        String focus = option(options, "focus");
        String fromFocus = extractAfterMarker(focus, "claim that");

        if (!fromFocus.isBlank()) {
            return fromFocus;
        }

        String debug = command.buildContext().debugTaskInstruction();
        String fromDebug = extractAfterMarker(debug, "claim that");

        if (!fromDebug.isBlank()) {
            return fromDebug;
        }

        return retrievalHint == null ? "" : retrievalHint;
    }

    private String retryInstruction(boolean retry) {
        if (!retry) {
            return "";
        }

        return """

        Previous response was incomplete or invalid.
        Retry with STRICT valid JSON only.
        Do not include markdown.
        Do not leave strings unterminated.
        Keep every string short.

        Critical repair instruction:
        - You omitted at least one requested node type.
        - Produce at least one node for every requested node type when source evidence supports it.
        - CLAIM nodes are not metrics. A CLAIM is the source-backed assertion that the metrics support.
        - METRIC nodes should contain numbers, coefficients, percentages, p-values, R2, or estimates.
        - FRAMEWORK nodes should contain methods, procedures, models, or analysis approaches.
        - Do not invent missing node types if the source spans do not support them.
        """;
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

    private String option(Map<String, String> options, String key) {
        if (options == null || key == null) {
            return "";
        }

        return options.getOrDefault(key, "");
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