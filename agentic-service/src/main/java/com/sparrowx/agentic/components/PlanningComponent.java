package com.sparrowx.agentic.components;

import com.sparrowx.agentic.planning.MissionIntent;
import com.sparrowx.agentic.planning.MissionPlan;
import com.sparrowx.agentic.planning.PlanValidator;
import com.sparrowx.agentic.planning.StepKind;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class PlanningComponent {

    private final Planner planner;
    private final PlanValidator planValidator;

    public PlanningComponent(
            Planner planner,
            PlanValidator planValidator) {

        this.planner = Objects.requireNonNull(
                planner,
                "planner must not be null");

        this.planValidator = Objects.requireNonNull(
                planValidator,
                "planValidator must not be null");
    }

    public MissionPlan plan(PlanningRequest request) {
        Objects.requireNonNull(request, "request must not be null");

        MissionPlan plan = Objects.requireNonNull(
                planner.plan(request),
                "planner returned null");

        planValidator.validate(
                plan,
                request.intent(),
                request.allowedTools(),
                request.remainingToolCalls());

        return plan;
    }

    @FunctionalInterface
    public interface Planner {
        MissionPlan plan(PlanningRequest request);
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
            Map<String, Object> attributes) {

        public PlanningRequest {
            missionId = requireText(missionId, "missionId");

            intent = Objects.requireNonNull(
                    intent,
                    "intent must not be null");

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
                        "remainingToolCalls must not be negative");
            }

            if (remainingLlmCalls < 0) {
                throw new IllegalArgumentException(
                        "remainingLlmCalls must not be negative");
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
            Map<String, Object> attributes) {

        public Observation {
            stepId = requireText(stepId, "stepId");

            stepKind = Objects.requireNonNull(
                    stepKind,
                    "stepKind must not be null");

            summary = summary == null ? "" : summary;

            checkpointReference = checkpointReference == null
                    ? ""
                    : checkpointReference;

            attributes = attributes == null
                    ? Map.of()
                    : Map.copyOf(attributes);
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