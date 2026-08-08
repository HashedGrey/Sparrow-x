package com.sparrowx.agentic.mission.model;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Public projection of an unresolved human approval gate.
 */
public record HumanGateState(
        String gateId,
        String missionId,
        String title,
        String reason,
        List<String> requiredReviewerRoles,
        Map<String, Object> reviewPayload,
        Instant createdAt,
        Instant expiresAt
) {

    public HumanGateState {
        gateId = nullToEmpty(gateId);
        missionId = nullToEmpty(missionId);
        title = nullToEmpty(title);
        reason = nullToEmpty(reason);
        requiredReviewerRoles = requiredReviewerRoles == null
                ? List.of()
                : List.copyOf(requiredReviewerRoles);
        reviewPayload = immutableStruct(reviewPayload);
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