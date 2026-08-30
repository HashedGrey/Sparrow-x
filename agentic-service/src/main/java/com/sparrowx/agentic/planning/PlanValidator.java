package com.sparrowx.agentic.planning;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class PlanValidator {

    public void validate(
            MissionPlan plan,
            MissionIntent intent,
            Set<String> allowedTools,
            int remainingToolCalls) {

        Objects.requireNonNull(plan, "plan must not be null");
        Objects.requireNonNull(intent, "intent must not be null");

        if (remainingToolCalls < 0) {
            throw new IllegalArgumentException(
                    "remainingToolCalls must not be negative");
        }

        if (!plan.missionId().equals(intent.missionId())) {
            throw new IllegalArgumentException(
                    "plan and intent belong to different missions");
        }

        if (!plan.intent().equals(intent)) {
            throw new IllegalArgumentException(
                    "plan contains a different mission intent");
        }

        if (plan.steps().isEmpty()) {
            throw new IllegalArgumentException(
                    "plan must contain at least one step");
        }

        if (plan.steps().size() > remainingToolCalls) {
            throw new IllegalArgumentException(
                    "plan exceeds the remaining tool-call budget");
        }

        ToolScope effectiveTools = effectiveTools(
                intent.allowedTools(),
                allowedTools);

        Map<String, PlannedStep> stepsById = new HashMap<>();

        for (PlannedStep step : plan.steps()) {
            stepsById.put(step.stepId(), step);


            if (effectiveTools.restricted()
                    && !effectiveTools.capabilities()
                    .contains(step.capability())) {
                throw new IllegalArgumentException(
                        "step uses an unauthorized capability: "
                                + step.capability());
            }
            validateArguments(step);
        }

        validateDependencies(stepsById);
        validateAcyclic(stepsById);
    }

    private static void validateArguments(PlannedStep step) {
        switch (step.kind()) {
            case SEARCH_INTERNAL_ENTITIES ->
                    validateInternalSearchArguments(step);
            default -> {
            }
        }
    }

    private static void validateInternalSearchArguments(PlannedStep step) {
        Set<String> allowed = Set.of(
                "query",
                "allowedNodeTypes",
                "rootEntityId",
                "rootNodeType",
                "depth",
                "limit",
                "includeFuzzyMatches",
                "filters"
        );

        Set<String> unknown = new HashSet<>(step.arguments().keySet());
        unknown.removeAll(allowed);

        if (!unknown.isEmpty()) {
            throw new IllegalArgumentException(
                    "SEARCH_INTERNAL_ENTITIES contains unsupported arguments: "
                            + unknown);
        }

        Object query = step.arguments().get("query");

        if (!(query instanceof String text) || text.isBlank()) {
            throw new IllegalArgumentException(
                    "SEARCH_INTERNAL_ENTITIES requires a non-blank query");
        }
    }

    private static void validateDependencies(
            Map<String, PlannedStep> stepsById) {

        for (PlannedStep step : stepsById.values()) {
            for (String dependencyId
                    : step.dependencyStepIds()) {

                if (!stepsById.containsKey(dependencyId)) {
                    throw new IllegalArgumentException(
                            "unknown dependency "
                                    + dependencyId
                                    + " for step "
                                    + step.stepId());
                }
            }
        }
    }

    private static void validateAcyclic(
            Map<String, PlannedStep> stepsById) {

        Map<String, VisitState> states = new HashMap<>();

        for (String stepId : stepsById.keySet()) {
            visit(stepId, stepsById, states);
        }
    }

    private static void visit(
            String stepId,
            Map<String, PlannedStep> stepsById,
            Map<String, VisitState> states) {

        VisitState state = states.get(stepId);

        if (state == VisitState.VISITED) {
            return;
        }

        if (state == VisitState.VISITING) {
            throw new IllegalArgumentException(
                    "plan contains a dependency cycle at " + stepId);
        }

        states.put(stepId, VisitState.VISITING);

        for (String dependencyId
                : stepsById.get(stepId).dependencyStepIds()) {

            visit(dependencyId, stepsById, states);
        }

        states.put(stepId, VisitState.VISITED);
    }

    private static ToolScope effectiveTools(
            Set<String> intentTools,
            Set<String> policyTools) {

        Set<String> fromIntent = intentTools == null
                ? Set.of()
                : Set.copyOf(intentTools);
        Set<String> fromPolicy = policyTools == null
                ? Set.of()
                : Set.copyOf(policyTools);

        if (fromIntent.isEmpty()) {
            return new ToolScope(
                    !fromPolicy.isEmpty(),
                    fromPolicy);
        }

        if (fromPolicy.isEmpty()) {
            return new ToolScope(true, fromIntent);
        }

        Set<String> intersection = new HashSet<>(fromIntent);
        intersection.retainAll(fromPolicy);
        return new ToolScope(
                true,
                Set.copyOf(intersection));
    }

    private record ToolScope(
            boolean restricted,
            Set<String> capabilities) {
    }

    private enum VisitState {
        VISITING,
        VISITED
    }
}