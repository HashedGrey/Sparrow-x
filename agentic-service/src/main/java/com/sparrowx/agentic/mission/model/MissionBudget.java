package com.sparrowx.agentic.mission.model;

/**
 * Raw mission limits. BudgetPolicy validates and normalizes these values.
 */
public record MissionBudget(
        int maxLlmCalls,
        int maxToolCalls,
        int maxRetrievalQueries,
        int maxItemsToHydrate,
        long maxInputTokens,
        long maxOutputTokens,
        long maxCostMicros
) {
}