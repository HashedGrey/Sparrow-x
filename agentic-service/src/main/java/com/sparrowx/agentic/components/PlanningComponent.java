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
import java.util.*;

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

        List<PlannedStep> steps = normalizeSteps(
                output.steps(),
                request.remainingToolCalls(),
                request.intent()
        );
        System.out.println(">>> NORMALIZED PLAN");

        for (PlannedStep step : steps) {
            System.out.printf(
                    ">>> STEP id=[%s] kind=[%s] deps=%s args=%s%n",
                    step.stepId(),
                    step.kind(),
                    step.dependencyStepIds(),
                    step.arguments()
            );
        }
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

    private static boolean allowedByIntent(
            PlannedStep step,
            MissionIntent intent
    ) {
        return switch (step.kind()) {
            case SEARCH_INTERNAL_ENTITIES,
                 READ_INTERNAL_COMPANY_GRAPH,
                 READ_LEARNING_GRAPH ->
                    intent.requiresInternalContext();

            case BUILD_DOCUMENT_EVIDENCE,
                 SEARCH_DOCUMENT_SPANS,
                 VERIFY_DOCUMENT_EVIDENCE ->
                    intent.requiresDocumentEvidence();

            default -> true;
        };
    }

    private static List<PlannedStep> normalizeSteps(
            List<PlannedStep> rawSteps,
            int remainingToolCalls,
            MissionIntent intent
    ) {
        List<PlannedStep> normalized = rawSteps == null
                ? List.of()
                : rawSteps.stream()
                .map(step -> normalizeStep(step, intent))
                .filter(step -> allowedByIntent(step, intent))
                .toList();

        Set<String> retainedStepIds = normalized.stream()
                .map(PlannedStep::stepId)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));

        List<PlannedStep> base = normalized.stream()
                .map(step -> removeMissingDependencies(step, retainedStepIds))
                .toList();

        Map<String, PlannedStep> stepsById = new LinkedHashMap<>();
        for (PlannedStep step : base) {
            stepsById.put(step.stepId(), step);
        }

        Set<String> usedStepIds = new LinkedHashSet<>(stepsById.keySet());

        List<PlannedStep> result = new ArrayList<>();

        for (PlannedStep step : base) {
            if (!isGraphRead(step)
                    || hasGraphRoot(step.arguments())
                    || hasDirectSearchDependency(step, stepsById)) {

                result.add(step);
                continue;
            }

            String searchStepId =
                    uniqueSearchStepId(step.stepId(), usedStepIds);

            Map<String, Object> searchArguments =
                    buildSyntheticSearchArguments(step, intent);

            PlannedStep searchStep = new PlannedStep(
                    searchStepId,
                    StepKind.SEARCH_INTERNAL_ENTITIES,
                    step.dependencyStepIds(),
                    "Resolve the internal root entity required for: "
                            + step.objective(),
                    "Resolved internal entity id and node type",
                    false,
                    searchArguments,
                    Map.of(
                            "syntheticPrerequisite", true,
                            "forStepId", step.stepId()
                    )
            );

            Set<String> graphDependencies =
                    new LinkedHashSet<>(step.dependencyStepIds());

            graphDependencies.add(searchStepId);

            PlannedStep graphStep = new PlannedStep(
                    step.stepId(),
                    step.kind(),
                    Set.copyOf(graphDependencies),
                    step.objective(),
                    step.expectedOutput(),
                    step.requiresHumanApproval(),
                    step.arguments(),
                    step.attributes()
            );

            result.add(searchStep);
            result.add(graphStep);

            stepsById.put(searchStepId, searchStep);
            usedStepIds.add(searchStepId);
        }

        if (result.size() > remainingToolCalls) {
            throw new IllegalArgumentException(
                    "normalized plan exceeds the remaining tool-call budget"
            );
        }

        return List.copyOf(result);
    }

    private static PlannedStep removeMissingDependencies(
            PlannedStep step,
            Set<String> retainedStepIds
    ) {
        Set<String> dependencies = step.dependencyStepIds().stream()
                .filter(retainedStepIds::contains)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));

        if (dependencies.equals(step.dependencyStepIds())) {
            return step;
        }

        return new PlannedStep(
                step.stepId(),
                step.kind(),
                Set.copyOf(dependencies),
                step.objective(),
                step.expectedOutput(),
                step.requiresHumanApproval(),
                step.arguments(),
                step.attributes()
        );
    }

    private static PlannedStep normalizeStep(
            PlannedStep step,
            MissionIntent intent) {

        Map<String, Object> arguments = switch (step.kind()) {
            case SEARCH_INTERNAL_ENTITIES -> normalizeInternalSearchArguments(step, intent);
            case BUILD_DOCUMENT_EVIDENCE -> normalizeDocumentEvidenceArguments(step);
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

    private static Map<String, Object> normalizeDocumentEvidenceArguments(PlannedStep step) {
        Map<String, Object> source = step.arguments() == null ? Map.of() : step.arguments();
        Map<String, Object> target = new LinkedHashMap<>(source);

        if (!target.containsKey("topics")) {
            Object queries = source.get("queries");

            if (queries instanceof List<?> values) {
                List<String> topics = values.stream()
                        .filter(Objects::nonNull)
                        .map(String::valueOf)
                        .map(String::trim)
                        .filter(value -> !value.isBlank())
                        .toList();

                if (!topics.isEmpty()) {
                    target.put("topics", topics);
                }
            }
        }

        if (!target.containsKey("retrievalHint")) {
            Object query = source.get("query");

            if (query instanceof String text && !text.isBlank()) {
                target.put("retrievalHint", text.trim());
            } else {
                target.put("retrievalHint", step.objective());
            }
        }

        Map<String, Object> scope = new LinkedHashMap<>();

        Object existingScope = source.get("scope");
        if (existingScope instanceof Map<?, ?> existing) {
            existing.forEach((key, value) -> {
                if (key != null && value != null) {
                    scope.put(String.valueOf(key), value);
                }
            });
        }

        Object documentName = source.get("documentName");
        if (documentName instanceof String name && !name.isBlank()) {
            scope.put("fileNames", List.of(name.trim()));
        }

        Object fileName = source.get("fileName");
        if (fileName instanceof String name && !name.isBlank()) {
            scope.put("fileNames", List.of(name.trim()));
        }

        if (!scope.isEmpty()) {
            target.put("scope", Map.copyOf(scope));
        }

        target.remove("query");
        target.remove("queries");
        target.remove("documentName");
        target.remove("fileName");

        return Map.copyOf(target);
    }

    private static boolean isGraphRead(
            PlannedStep step
    ) {
        return step.kind() == StepKind.READ_INTERNAL_COMPANY_GRAPH
                || step.kind() == StepKind.READ_LEARNING_GRAPH;
    }

    private static boolean hasGraphRoot(
            Map<String, Object> arguments
    ) {
        if (arguments == null || arguments.isEmpty()) {
            return false;
        }

        for (String key : List.of(
                "rootEntityId",
                "root_entity_id",
                "entityId",
                "entity_id"
        )) {
            Object value = arguments.get(key);

            if (value instanceof String text
                    && !text.isBlank()
                    && !isPlannerReference(text)) {
                return true;
            }
        }

        Object entityIds = arguments.get("entity_ids");

        return entityIds instanceof List<?> values
                && !values.isEmpty()
                && values.getFirst() instanceof String text
                && !text.isBlank()
                && !isPlannerReference(text);
    }

    private static boolean isPlannerReference(String value) {
        String text = value.trim();

        return text.contains("${")
                || text.contains("{{")
                || text.contains("}}")
                || text.startsWith("steps.")
                || text.contains(".output.");
    }

    private static boolean hasDirectSearchDependency(
            PlannedStep step,
            Map<String, PlannedStep> stepsById
    ) {
        for (String dependencyId : step.dependencyStepIds()) {
            PlannedStep dependency = stepsById.get(dependencyId);

            if (dependency != null
                    && dependency.kind()
                    == StepKind.SEARCH_INTERNAL_ENTITIES) {
                return true;
            }
        }

        return false;
    }

    private static Map<String, Object> buildSyntheticSearchArguments(
            PlannedStep graphStep,
            MissionIntent intent) {

        Map<String, Object> target = new LinkedHashMap<>();

        target.put("query", internalSearchQuery(Map.of(), graphStep, intent));
        target.put("depth", 0);
        target.put("limit", 20);
        target.put("includeFuzzyMatches", true);

        Map<String, Object> graphArguments = graphStep.arguments() == null ? Map.of() : graphStep.arguments();

        Object rootNodeType = graphArguments.get("rootNodeType");

        if (rootNodeType == null) {
            rootNodeType = graphArguments.get("root_node_type");
        }

        if (rootNodeType != null) {
            target.put("allowedNodeTypes", List.of(rootNodeType));
        }

        return Map.copyOf(target);
    }

    private static String uniqueSearchStepId(
            String graphStepId,
            Set<String> usedStepIds
    ) {
        String base = graphStepId + ":resolve-root";

        if (!usedStepIds.contains(base)) {
            return base;
        }

        int suffix = 2;

        while (usedStepIds.contains(base + ":" + suffix)) {
            suffix++;
        }

        return base + ":" + suffix;
    }

    private static Map<String, Object> normalizeInternalSearchArguments(
            PlannedStep step,
            MissionIntent intent) {

        Map<String, Object> source = step.arguments() == null ? Map.of() : step.arguments();
        Map<String, Object> target = new LinkedHashMap<>();

        target.put("query", internalSearchQuery(source, step, intent));

        copyIfPresent(source, target, "allowedNodeTypes");
        copyIfPresent(source, target, "rootEntityId");
        copyIfPresent(source, target, "rootNodeType");
        copyIfPresent(source, target, "filters");

        target.put("depth", source.getOrDefault("depth", 0));
        target.put("limit", source.getOrDefault("limit", 20));
        target.put("includeFuzzyMatches", source.getOrDefault("includeFuzzyMatches", true));

        return Map.copyOf(target);
    }

    private static String internalSearchQuery(
            Map<String, Object> source,
            PlannedStep step,
            MissionIntent intent) {

        List<String> targets = intent.targetEntities().stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .sorted()
                .toList();

        if (targets.size() == 1) {
            return targets.getFirst();
        }

        Object query = source.get("query");

        if (query instanceof String text && !text.isBlank()) {
            return text.trim();
        }

        return step.objective();
    }

    private static void copyIfPresent(
            Map<String, Object> source,
            Map<String, Object> target,
            String key
    ) {
        if (source.get(key) != null) {
            target.put(key, source.get(key));
        }
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
}