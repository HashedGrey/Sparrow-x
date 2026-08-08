package com.sparrowx.agentic.observability;

import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Locale;
import java.util.Objects;

/**
 * Business metrics with bounded-cardinality tags.
 *
 * Mission, tenant, request and Workflow identifiers must not be metric tags.
 */
@Component
public final class MissionMetrics {

    private static final String METRIC_PREFIX =
            "sparrowx.agentic.";

    private final MeterRegistry registry;

    public MissionMetrics(MeterRegistry registry) {
        this.registry = Objects.requireNonNull(
                registry,
                "registry must not be null"
        );
    }

    public void missionSubmitted(String path) {
        registry.counter(
                METRIC_PREFIX + "missions.submitted",
                "path",
                tag(path)
        ).increment();
    }

    public void missionStarted() {
        registry.counter(
                METRIC_PREFIX + "missions.started"
        ).increment();
    }

    public void missionWaitingForApproval() {
        registry.counter(
                METRIC_PREFIX + "missions.waiting_for_approval"
        ).increment();
    }

    public void missionCompleted(
            String outcome,
            Duration elapsed
    ) {
        registry.counter(
                METRIC_PREFIX + "missions.terminal",
                "outcome",
                tag(outcome)
        ).increment();

        missionTimer(outcome).record(safe(elapsed));
    }

    public void missionFailed(
            boolean retryable,
            Duration elapsed
    ) {
        String outcome = retryable
                ? "failed_retryable"
                : "failed_terminal";

        missionCompleted(outcome, elapsed);
    }

    public void missionCancelled(Duration elapsed) {
        missionCompleted("cancelled", elapsed);
    }

    public void stepStarted(String stepKind) {
        registry.counter(
                METRIC_PREFIX + "steps.started",
                "step_kind",
                tag(stepKind)
        ).increment();
    }

    public void stepCompleted(
            String stepKind,
            String outcome,
            Duration elapsed
    ) {
        registry.counter(
                METRIC_PREFIX + "steps.completed",
                "step_kind",
                tag(stepKind),
                "outcome",
                tag(outcome)
        ).increment();

        Timer.builder(METRIC_PREFIX + "step.duration")
                .description("Agentic step execution duration")
                .tag("step_kind", tag(stepKind))
                .tag("outcome", tag(outcome))
                .register(registry)
                .record(safe(elapsed));
    }

    public void toolCallCompleted(
            String toolName,
            String operation,
            String outcome,
            Duration elapsed
    ) {
        registry.counter(
                METRIC_PREFIX + "tool.calls",
                "tool",
                tag(toolName),
                "operation",
                tag(operation),
                "outcome",
                tag(outcome)
        ).increment();

        Timer.builder(METRIC_PREFIX + "tool.call.duration")
                .description("Downstream tool-call duration")
                .tag("tool", tag(toolName))
                .tag("operation", tag(operation))
                .tag("outcome", tag(outcome))
                .register(registry)
                .record(safe(elapsed));
    }

    public void llmCallCompleted(
            String provider,
            String model,
            String outcome,
            Duration elapsed
    ) {
        registry.counter(
                METRIC_PREFIX + "llm.calls",
                "provider",
                tag(provider),
                "model",
                tag(model),
                "outcome",
                tag(outcome)
        ).increment();

        Timer.builder(METRIC_PREFIX + "llm.call.duration")
                .description("Structured LLM call duration")
                .tag("provider", tag(provider))
                .tag("model", tag(model))
                .tag("outcome", tag(outcome))
                .register(registry)
                .record(safe(elapsed));
    }

    public void budgetObserved(
            String budgetName,
            long consumed
    ) {
        DistributionSummary.builder(
                        METRIC_PREFIX + "budget.consumed"
                )
                .description("Observed mission budget consumption")
                .baseUnit("count")
                .tag("budget", tag(budgetName))
                .register(registry)
                .record(Math.max(0L, consumed));
    }

    public void costObserved(long costMicros) {
        DistributionSummary.builder(
                        METRIC_PREFIX + "cost"
                )
                .description("Observed mission cost in micros")
                .baseUnit("micros")
                .register(registry)
                .record(Math.max(0L, costMicros));
    }

    private Timer missionTimer(String outcome) {
        return Timer.builder(METRIC_PREFIX + "mission.duration")
                .description("End-to-end Agentic mission duration")
                .tag("outcome", tag(outcome))
                .register(registry);
    }

    private static Duration safe(Duration duration) {
        if (duration == null || duration.isNegative()) {
            return Duration.ZERO;
        }

        return duration;
    }

    private static String tag(String value) {
        if (value == null || value.isBlank()) {
            return "unspecified";
        }

        return value.trim()
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9_.-]+", "_");
    }
}