package com.sparrowx.agentic.features.rejectmissiongate;

import com.sparrowx.agentic.mission.model.MissionStatus;

import java.time.Instant;
import java.util.Objects;

public record RejectMissionGateResult(
        String missionId,
        MissionStatus status,
        Instant rejectedAt
) {

    public RejectMissionGateResult {
        if (missionId == null || missionId.isBlank()) {
            throw new IllegalArgumentException(
                    "Mission ID is required"
            );
        }

        status = Objects.requireNonNull(
                status,
                "status is required"
        );
        rejectedAt = Objects.requireNonNull(
                rejectedAt,
                "rejectedAt is required"
        );
    }
}