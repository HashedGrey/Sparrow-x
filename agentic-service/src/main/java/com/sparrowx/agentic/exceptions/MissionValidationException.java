package com.sparrowx.agentic.exceptions;

import java.util.Map;

public final class MissionValidationException
        extends AgenticServiceException {

    public MissionValidationException(String message) {
        this(message, Map.of(), null);
    }

    public MissionValidationException(
            String message,
            Throwable cause
    ) {
        this(message, Map.of(), cause);
    }

    public MissionValidationException(
            String message,
            Map<String, Object> details
    ) {
        this(message, details, null);
    }

    public MissionValidationException(
            String message,
            Map<String, Object> details,
            Throwable cause
    ) {
        super(
                "MISSION_VALIDATION_FAILED",
                message,
                false,
                details,
                cause
        );
    }
}