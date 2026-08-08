package com.sparrowx.agentic.runtime.model;

/**
 * QUEUED means scheduled; RUNNING means the Activity is executing.
 */
public enum StepStatus {
    UNSPECIFIED,
    QUEUED,
    RUNNING,
    PAUSED,
    SUCCEEDED,
    FAILED_RETRYABLE,
    FAILED_TERMINAL,
    CANCELLED;

    public boolean isTerminal() {
        return this == SUCCEEDED
                || this == FAILED_TERMINAL
                || this == CANCELLED;
    }
}