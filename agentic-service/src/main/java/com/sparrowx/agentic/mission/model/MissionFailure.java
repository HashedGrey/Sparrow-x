package com.sparrowx.agentic.mission.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Stable failure envelope persisted with a failed mission projection.
 */
public record MissionFailure(
        String code,
        String message,
        MissionFailureReason reason,
        boolean retryable,
        String failedStageId,
        String failedStepId,
        String failedComponentId,
        Map<String, Object> details
) {

    public MissionFailure {
        code = nullToEmpty(code);
        message = nullToEmpty(message);
        reason = reason == null ? MissionFailureReason.UNSPECIFIED : reason;
        failedStageId = nullToEmpty(failedStageId);
        failedStepId = nullToEmpty(failedStepId);
        failedComponentId = nullToEmpty(failedComponentId);
        details = immutableStruct(details);
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private static Map<String, Object> immutableStruct(Map<String, Object> value) {
        return value == null
                ? Map.of()
                : Collections.unmodifiableMap(new LinkedHashMap<>(value));
    }
}