package com.sparrowx.agentic.components;

import com.sparrowx.agentic.components.PlanningComponent.Observation;
import com.sparrowx.agentic.planning.MissionIntent;
import com.sparrowx.agentic.planning.MissionPlan;
import com.sparrowx.agentic.planning.PlannedStep;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class ReviewComponent {

    private final Reviewer reviewer;

    public ReviewComponent(Reviewer reviewer) {
        this.reviewer = Objects.requireNonNull(
                reviewer,
                "reviewer must not be null");
    }

    public ReviewDecision review(ReviewRequest request) {
        Objects.requireNonNull(request, "request must not be null");

        return Objects.requireNonNull(
                reviewer.review(request),
                "reviewer returned null");
    }

    @FunctionalInterface
    public interface Reviewer {
        ReviewDecision review(ReviewRequest request);
    }

    public record ReviewRequest(
            String missionId,
            MissionIntent intent,
            MissionPlan plan,
            PlannedStep executedStep,
            Observation observation,
            Set<String> completedStepIds,
            int remainingToolCalls,
            boolean cancellationRequested,
            Map<String, Object> attributes) {

        public ReviewRequest {
            missionId = requireText(missionId, "missionId");

            intent = Objects.requireNonNull(
                    intent,
                    "intent must not be null");

            plan = Objects.requireNonNull(
                    plan,
                    "plan must not be null");

            executedStep = Objects.requireNonNull(
                    executedStep,
                    "executedStep must not be null");

            observation = Objects.requireNonNull(
                    observation,
                    "observation must not be null");

            completedStepIds = completedStepIds == null
                    ? Set.of()
                    : Set.copyOf(completedStepIds);

            if (remainingToolCalls < 0) {
                throw new IllegalArgumentException(
                        "remainingToolCalls must not be negative");
            }

            attributes = attributes == null
                    ? Map.of()
                    : Map.copyOf(attributes);
        }
    }

    public record ReviewDecision(
            DecisionType type,
            String reason,
            Map<String, Object> planHints) {

        public ReviewDecision {
            type = Objects.requireNonNull(
                    type,
                    "type must not be null");

            reason = reason == null ? "" : reason;

            planHints = planHints == null
                    ? Map.of()
                    : Map.copyOf(planHints);
        }
    }

    public enum DecisionType {
        CONTINUE,
        REPLAN,
        COMPLETE,
        WAIT_FOR_APPROVAL,
        FAIL
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