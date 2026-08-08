package com.sparrowx.agentic.governance;

import com.sparrowx.agentic.governance.model.GovernanceDecision;
import com.sparrowx.agentic.governance.model.GovernanceDecisionType;
import com.sparrowx.agentic.mission.model.MissionBudget;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public final class BudgetPolicy {

    private static final MissionBudget DEFAULT_BUDGET =
            new MissionBudget(
                    8,
                    16,
                    12,
                    100,
                    120_000L,
                    16_000L,
                    5_000_000L
            );

    private static final MissionBudget MAXIMUM_BUDGET =
            new MissionBudget(
                    64,
                    256,
                    256,
                    10_000,
                    2_000_000L,
                    256_000L,
                    100_000_000L
            );

    private final MissionBudget defaults;
    private final MissionBudget maximums;

    public BudgetPolicy() {
        this(DEFAULT_BUDGET, MAXIMUM_BUDGET);
    }

    private BudgetPolicy(
            MissionBudget defaults,
            MissionBudget maximums
    ) {
        this.defaults = Objects.requireNonNull(
                defaults,
                "defaults must not be null"
        );
        this.maximums = Objects.requireNonNull(
                maximums,
                "maximums must not be null"
        );

        validateConfiguredLimits(defaults, maximums);
    }

    public static BudgetPolicy configured(
            MissionBudget defaults,
            MissionBudget maximums
    ) {
        return new BudgetPolicy(defaults, maximums);
    }

    /**
     * Preserves the signature consumed by MissionSubmissionService.
     * A zero proto scalar means the configured default applies.
     */
    public MissionBudget normalize(MissionBudget requested) {
        if (requested == null) {
            return defaults;
        }

        return new MissionBudget(
                normalizeInt(
                        "max_llm_calls",
                        requested.maxLlmCalls(),
                        defaults.maxLlmCalls(),
                        maximums.maxLlmCalls()
                ),
                normalizeInt(
                        "max_tool_calls",
                        requested.maxToolCalls(),
                        defaults.maxToolCalls(),
                        maximums.maxToolCalls()
                ),
                normalizeInt(
                        "max_retrieval_queries",
                        requested.maxRetrievalQueries(),
                        defaults.maxRetrievalQueries(),
                        maximums.maxRetrievalQueries()
                ),
                normalizeInt(
                        "max_items_to_hydrate",
                        requested.maxItemsToHydrate(),
                        defaults.maxItemsToHydrate(),
                        maximums.maxItemsToHydrate()
                ),
                normalizeLong(
                        "max_input_tokens",
                        requested.maxInputTokens(),
                        defaults.maxInputTokens(),
                        maximums.maxInputTokens()
                ),
                normalizeLong(
                        "max_output_tokens",
                        requested.maxOutputTokens(),
                        defaults.maxOutputTokens(),
                        maximums.maxOutputTokens()
                ),
                normalizeLong(
                        "max_cost_micros",
                        requested.maxCostMicros(),
                        defaults.maxCostMicros(),
                        maximums.maxCostMicros()
                )
        );
    }

    public Remaining remaining(
            MissionBudget budget,
            Duration maxRuntime,
            Usage usage
    ) {
        Objects.requireNonNull(budget, "budget must not be null");
        Objects.requireNonNull(
                maxRuntime,
                "maxRuntime must not be null"
        );
        Objects.requireNonNull(usage, "usage must not be null");

        validateNonNegativeBudget(budget);

        if (maxRuntime.isNegative()) {
            throw new IllegalArgumentException(
                    "constraints.max_runtime must be >= 0"
            );
        }

        boolean runtimeLimited = !maxRuntime.isZero();
        Duration remainingRuntime = Duration.ZERO;

        if (runtimeLimited) {
            Duration candidate = maxRuntime.minus(usage.runtime());
            remainingRuntime = candidate.isNegative()
                    ? Duration.ZERO
                    : candidate;
        }

        return new Remaining(
                subtractFloor(
                        budget.maxLlmCalls(),
                        usage.llmCalls()
                ),
                subtractFloor(
                        budget.maxToolCalls(),
                        usage.toolCalls()
                ),
                subtractFloor(
                        budget.maxRetrievalQueries(),
                        usage.retrievalQueries()
                ),
                subtractFloor(
                        budget.maxItemsToHydrate(),
                        usage.itemsHydrated()
                ),
                subtractFloor(
                        budget.maxInputTokens(),
                        usage.inputTokens()
                ),
                subtractFloor(
                        budget.maxOutputTokens(),
                        usage.outputTokens()
                ),
                subtractFloor(
                        budget.maxCostMicros(),
                        usage.costMicros()
                ),
                remainingRuntime,
                runtimeLimited
        );
    }

    public GovernanceDecision evaluate(
            String decisionId,
            MissionBudget budget,
            Duration maxRuntime,
            Usage usage
    ) {
        requireText(decisionId, "decisionId");

        Remaining remaining = remaining(
                budget,
                maxRuntime,
                usage
        );

        List<String> exceeded = new ArrayList<>();

        addIfExceeded(
                exceeded,
                "LLM_CALLS",
                usage.llmCalls(),
                budget.maxLlmCalls()
        );
        addIfExceeded(
                exceeded,
                "TOOL_CALLS",
                usage.toolCalls(),
                budget.maxToolCalls()
        );
        addIfExceeded(
                exceeded,
                "RETRIEVAL_QUERIES",
                usage.retrievalQueries(),
                budget.maxRetrievalQueries()
        );
        addIfExceeded(
                exceeded,
                "ITEMS_TO_HYDRATE",
                usage.itemsHydrated(),
                budget.maxItemsToHydrate()
        );
        addIfExceeded(
                exceeded,
                "INPUT_TOKENS",
                usage.inputTokens(),
                budget.maxInputTokens()
        );
        addIfExceeded(
                exceeded,
                "OUTPUT_TOKENS",
                usage.outputTokens(),
                budget.maxOutputTokens()
        );
        addIfExceeded(
                exceeded,
                "COST_MICROS",
                usage.costMicros(),
                budget.maxCostMicros()
        );

        if (!maxRuntime.isZero()
                && usage.runtime().compareTo(maxRuntime) > 0) {
            exceeded.add("RUNTIME");
        }

        Map<String, Object> attributes = new LinkedHashMap<>();
        attributes.put(
                "exceededDimensions",
                List.copyOf(exceeded)
        );
        attributes.put(
                "remainingLlmCalls",
                remaining.llmCalls()
        );
        attributes.put(
                "remainingToolCalls",
                remaining.toolCalls()
        );
        attributes.put(
                "remainingRetrievalQueries",
                remaining.retrievalQueries()
        );
        attributes.put(
                "remainingItemsToHydrate",
                remaining.itemsToHydrate()
        );
        attributes.put(
                "remainingInputTokens",
                remaining.inputTokens()
        );
        attributes.put(
                "remainingOutputTokens",
                remaining.outputTokens()
        );
        attributes.put(
                "remainingCostMicros",
                remaining.costMicros()
        );
        attributes.put(
                "runtimeLimited",
                remaining.runtimeLimited()
        );
        attributes.put(
                "remainingRuntimeMillis",
                remaining.runtime().toMillis()
        );

        return new GovernanceDecision(
                decisionId,
                "budget",
                exceeded.isEmpty()
                        ? GovernanceDecisionType.ALLOWED
                        : GovernanceDecisionType.DENIED,
                exceeded.isEmpty()
                        ? "Usage is within every independent budget dimension."
                        : "Budget exceeded: " + String.join(",", exceeded),
                attributes
        );
    }

    public MissionBudget defaults() {
        return defaults;
    }

    public MissionBudget maximums() {
        return maximums;
    }

    private static int normalizeInt(
            String field,
            int requested,
            int fallback,
            int maximum
    ) {
        if (requested < 0) {
            throw new IllegalArgumentException(
                    "budget." + field + " must be >= 0"
            );
        }

        int normalized = requested == 0 ? fallback : requested;

        if (normalized > maximum) {
            throw new IllegalArgumentException(
                    "budget." + field + " exceeds policy maximum"
            );
        }

        return normalized;
    }

    private static long normalizeLong(
            String field,
            long requested,
            long fallback,
            long maximum
    ) {
        if (requested < 0L) {
            throw new IllegalArgumentException(
                    "budget." + field + " must be >= 0"
            );
        }

        long normalized = requested == 0L ? fallback : requested;

        if (normalized > maximum) {
            throw new IllegalArgumentException(
                    "budget." + field + " exceeds policy maximum"
            );
        }

        return normalized;
    }

    private static void validateConfiguredLimits(
            MissionBudget defaults,
            MissionBudget maximums
    ) {
        validateNonNegativeBudget(defaults);
        validateNonNegativeBudget(maximums);

        if (defaults.maxLlmCalls() > maximums.maxLlmCalls()
                || defaults.maxToolCalls() > maximums.maxToolCalls()
                || defaults.maxRetrievalQueries()
                > maximums.maxRetrievalQueries()
                || defaults.maxItemsToHydrate()
                > maximums.maxItemsToHydrate()
                || defaults.maxInputTokens()
                > maximums.maxInputTokens()
                || defaults.maxOutputTokens()
                > maximums.maxOutputTokens()
                || defaults.maxCostMicros()
                > maximums.maxCostMicros()) {
            throw new IllegalArgumentException(
                    "each default budget dimension must be <= its policy maximum"
            );
        }
    }

    private static void validateNonNegativeBudget(
            MissionBudget budget
    ) {
        if (budget.maxLlmCalls() < 0
                || budget.maxToolCalls() < 0
                || budget.maxRetrievalQueries() < 0
                || budget.maxItemsToHydrate() < 0
                || budget.maxInputTokens() < 0L
                || budget.maxOutputTokens() < 0L
                || budget.maxCostMicros() < 0L) {
            throw new IllegalArgumentException(
                    "budget dimensions must be >= 0"
            );
        }
    }

    private static int subtractFloor(int limit, int used) {
        return Math.max(0, limit - used);
    }

    private static long subtractFloor(long limit, long used) {
        return Math.max(0L, limit - used);
    }

    private static void addIfExceeded(
            List<String> exceeded,
            String dimension,
            long used,
            long limit
    ) {
        if (used > limit) {
            exceeded.add(dimension);
        }
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    field + " must not be blank"
            );
        }
        return value;
    }

    public record Usage(
            int llmCalls,
            int toolCalls,
            int retrievalQueries,
            int itemsHydrated,
            long inputTokens,
            long outputTokens,
            long costMicros,
            Duration runtime
    ) {
        public Usage {
            runtime = Objects.requireNonNull(
                    runtime,
                    "runtime must not be null"
            );

            if (llmCalls < 0
                    || toolCalls < 0
                    || retrievalQueries < 0
                    || itemsHydrated < 0
                    || inputTokens < 0L
                    || outputTokens < 0L
                    || costMicros < 0L
                    || runtime.isNegative()) {
                throw new IllegalArgumentException(
                        "usage dimensions must be >= 0"
                );
            }
        }
    }

    public record Remaining(
            int llmCalls,
            int toolCalls,
            int retrievalQueries,
            int itemsToHydrate,
            long inputTokens,
            long outputTokens,
            long costMicros,
            Duration runtime,
            boolean runtimeLimited
    ) {
        public Remaining {
            runtime = Objects.requireNonNull(
                    runtime,
                    "runtime must not be null"
            );
        }
    }
}