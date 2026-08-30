package com.sparrowx.agentic.agents;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparrowx.agentic.actions.document.BuildDocumentEvidenceAction;
import com.sparrowx.agentic.components.PlanningComponent.Observation;
import com.sparrowx.agentic.mission.evidence.EvidenceRef;
import com.sparrowx.agentic.mission.evidence.EvidenceRegistry;
import com.sparrowx.agentic.planning.MissionIntent;
import com.sparrowx.agentic.planning.MissionPlan;
import com.sparrowx.agentic.planning.PlannedStep;
import com.sparrowx.agentic.planning.StepKind;
import com.sparrowx.agentic.steps.BuildDocumentEvidenceStep;
import com.sparrowx.agentic.steps.ResolveInternalContextStep;
import com.sparrowx.agentic.tools.document.DocumentEvidenceRequestBuilder.BuildSpec;
import org.springframework.stereotype.Component;
import com.sparrowx.agentic.tools.document.DocumentSpanSearchRequestBuilder.Scope;

import com.sparrowx.document.proto.EvidenceGoalProto;
import com.sparrowx.document.proto.RetrievalModeProto;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Executes the enterprise capability portion of an Embabel action.
 *
 * The MissionPlan argument maps are converted to the existing typed request
 * records. This keeps all gRPC, validation, policy and resilience code behind
 * the existing steps while removing Temporal from agentic step selection.
 */
@Component
public final class DefaultMissionEvidenceService
        implements MissionEvidenceService {

    private final BuildDocumentEvidenceStep documentStep;
    private final ResolveInternalContextStep internalStep;
    private final ObjectMapper objectMapper;

    public DefaultMissionEvidenceService(
            BuildDocumentEvidenceStep documentStep,
            ResolveInternalContextStep internalStep,
            ObjectMapper objectMapper
    ) {
        this.documentStep = Objects.requireNonNull(
                documentStep,
                "documentStep must not be null"
        );
        this.internalStep = Objects.requireNonNull(
                internalStep,
                "internalStep must not be null"
        );
        this.objectMapper = Objects.requireNonNull(
                objectMapper,
                "objectMapper must not be null"
        );
    }

    @Override
    public MissionEvidence collect(
            MissionRunInput input,
            MissionIntent intent,
            MissionPlan plan
    ) {
        Objects.requireNonNull(input, "input must not be null");
        Objects.requireNonNull(intent, "intent must not be null");
        Objects.requireNonNull(plan, "plan must not be null");

        if (!input.missionId().equals(intent.missionId())
                || !input.missionId().equals(plan.missionId())) {
            throw new IllegalArgumentException(
                    "input, intent and plan must belong to one mission"
            );
        }

        List<Observation> observations = new ArrayList<>();
        List<EvidenceRef> collectedEvidence = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        Set<String> completed = new LinkedHashSet<>();
        Map<String, StepResult> completedResults = new LinkedHashMap<>();
        List<PlannedStep> pending = new ArrayList<>(plan.steps());

        while (!pending.isEmpty()) {
            PlannedStep step = pending.stream()
                    .filter(candidate ->
                            candidate.dependenciesSatisfied(completed))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException(
                            "no executable planned step; dependency cycle "
                                    + "or missing dependency"
                    ));

            PlannedStep executableStep =
                    hydrateDependencyArguments(
                            step,
                            completedResults
                    );

            StepResult result = executeStep(
                    input,
                    executableStep
            );

            observations.add(new Observation(
                    step.stepId(),
                    step.kind(),
                    result.summary(),
                    "",
                    result.attributes()
            ));

            collectedEvidence.addAll(result.evidenceRefs());
            warnings.addAll(result.warnings());
            completedResults.put(step.stepId(), result);

            completed.add(step.stepId());
            pending.remove(step);
        }

        EvidenceRegistry registry = new EvidenceRegistry();
        collectedEvidence.forEach(registry::register);

        return new MissionEvidence(
                observations,
                registry.snapshot(),
                warnings,
                Map.of()
        );
    }

    private StepResult executeStep(
            MissionRunInput input,
            PlannedStep step
    ) {
        if (step.requiresHumanApproval()
                && input.approvedGateIds().isEmpty()) {
            throw new IllegalStateException(
                    "planned step requires an approved enterprise gate: "
                            + step.stepId()
            );
        }

        return switch (step.kind()) {
            case BUILD_DOCUMENT_EVIDENCE ->
                    executeDocument(input, step);
            case SEARCH_INTERNAL_ENTITIES,
                 READ_INTERNAL_COMPANY_GRAPH,
                 READ_LEARNING_GRAPH -> executeInternal(input, step);
            case PREPARE_INPUT_ARTIFACTS,
                 UPLOAD_DOCUMENT,
                 GET_INGESTION_JOB -> deferred(
                    step,
                    "Input artifact preparation completed before agent start"
            );
            case REQUEST_HUMAN_APPROVAL -> deferred(
                    step,
                    "Enterprise approval was resolved before agent execution"
            );
            case APPLY_REDACTION,
                 CHECK_GROUNDING,
                 BUILD_CITATIONS,
                 COMPOSE_ANSWER,
                 SEARCH_DOCUMENT_SPANS,
                 VERIFY_DOCUMENT_EVIDENCE -> deferred(
                    step,
                    "Handled by the terminal synthesis/governance boundary"
            );
        };
    }

    private static PlannedStep hydrateDependencyArguments(
            PlannedStep step,
            Map<String, StepResult> completedResults
    ) {
        if (step.kind() != StepKind.READ_INTERNAL_COMPANY_GRAPH
                && step.kind() != StepKind.READ_LEARNING_GRAPH) {
            return step;
        }

        Map<String, Object> arguments =
                new LinkedHashMap<>(
                        step.arguments() == null
                                ? Map.of()
                                : step.arguments()
                );

        if (hasNonBlankString(
                arguments,
                "rootEntityId",
                "root_entity_id",
                "entityId",
                "entity_id"
        )) {
            return step;
        }

        for (String dependencyStepId
                : step.dependencyStepIds()) {

            StepResult dependencyResult =
                    completedResults.get(dependencyStepId);

            if (dependencyResult == null) {
                continue;
            }

            Object resolvedEntityId =
                    dependencyResult.attributes()
                            .get("resolvedEntityId");

            if (!(resolvedEntityId instanceof String entityId)
                    || entityId.isBlank()) {
                continue;
            }

            arguments.put(
                    "rootEntityId",
                    entityId
            );

            Object resolvedNodeType =
                    dependencyResult.attributes()
                            .get("resolvedNodeType");

            if (resolvedNodeType != null
                    && !hasNonBlankString(
                    arguments,
                    "rootNodeType",
                    "root_node_type"
            )) {
                arguments.put(
                        "rootNodeType",
                        resolvedNodeType
                );
            }

            return new PlannedStep(
                    step.stepId(),
                    step.kind(),
                    step.dependencyStepIds(),
                    step.objective(),
                    step.expectedOutput(),
                    step.requiresHumanApproval(),
                    Map.copyOf(arguments),
                    step.attributes()
            );
        }

        return step;
    }

    private StepResult executeDocument(
            MissionRunInput input,
            PlannedStep step
    ) {
        BuildSpec spec = buildDocumentSpec(input, step);

        BuildDocumentEvidenceAction.Result result =
                documentStep.execute(input.request().context(), spec);

        return new StepResult(
                "Built document evidence; coverage="
                        + result.coverageScore(),
                result.evidenceRefs(),
                result.warnings(),
                Map.of(
                        "coverageScore", result.coverageScore(),
                        "usedChunkRetrieval",
                        result.usedChunkRetrieval(),
                        "usedClaimCache",
                        result.usedClaimCache()
                )
        );
    }

    private BuildSpec buildDocumentSpec(
            MissionRunInput input,
            PlannedStep step
    ) {
        Map<String, Object> arguments =
                step.arguments() == null
                        ? Map.of()
                        : step.arguments();

        EvidenceGoalProto goal =
                enumArgument(
                        arguments,
                        EvidenceGoalProto.class,
                        "goal",
                        "evidenceGoal",
                        "evidence_goal"
                );

        String customGoal =
                stringArgument(
                        arguments,
                        "customGoal",
                        "custom_goal"
                );

        if (goal == null
                || goal == EvidenceGoalProto.EVIDENCE_GOAL_UNSPECIFIED
                || goal == EvidenceGoalProto.UNRECOGNIZED) {

            goal = EvidenceGoalProto.EVIDENCE_GOAL_CUSTOM;
            customGoal = step.objective();
        }

        if (goal == EvidenceGoalProto.EVIDENCE_GOAL_CUSTOM
                && (customGoal == null || customGoal.isBlank())) {
            customGoal = step.objective();
        }
        Map<String, Object> spec = new LinkedHashMap<>();

        spec.put(
                "requestId",
                effectId(input, step)
        );

        copyFirstPresent(arguments, spec, "goal", "goal", "evidenceGoal", "evidence_goal");
        copyFirstPresent(arguments, spec, "customGoal", "customGoal", "custom_goal");

        copyFirstPresent(
                arguments,
                spec,
                "requestedNodeTypes",
                "requestedNodeTypes",
                "requested_node_types"
        );

        copyFirstPresent(
                arguments,
                spec,
                "requestedRelationTypes",
                "requestedRelationTypes",
                "requested_relation_types"
        );

        copyFirstPresent(
                arguments,
                spec,
                "outputSchemaRef",
                "outputSchemaRef",
                "output_schema_ref"
        );

        copyFirstPresent(
                arguments,
                spec,
                "outputSchemaVersion",
                "outputSchemaVersion",
                "output_schema_version"
        );

        copyFirstPresent(
                arguments,
                spec,
                "options",
                "options"
        );

        copyFirstPresent(
                arguments,
                spec,
                "retrievalHint",
                "retrievalHint",
                "retrieval_hint"
        );

        copyFirstPresent(
                arguments,
                spec,
                "topics",
                "topics"
        );

        copyFirstPresent(
                arguments,
                spec,
                "entityNames",
                "entityNames",
                "entity_names"
        );

        copyFirstPresent(
                arguments,
                spec,
                "keywords",
                "keywords"
        );

        copyFirstPresent(
                arguments,
                spec,
                "metadataFilters",
                "metadataFilters",
                "metadata_filters"
        );

        copyFirstPresent(
                arguments,
                spec,
                "debugTaskInstruction",
                "debugTaskInstruction",
                "debug_task_instruction"
        );

        copyFirstPresent(
                arguments,
                spec,
                "retrievalMode",
                "retrievalMode",
                "retrieval_mode"
        );

        spec.put(
                "limit",
                arguments.getOrDefault("limit", 20)
        );

        spec.put(
                "includeExcerpts",
                firstPresent(
                        arguments,
                        true,
                        "includeExcerpts",
                        "include_excerpts"
                )
        );

        spec.put(
                "allowClaimCache",
                firstPresent(
                        arguments,
                        true,
                        "allowClaimCache",
                        "allow_claim_cache"
                )
        );

        spec.put(
                "requireVerification",
                firstPresent(
                        arguments,
                        false,
                        "requireVerification",
                        "require_verification"
                )
        );

        spec.put(
                "scope",
                buildDocumentScope(arguments)
        );

        return objectMapper.convertValue(
                spec,
                BuildSpec.class
        );
    }

    private static Scope buildDocumentScope(
            Map<String, Object> arguments
    ) {
        Object existingScope = arguments.get("scope");

        if (existingScope instanceof Scope scope) {
            return scope;
        }

        Map<String, Object> source =
                existingScope instanceof Map<?, ?> map
                        ? castStringObjectMap(map)
                        : arguments;

        return new Scope(
                stringList(
                        source,
                        "documentIds",
                        "document_ids"
                ),
                stringList(
                        source,
                        "fileNames",
                        "file_names"
                ),
                stringList(
                        source,
                        "collectionIds",
                        "collection_ids"
                ),
                stringList(
                        source,
                        "tags"
                ),
                stringMap(
                        source,
                        "metadataFilters",
                        "metadata_filters"
                )
        );
    }

    private StepResult executeInternal(
            MissionRunInput input,
            PlannedStep step
    ) {
        String operation = switch (step.kind()) {
            case SEARCH_INTERNAL_ENTITIES -> "SEARCH_ENTITIES";
            case READ_INTERNAL_COMPANY_GRAPH -> "READ_COMPANY_GRAPH";
            case READ_LEARNING_GRAPH -> "READ_LEARNING_GRAPH";
            default -> throw new IllegalArgumentException(
                    "not an internal step: " + step.kind()
            );
        };

        Map<String, Object> spec =
                step.kind() == StepKind.SEARCH_INTERNAL_ENTITIES
                        ? buildInternalSearchSpec(input, step)
                        : buildInternalGraphSpec(input, step);

        Map<String, Object> envelope = new LinkedHashMap<>();
        envelope.put("operation", operation);
        envelope.put(
                step.kind() == StepKind.SEARCH_INTERNAL_ENTITIES
                        ? "searchSpec"
                        : "graphSpec",
                spec
        );

        ResolveInternalContextStep.Request request =
                objectMapper.convertValue(
                        envelope,
                        ResolveInternalContextStep.Request.class
                );
        ResolveInternalContextStep.Result result =
                internalStep.execute(input.request().context(), request);

        return new StepResult(
                result.summary(),
                result.evidenceRefs(),
                result.warnings(),
                result.attributes()
        );
    }

    private static StepResult deferred(
            PlannedStep step,
            String summary
    ) {
        return new StepResult(
                summary,
                List.of(),
                List.of(),
                Map.of("capability", step.capability())
        );
    }

    private static Map<String, Object> withRequestId(
            Map<String, Object> source,
            String requestId
    ) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (source != null) {
            result.putAll(source);
        }
        result.putIfAbsent("requestId", requestId);
        return Map.copyOf(result);
    }

    private static String effectId(
            MissionRunInput input,
            PlannedStep step
    ) {
        return input.request().context().requestId()
                + ":embabel:"
                + step.stepId();
    }

    private record StepResult(
            String summary,
            List<EvidenceRef> evidenceRefs,
            List<String> warnings,
            Map<String, Object> attributes
    ) {
        private StepResult {
            summary = summary == null ? "" : summary;
            evidenceRefs = evidenceRefs == null
                    ? List.of()
                    : List.copyOf(evidenceRefs);
            warnings = warnings == null
                    ? List.of()
                    : List.copyOf(warnings);
            attributes = attributes == null
                    ? Map.of()
                    : Map.copyOf(attributes);
        }
    }

    private static Map<String, Object> buildInternalSearchSpec(
            MissionRunInput input,
            PlannedStep step
    ) {
        Map<String, Object> arguments =
                step.arguments() == null
                        ? Map.of()
                        : step.arguments();

        Map<String, Object> spec = new LinkedHashMap<>();

        spec.put(
                "requestId",
                effectId(input, step)
        );

        Object query = arguments.get("query");


        if (query instanceof String text && !text.isBlank()) {
            spec.put("query", text);
        } else {
            spec.put("query", step.objective());
        }

        copyIfPresent(arguments, spec, "allowedNodeTypes");
        copyIfPresent(arguments, spec, "rootEntityId");
        copyIfPresent(arguments, spec, "rootNodeType");
        copyIfPresent(arguments, spec, "filters");

        spec.put("depth", arguments.getOrDefault("depth", 0));
        spec.put("limit", arguments.getOrDefault("limit", 20));
        spec.put("includeFuzzyMatches", arguments.getOrDefault(
                        "includeFuzzyMatches",
                        true
                )
        );

        return spec;
    }

    private static Map<String, Object> buildInternalGraphSpec(
            MissionRunInput input,
            PlannedStep step
    ) {
        Map<String, Object> arguments =
                step.arguments() == null
                        ? Map.of()
                        : step.arguments();

        Map<String, Object> spec = new LinkedHashMap<>();

        spec.put("requestId", effectId(input, step)
        );

        copyFirstPresent(
                arguments,
                spec,
                "rootEntityId",
                "rootEntityId",
                "root_entity_id",
                "entityId",
                "entity_id"
        );



        if (!spec.containsKey("rootEntityId")) {
            Object entityIds = arguments.get("entity_ids");

            if (entityIds instanceof List<?> values
                    && !values.isEmpty()
                    && values.getFirst() instanceof String entityId
                    && !entityId.isBlank()) {
                spec.put("rootEntityId", entityId);
            }
        }

        copyFirstPresent(
                arguments, spec, "rootNodeType", "rootNodeType", "root_node_type"
        );

        Object rootEntityId = spec.get("rootEntityId");

        if (rootEntityId instanceof String text
                && text.isBlank()) {
            spec.remove("rootEntityId");
        }

        spec.put("depth", arguments.getOrDefault("depth", 1));
        spec.put("limit", arguments.getOrDefault("limit", 50));

        return Map.copyOf(spec);
    }

    private static void copyFirstPresent(
            Map<String, Object> source,
            Map<String, Object> target,
            String targetKey,
            String... sourceKeys
    ) {
        for (String sourceKey : sourceKeys) {Object value = source.get(sourceKey);
            if (value != null) {
                target.put(targetKey, value);
                return;
            }
        }
    }

    private static Object firstPresent(
            Map<String, Object> source,
            Object defaultValue,
            String... keys
    ) {
        for (String key : keys) {
            Object value = source.get(key);

            if (value != null) {
                return value;
            }
        }

        return defaultValue;
    }

    private static List<String> stringList(
            Map<String, Object> source,
            String... keys
    ) {
        Object value = firstPresent(
                source,
                List.of(),
                keys
        );

        if (!(value instanceof List<?> values)) {
            return List.of();
        }

        return values.stream()
                .filter(Objects::nonNull)
                .map(String::valueOf)
                .filter(text -> !text.isBlank())
                .toList();
    }

    private static Map<String, String> stringMap(
            Map<String, Object> source,
            String... keys
    ) {
        Object value = firstPresent(
                source,
                Map.of(),
                keys
        );

        if (!(value instanceof Map<?, ?> values)) {
            return Map.of();
        }

        Map<String, String> result = new LinkedHashMap<>();

        values.forEach((key, item) -> {
            if (key != null && item != null) {
                result.put(
                        String.valueOf(key),
                        String.valueOf(item)
                );
            }
        });

        return Map.copyOf(result);
    }

    private static boolean hasNonBlankString(Map<String, Object> source, String... keys
    ) {
        for (String key : keys) {
            Object value = source.get(key);

            if (value instanceof String text
                    && !text.isBlank()) {
                return true;
            }
        }

        return false;
    }

    private static Map<String, Object> castStringObjectMap(Map<?, ?> source) {
        Map<String, Object> result = new LinkedHashMap<>();

        source.forEach((key, value) -> {
            if (key != null) {
                result.put(
                        String.valueOf(key),
                        value
                );
            }
        });

        return result;
    }

    private static void copyIfPresent(
            Map<String, Object> source,
            Map<String, Object> target,
            String key
    ) {
        Object value = source.get(key);

        if (value != null) {
            target.put(key, value);
        }
    }

    private static String stringArgument(
            Map<String, Object> source,
            String... keys
    ) {
        Object value = firstPresent(
                source,
                null,
                keys
        );

        if (value == null) {
            return "";
        }

        if (value instanceof String text) {
            return text.trim();
        }

        return String.valueOf(value).trim();
    }

    private static <E extends Enum<E>> E enumArgument(
            Map<String, Object> source,
            Class<E> enumType,
            String... keys
    ) {
        Objects.requireNonNull(source, "source must not be null");
        Objects.requireNonNull(enumType, "enumType must not be null");

        Object value = firstPresent(
                source,
                null,
                keys
        );

        if (value == null) {
            return null;
        }

        if (enumType.isInstance(value)) {
            return enumType.cast(value);
        }

        String name = String.valueOf(value).trim();

        if (name.isEmpty()) {
            return null;
        }

        try {
            return Enum.valueOf(enumType, name);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }
}
