package com.sparrowx.agentic.planning;

import com.sparrowx.agentic.adapters.llm.LlmFallbackPolicy;
import com.sparrowx.agentic.adapters.llm.LlmFallbackPolicy.ModelRoute;
import com.sparrowx.agentic.adapters.llm.LlmFallbackPolicy.Selection;
import com.sparrowx.agentic.adapters.llm.StructuredLlmClient;
import com.sparrowx.agentic.adapters.llm.StructuredLlmResponse;
import com.sparrowx.agentic.components.PlanningComponent;
import com.sparrowx.agentic.components.PlanningComponent.PlanningRequest;
import com.sparrowx.agentic.prompts.PromptPack;
import com.sparrowx.agentic.prompts.PromptPack.RenderedPrompt;
import com.sparrowx.agentic.prompts.StructuredOutputSchemas;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class MissionPlanner
        implements PlanningComponent.Planner {

    private final StructuredLlmClient llmClient;
    private final LlmFallbackPolicy fallbackPolicy;
    private final PromptPack promptPack;
    private final StructuredOutputSchemas outputSchemas;
    private final PlanDecoder decoder;
    private final Settings settings;

    public MissionPlanner(
            StructuredLlmClient llmClient,
            LlmFallbackPolicy fallbackPolicy,
            PromptPack promptPack,
            StructuredOutputSchemas outputSchemas,
            PlanDecoder decoder,
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
    public MissionPlan plan(PlanningRequest request) {
        Objects.requireNonNull(request, "request must not be null");

        int revision = request.currentPlan() == null
                ? 1
                : Math.addExact(
                request.currentPlan().revision(),
                1);

        RenderedPrompt prompt = promptPack.render(
                settings.promptName(),
                promptVariables(request, revision));

        ModelRoute route = fallbackPolicy.selectInitial(
                settings.routeSelection());

        StructuredLlmResponse response = llmClient.complete(
                route,
                new StructuredLlmClient.Request(
                        request.missionId(),
                        "plan-mission-turn",
                        request.missionId()
                                + ":plan:"
                                + revision
                                + ":"
                                + prompt.version(),
                        prompt.systemPrompt(),
                        prompt.userPrompt(),
                        outputSchemas.schema(settings.schemaName()),
                        settings.maxOutputTokens(),
                        settings.temperature(),
                        prompt.metadata()));

        MissionPlan plan = Objects.requireNonNull(
                decoder.decode(
                        request,
                        revision,
                        response.parsedOutput()),
                "decoder returned null");

        if (!request.missionId().equals(plan.missionId())) {
            throw new IllegalArgumentException(
                    "decoded plan belongs to another mission");
        }

        if (plan.revision() != revision) {
            throw new IllegalArgumentException(
                    "decoded plan revision does not match request");
        }

        return plan;
    }

    private static Map<String, Object> promptVariables(
            PlanningRequest request,
            int revision) {

        Map<String, Object> variables = new LinkedHashMap<>();
        variables.put("missionId", request.missionId());
        variables.put("revision", revision);
        variables.put("intent", request.intent());
        variables.put("observations", request.observations());
        variables.put(
                "completedStepIds",
                request.completedStepIds());
        variables.put("allowedTools", request.allowedTools());
        variables.put(
                "remainingToolCalls",
                request.remainingToolCalls());
        variables.put(
                "remainingLlmCalls",
                request.remainingLlmCalls());
        variables.put("attributes", request.attributes());

        if (request.currentPlan() != null) {
            variables.put("currentPlan", request.currentPlan());
        }

        return Map.copyOf(variables);
    }

    @FunctionalInterface
    public interface PlanDecoder {
        MissionPlan decode(
                PlanningRequest request,
                int revision,
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