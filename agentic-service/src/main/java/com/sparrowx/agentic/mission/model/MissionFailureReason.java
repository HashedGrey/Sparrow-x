package com.sparrowx.agentic.mission.model;

/**
 * Stable classification used to map failures into public MissionError values.
 */
public enum MissionFailureReason {
    UNSPECIFIED,
    VALIDATION_FAILED,
    AUTHENTICATION_FAILED,
    AUTHORIZATION_DENIED,
    POLICY_DENIED,
    BUDGET_EXHAUSTED,
    DOWNSTREAM_UNAVAILABLE,
    DOWNSTREAM_REJECTED,
    LLM_INVOCATION_FAILED,
    WORKFLOW_EXECUTION_FAILED,
    CHECKPOINT_CORRUPTED,
    INTERNAL_ERROR
}