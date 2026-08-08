package com.sparrowx.agentic.features.cancelmission;

import com.sparrowx.agentic.mission.model.MissionStatus;

import java.time.Instant;
import java.util.Objects;

public record CancelMissionResult(
        String missionId,
        MissionStatus status,
        Instant cancelledAt
) {

    public CancelMissionResult {
        if (missionId == null || missionId.isBlank()) {
            throw new IllegalArgumentException(
                    "Mission ID is required"
            );
        }

        status = Objects.requireNonNull(
                status,
                "status is required"
        );

        if (status != MissionStatus.CANCELLED) {
            throw new IllegalArgumentException(
                    "status must be CANCELLED"
            );
        }

        cancelledAt = Objects.requireNonNull(
                cancelledAt,
                "cancelledAt is required"
        );
    }
}