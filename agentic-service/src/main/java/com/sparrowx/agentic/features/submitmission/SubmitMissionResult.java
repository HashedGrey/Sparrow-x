package com.sparrowx.agentic.features.submitmission;

import com.sparrowx.agentic.mission.model.MissionPath;
import com.sparrowx.agentic.mission.model.MissionStatus;

import java.time.Instant;
import java.util.Objects;

public record SubmitMissionResult(
        String missionId,
        MissionStatus status,
        MissionPath selectedPath,
        Instant submittedAt
) {
    public SubmitMissionResult {
        if (missionId == null || missionId.isBlank()) {
            throw new IllegalArgumentException("Mission ID is required.");
        }

        missionId = missionId.trim();
        status = Objects.requireNonNull(status, "status");
        selectedPath = Objects.requireNonNull(selectedPath, "selectedPath");
        submittedAt = Objects.requireNonNull(submittedAt, "submittedAt");
    }
}