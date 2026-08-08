package com.sparrowx.agentic.mission.model;

/**
 * Public mission lifecycle state aligned with the Agentic API contract.
 */
public enum MissionStatus {
    UNSPECIFIED,
    SUBMITTED,
    RUNNING,
    WAITING_APPROVAL,
    FAILED_RETRYABLE,
    FAILED_TERMINAL,
    COMPLETED,
    CANCELLED;

    public boolean isTerminal() {
        return this == FAILED_TERMINAL
                || this == COMPLETED
                || this == CANCELLED;
    }
}