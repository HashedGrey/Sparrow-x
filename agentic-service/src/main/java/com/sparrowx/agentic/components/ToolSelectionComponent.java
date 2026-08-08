package com.sparrowx.agentic.components;

import com.sparrowx.agentic.planning.MissionIntent;
import com.sparrowx.agentic.planning.MissionPlan;
import com.sparrowx.agentic.planning.PlannedStep;
import com.sparrowx.agentic.planning.ToolSelectionPolicy;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class ToolSelectionComponent {

    private final ToolSelectionPolicy selectionPolicy;

    public ToolSelectionComponent(
            ToolSelectionPolicy selectionPolicy) {

        this.selectionPolicy = Objects.requireNonNull(
                selectionPolicy,
                "selectionPolicy must not be null");
    }

    public PlannedStep select(SelectionRequest request) {
        Objects.requireNonNull(request, "request must not be null");

        if (request.remainingToolCalls() == 0) {
            throw new IllegalStateException(
                    "tool-call budget is exhausted");
        }

        return Objects.requireNonNull(
                selectionPolicy.selectNext(
                        request.intent(),
                        request.plan(),
                        request.completedStepIds(),
                        request.allowedTools(),
                        request.remainingToolCalls(),
                        request.governanceAttributes()),
                "selectionPolicy returned null");
    }

    public record SelectionRequest(
            MissionIntent intent,
            MissionPlan plan,
            Set<String> completedStepIds,
            Set<String> allowedTools,
            int remainingToolCalls,
            Map<String, Object> governanceAttributes) {

        public SelectionRequest {
            intent = Objects.requireNonNull(
                    intent,
                    "intent must not be null");

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

            governanceAttributes =
                    governanceAttributes == null
                            ? Map.of()
                            : Map.copyOf(governanceAttributes);
        }

        public SelectionRequest withPlan(MissionPlan replacement) {
            return new SelectionRequest(
                    intent,
                    Objects.requireNonNull(
                            replacement,
                            "replacement plan must not be null"),
                    completedStepIds,
                    allowedTools,
                    remainingToolCalls,
                    governanceAttributes);
        }
    }
}