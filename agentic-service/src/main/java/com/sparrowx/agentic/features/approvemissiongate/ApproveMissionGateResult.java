package com.sparrowx.agentic.features.approvemissiongate;

import com.sparrowx.agentic.mission.model.MissionStatus;

import java.time.Instant;
import java.util.Objects;

public record ApproveMissionGateResult(
        String missionId,
        MissionStatus status,
        Instant approvedAt
) {

    public ApproveMissionGateResult {
        missionId = requireText(missionId, "missionId");
        status = Objects.requireNonNull(
                status,
                "status must not be null"
        );
        approvedAt = Objects.requireNonNull(
                approvedAt,
                "approvedAt must not be null"
        );
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    field + " must not be blank"
            );
        }
        return value;
    }
}