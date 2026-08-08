package com.sparrowx.agentic.planning;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public record MissionPlan(
        String planId,
        String missionId,
        int revision,
        MissionIntent intent,
        List<PlannedStep> steps,
        String rationale,
        Instant createdAt,
        Map<String, Object> attributes) {

    public MissionPlan {
        planId = requireText(planId, "planId");
        missionId = requireText(missionId, "missionId");

        if (revision <= 0) {
            throw new IllegalArgumentException(
                    "revision must be positive");
        }

        intent = Objects.requireNonNull(
                intent,
                "intent must not be null");

        if (!missionId.equals(intent.missionId())) {
            throw new IllegalArgumentException(
                    "intent belongs to another mission");
        }

        steps = steps == null
                ? List.of()
                : List.copyOf(steps);
        requireUniqueStepIds(steps);
        rationale = rationale == null ? "" : rationale;
        createdAt = Objects.requireNonNull(
                createdAt,
                "createdAt must not be null");
        attributes = attributes == null
                ? Map.of()
                : Map.copyOf(attributes);
    }

    public Optional<PlannedStep> findStep(String stepId) {
        if (stepId == null || stepId.isBlank()) {
            return Optional.empty();
        }

        return steps.stream()
                .filter(step -> step.stepId().equals(stepId))
                .findFirst();
    }

    public List<PlannedStep> pendingSteps(
            Set<String> completedStepIds) {

        Set<String> completed = completedStepIds == null
                ? Set.of()
                : Set.copyOf(completedStepIds);

        return steps.stream()
                .filter(step -> !completed.contains(step.stepId()))
                .toList();
    }

    private static void requireUniqueStepIds(
            List<PlannedStep> steps) {

        Set<String> stepIds = new HashSet<>();

        for (PlannedStep step : steps) {
            Objects.requireNonNull(
                    step,
                    "steps must not contain null");

            if (!stepIds.add(step.stepId())) {
                throw new IllegalArgumentException(
                        "duplicate planned step: " + step.stepId());
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