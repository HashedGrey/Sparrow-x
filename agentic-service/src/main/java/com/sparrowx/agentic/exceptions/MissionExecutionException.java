package com.sparrowx.agentic.exceptions;

import java.util.Map;

public final class MissionExecutionException
        extends AgenticServiceException {

    private final String missionId;

    public MissionExecutionException(String message) {
        this("", message, false, null);
    }

    public MissionExecutionException(
            String message,
            Throwable cause
    ) {
        this("", message, false, cause);
    }

    public MissionExecutionException(
            String message,
            boolean retryable
    ) {
        this("", message, retryable, null);
    }

    public MissionExecutionException(
            String missionId,
            String message,
            boolean retryable
    ) {
        this(missionId, message, retryable, null);
    }

    public MissionExecutionException(
            String missionId,
            String message,
            boolean retryable,
            Throwable cause
    ) {
        super(
                "MISSION_EXECUTION_FAILED",
                message,
                retryable,
                details(missionId),
                cause
        );
        this.missionId = normalize(missionId);
    }

    public String missionId() {
        return missionId;
    }

    private static Map<String, Object> details(String missionId) {
        String normalized = normalize(missionId);

        return normalized.isEmpty()
                ? Map.of()
                : Map.of("missionId", normalized);
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}