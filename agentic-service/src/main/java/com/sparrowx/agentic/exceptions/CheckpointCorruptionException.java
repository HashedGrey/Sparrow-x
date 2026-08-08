package com.sparrowx.agentic.exceptions;

import java.util.Map;

public final class CheckpointCorruptionException
        extends AgenticServiceException {

    private final String checkpointId;

    public CheckpointCorruptionException(String message) {
        this("", message, null);
    }

    public CheckpointCorruptionException(
            String message,
            Throwable cause
    ) {
        this("", message, cause);
    }

    public CheckpointCorruptionException(
            String checkpointId,
            String message
    ) {
        this(checkpointId, message, null);
    }

    public CheckpointCorruptionException(
            String checkpointId,
            String message,
            Throwable cause
    ) {
        super(
                "CHECKPOINT_CORRUPTION",
                message,
                false,
                details(checkpointId),
                cause
        );
        this.checkpointId = normalize(checkpointId);
    }

    public String checkpointId() {
        return checkpointId;
    }

    private static Map<String, Object> details(String checkpointId) {
        String normalized = normalize(checkpointId);

        return normalized.isEmpty()
                ? Map.of()
                : Map.of("checkpointId", normalized);
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}