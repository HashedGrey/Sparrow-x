package com.sparrowx.agentic.planning;

import com.sparrowx.agentic.adapters.llm.LlmFallbackPolicy;
import com.sparrowx.agentic.adapters.llm.LlmFallbackPolicy.ModelRoute;
import com.sparrowx.agentic.adapters.llm.LlmFallbackPolicy.Selection;
import com.sparrowx.agentic.adapters.llm.StructuredLlmClient;
import com.sparrowx.agentic.adapters.llm.StructuredLlmResponse;
import com.sparrowx.agentic.components.IntentComponent;
import com.sparrowx.agentic.components.IntentComponent.IntentRequest;
import com.sparrowx.agentic.prompts.PromptPack;
import com.sparrowx.agentic.prompts.PromptPack.RenderedPrompt;
import com.sparrowx.agentic.prompts.StructuredOutputSchemas;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class MissionIntentClassifier
        implements IntentComponent.Interpreter {

    private final StructuredLlmClient llmClient;
    private final LlmFallbackPolicy fallbackPolicy;
    private final PromptPack promptPack;
    private final StructuredOutputSchemas outputSchemas;
    private final IntentDecoder decoder;
    private final Settings settings;

    public MissionIntentClassifier(
            StructuredLlmClient llmClient,
            LlmFallbackPolicy fallbackPolicy,
            PromptPack promptPack,
            StructuredOutputSchemas outputSchemas,
            IntentDecoder decoder,
            Settings settings) {

        this.llmClient = Objects.requireNonNull(
                llmClient,
                "llmClient must not be null");
        this.fallbackPolicy = Objects.requireNonNull(
                fallbackPolicy,
                "fallbackPolicy must not be null");
        this.promptPack = Objects.requireNonNull(
                promptPack,
                "promptPack must not be null");
        this.outputSchemas = Objects.requireNonNull(
                outputSchemas,
                "outputSchemas must not be null");
        this.decoder = Objects.requireNonNull(
                decoder,
                "decoder must not be null");
        this.settings = Objects.requireNonNull(
                settings,
                "settings must not be null");
    }

    @Override
    public MissionIntent interpret(IntentRequest request) {
        Objects.requireNonNull(request, "request must not be null");

        RenderedPrompt prompt = promptPack.render(
                settings.promptName(),
                promptVariables(request));

        ModelRoute route = fallbackPolicy.selectInitial(
                settings.routeSelection());

        StructuredLlmResponse response = llmClient.complete(
                route,
                new StructuredLlmClient.Request(
                        request.missionId(),
                        "classify-mission-intent",
                        request.missionId()
                                + ":intent:"
                                + prompt.version(),
                        prompt.systemPrompt(),
                        prompt.userPrompt(),
                        outputSchemas.schema(settings.schemaName()),
                        settings.maxOutputTokens(),
                        settings.temperature(),
                        prompt.metadata()));

        MissionIntent intent = Objects.requireNonNull(
                decoder.decode(request, response.parsedOutput()),
                "decoder returned null");

        if (!request.missionId().equals(intent.missionId())) {
            throw new IllegalArgumentException(
                    "decoded intent belongs to another mission");
        }

        return intent;
    }

    private static Map<String, Object> promptVariables(
            IntentRequest request) {

        Map<String, Object> variables = new LinkedHashMap<>();
        variables.put("missionId", request.missionId());
        variables.put("query", request.query());
        variables.put("artifacts", request.artifacts());
        variables.put("preferredPath", request.preferredPath().name());
        variables.put("allowedTools", request.allowedTools());
        variables.put(
                "allowedSourceServices",
                request.allowedSourceServices());
        variables.put(
                "requiredOutputSections",
                request.requiredOutputSections());
        variables.put(
                "requireCitations",
                request.requireCitations());
        variables.put(
                "requireHumanReview",
                request.requireHumanReview());
        variables.put(
                "allowExternalSources",
                request.allowExternalSources());
        variables.put("attributes", request.attributes());
        return Map.copyOf(variables);
    }

    @FunctionalInterface
    public interface IntentDecoder {
        MissionIntent decode(
                IntentRequest request,
                Map<String, Object> parsedOutput);
    }

    public record Settings(
            Selection routeSelection,
            String promptName,
            String schemaName,
            int maxOutputTokens,
            double temperature) {

        public Settings {
            routeSelection = Objects.requireNonNull(
                    routeSelection,
                    "routeSelection must not be null");
            promptName = requireText(
                    promptName,
                    "promptName");
            schemaName = requireText(
                    schemaName,
                    "schemaName");

            if (maxOutputTokens <= 0) {
                throw new IllegalArgumentException(
                        "maxOutputTokens must be positive");
            }

            if (!Double.isFinite(temperature)
                    || temperature < 0.0
                    || temperature > 2.0) {
                throw new IllegalArgumentException(
                        "temperature must be between 0.0 and 2.0");
            }
        }
    }

    private static String requireText(
            String value,
            String field) {

        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    field + " must not be blank");
        }

        return value;
    }
}