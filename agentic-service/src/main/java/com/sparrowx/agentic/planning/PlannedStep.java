package com.sparrowx.agentic.planning;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

public record PlannedStep(
        String stepId,
        StepKind kind,
        Set<String> dependencyStepIds,
        String objective,
        String expectedOutput,
        boolean requiresHumanApproval,
        Map<String, Object> arguments,
        Map<String, Object> attributes) {

    public PlannedStep {
        stepId = requireText(stepId, "stepId");
        kind = Objects.requireNonNull(
                kind,
                "kind must not be null");
        dependencyStepIds = dependencyStepIds == null
                ? Set.of()
                : Set.copyOf(dependencyStepIds);

        if (dependencyStepIds.contains(stepId)) {
            throw new IllegalArgumentException(
                    "step must not depend on itself");
        }

        objective = requireText(objective, "objective");
        expectedOutput = requireText(
                expectedOutput,
                "expectedOutput");
        arguments = arguments == null
                ? Map.of()
                : Map.copyOf(arguments);
        attributes = attributes == null
                ? Map.of()
                : Map.copyOf(attributes);
    }

    public String capability() {
        return kind.capability();
    }

    public boolean dependenciesSatisfied(
            Set<String> completedStepIds) {

        Set<String> completed = completedStepIds == null
                ? Set.of()
                : Set.copyOf(completedStepIds);

        return completed.containsAll(dependencyStepIds);
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