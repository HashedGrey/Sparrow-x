package com.sparrowx.agentic.components;

import com.embabel.agent.api.common.OperationContext;
import com.sparrowx.agentic.planning.MissionIntent;
import com.sparrowx.agentic.planning.MissionPlan;
import com.sparrowx.agentic.planning.PlanValidator;
import com.sparrowx.agentic.planning.PlannedStep;
import com.sparrowx.agentic.planning.StepKind;
import com.sparrowx.agentic.prompts.PromptPack;
import com.sparrowx.agentic.prompts.StructuredOutputSchemas;
import com.sparrowx.agentic.validation.StructuredOutputValidator;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class PlanningComponent {

    private final PromptPack promptPack;
    private final StructuredOutputSchemas schemas;
    private final StructuredOutputValidator outputValidator;
    private final PlanValidator planValidator;

    public PlanningComponent(
            PromptPack promptPack,
            StructuredOutputSchemas schemas,
            StructuredOutputValidator outputValidator,
            PlanValidator planValidator
    ) {
        this.promptPack = Objects.requireNonNull(promptPack);
        this.schemas = Objects.requireNonNull(schemas);
        this.outputValidator =
                Objects.requireNonNull(outputValidator);
        this.planValidator =
                Objects.requireNonNull(planValidator);
    }

    public MissionPlan plan(
            PlanningRequest request,
            OperationContext context
    ) {
        Objects.requireNonNull(request, "request must not be null");
        Objects.requireNonNull(context, "context must not be null");

        Map<String, Object> variables = new LinkedHashMap<>();
        variables.put("missionId", request.missionId());
        variables.put("intent", request.intent());
        variables.put("currentPlan", request.currentPlan());
        variables.put("observations", request.observations());
        variables.put(
                "completedStepIds",
                request.completedStepIds()
        );
        variables.put("allowedTools", request.allowedTools());
        variables.put(
                "remainingToolCalls",
                request.remainingToolCalls()
        );
        variables.put(
                "remainingLlmCalls",
                request.remainingLlmCalls()
        );
        variables.put("attributes", request.attributes());
        variables.put(
                "outputSchema",
                schemas.schema(StructuredOutputSchemas.MISSION_PLAN)
        );

        PromptPack.RenderedPrompt rendered =
                promptPack.render(
                        PromptPack.PLANNING_PROMPT,
                        variables
                );

        String prompt =
                rendered.systemPrompt()
                        + "\n\n"
                        + rendered.userPrompt();

        PlanProjection output =
                context.ai()
                        .withDefaultLlm()
                        .createObject(
                                prompt,
                                PlanProjection.class
                        );

        validateProjection(output);

        List<PlannedStep> steps = output.steps().stream()
                .map(PlanningComponent::normalizeStep)
                .toList();

        MissionPlan plan = new MissionPlan(
                output.planId(),
                request.missionId(),
                output.revision(),
                request.intent(),
                steps,
                output.rationale(),
                Instant.now(),
                mapOf(output.attributes())
        );

        planValidator.validate(
                plan,
                request.intent(),
                request.allowedTools(),
                request.remainingToolCalls()
        );

        return plan;
    }

    private void validateProjection(PlanProjection output) {
        Objects.requireNonNull(
                output,
                "plan projection must not be null"
        );

        Map<String, Object> value = new LinkedHashMap<>();
        value.put("planId", output.planId());
        value.put("revision", output.revision());

        List<Map<String, Object>> steps =
                output.steps() == null
                        ? List.of()
                        : output.steps().stream()
                        .map(PlanningComponent::stepMap)
                        .toList();

        value.put("steps", steps);
        value.put("rationale", output.rationale());
        value.put("attributes", mapOf(output.attributes()));

        outputValidator.validatePlan(value);
    }

    private static Map<String, Object> stepMap(
            PlannedStep step
    ) {
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("stepId", step.stepId());
        value.put("kind", step.kind().name());
        value.put(
                "dependencyStepIds",
                List.copyOf(step.dependencyStepIds())
        );
        value.put("objective", step.objective());
        value.put("expectedOutput", step.expectedOutput());
        value.put(
                "requiresHumanApproval",
                step.requiresHumanApproval()
        );
        value.put("arguments", step.arguments());
        value.put("attributes", step.attributes());

        //return Map.copyOf(value);
        return value;
    }

    public record PlanProjection(
            String planId,
            int revision,
            List<PlannedStep> steps,
            String rationale,
            Map<String, Object> attributes
    ) {
        public PlanProjection {
            steps = steps == null
                    ? List.of()
                    : List.copyOf(steps);
            rationale = rationale == null ? "" : rationale;
            attributes = attributes == null
                    ? Map.of()
                    : Map.copyOf(attributes);
        }
    }

    public record PlanningRequest(
            String missionId,
            MissionIntent intent,
            MissionPlan currentPlan,
            List<Observation> observations,
            Set<String> completedStepIds,
            Set<String> allowedTools,
            int remainingToolCalls,
            int remainingLlmCalls,
            Map<String, Object> attributes
    ) {
        public PlanningRequest {
            missionId = requireText(missionId, "missionId");

            intent = Objects.requireNonNull(
                    intent,
                    "intent must not be null"
            );

            observations = observations == null
                    ? List.of()
                    : List.copyOf(observations);

            completedStepIds = completedStepIds == null
                    ? Set.of()
                    : Set.copyOf(completedStepIds);

            allowedTools = allowedTools == null
                    ? Set.of()
                    : Set.copyOf(allowedTools);

            if (remainingToolCalls < 0) {
                throw new IllegalArgumentException(
                        "remainingToolCalls must not be negative"
                );
            }

            if (remainingLlmCalls < 0) {
                throw new IllegalArgumentException(
                        "remainingLlmCalls must not be negative"
                );
            }

            attributes = attributes == null
                    ? Map.of()
                    : Map.copyOf(attributes);
        }
    }

    public record Observation(
            String stepId,
            StepKind stepKind,
            String summary,
            String checkpointReference,
            Map<String, Object> attributes
    ) {
        public Observation {
            stepId = requireText(stepId, "stepId");

            stepKind = Objects.requireNonNull(
                    stepKind,
                    "stepKind must not be null"
            );

            summary = summary == null ? "" : summary;
            checkpointReference =
                    checkpointReference == null
                            ? ""
                            : checkpointReference;

            attributes = attributes == null
                    ? Map.of()
                    : Map.copyOf(attributes);
        }
    }

    private static Map<String, Object> mapOf(
            Map<String, Object> value
    ) {
        return value == null ? Map.of() : Map.copyOf(value);
    }

    private static String requireText(
            String value,
            String field
    ) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    field + " must not be blank"
            );
        }

        return value;
    }

    private static PlannedStep normalizeStep(PlannedStep step) {
        Map<String, Object> arguments = switch (step.kind()) {
            case SEARCH_INTERNAL_ENTITIES -> normalizeInternalSearchArguments(step);
            default -> step.arguments();
        };

        return new PlannedStep(
                step.stepId(),
                step.kind(),
                step.dependencyStepIds(),
                step.objective(),
                step.expectedOutput(),
                step.requiresHumanApproval(),
                arguments,
                step.attributes()
        );
    }

    private static Map<String, Object> normalizeInternalSearchArguments(PlannedStep step) {
        Map<String, Object> source = step.arguments();
        Map<String, Object> target = new LinkedHashMap<>();

        Object query = source.get("query");
        target.put("query", query instanceof String text && !text.isBlank() ? text : step.objective());

        copyIfPresent(source, target, "allowedNodeTypes");
        copyIfPresent(source, target, "rootEntityId");
        copyIfPresent(source, target, "rootNodeType");
        copyIfPresent(source, target, "filters");

        target.put("depth", source.getOrDefault("depth", 0));
        target.put("limit", source.getOrDefault("limit", 20));
        target.put("includeFuzzyMatches", source.getOrDefault("includeFuzzyMatches", true));

        return Map.copyOf(target);
    }

    private static void copyIfPresent(Map<String, Object> source, Map<String, Object> target, String key) {
        if (source.get(key) != null) target.put(key, source.get(key));
    }
}