package com.sparrowx.agentic.planning;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class ToolSelectionPolicy {

    private static final String DENIED_STEP_IDS =
            "deniedStepIds";
    private static final String DENIED_CAPABILITIES =
            "deniedCapabilities";

    public PlannedStep selectNext(
            MissionIntent intent,
            MissionPlan plan,
            Set<String> completedStepIds,
            Set<String> allowedTools,
            int remainingToolCalls,
            Map<String, Object> governanceAttributes) {

        Objects.requireNonNull(intent, "intent must not be null");
        Objects.requireNonNull(plan, "plan must not be null");

        if (!intent.missionId().equals(plan.missionId())) {
            throw new IllegalArgumentException(
                    "plan and intent belong to different missions");
        }

        if (!plan.intent().equals(intent)) {
            throw new IllegalArgumentException(
                    "plan contains a different mission intent");
        }

        if (remainingToolCalls <= 0) {
            throw new IllegalStateException(
                    "tool-call budget is exhausted");
        }

        Set<String> completed = completedStepIds == null
                ? Set.of()
                : Set.copyOf(completedStepIds);
        ToolScope effectiveTools = effectiveTools(
                intent.allowedTools(),
                allowedTools);
        Map<String, Object> governance =
                governanceAttributes == null
                        ? Map.of()
                        : Map.copyOf(governanceAttributes);
        Set<String> deniedStepIds = stringSet(
                governance.get(DENIED_STEP_IDS));
        Set<String> deniedCapabilities = stringSet(
                governance.get(DENIED_CAPABILITIES));

        boolean hasPendingStep = plan.steps().stream()
                .anyMatch(step ->
                        !completed.contains(step.stepId()));

        if (!hasPendingStep) {
            throw new IllegalStateException(
                    "plan has no pending step");
        }

        return plan.steps().stream()
                .filter(step ->
                        !completed.contains(step.stepId()))
                .filter(step ->
                        step.dependenciesSatisfied(completed))
                .filter(step ->
                        !effectiveTools.restricted()
                                || effectiveTools.capabilities()
                                .contains(step.capability()))
                .filter(step ->
                        !deniedStepIds.contains(step.stepId()))
                .filter(step ->
                        !deniedCapabilities.contains(
                                step.capability()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "no authorized planned step is ready"));
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

    private static Set<String> stringSet(Object value) {
        if (!(value instanceof Collection<?> values)) {
            return Set.of();
        }

        Set<String> result = new HashSet<>();

        for (Object item : values) {
            if (item instanceof String text
                    && !text.isBlank()) {
                result.add(text);
            }
        }

        return Set.copyOf(result);
    }

    private record ToolScope(
            boolean restricted,
            Set<String> capabilities) {
    }
}