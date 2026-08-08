package com.sparrowx.agentic.features.streammissionprogress;

import com.sparrowx.agentic.mission.model.ComponentInvocation;
import com.sparrowx.agentic.mission.model.MissionProgressEvent;
import com.sparrowx.agentic.mission.model.MissionStatus;
import com.sparrowx.agentic.runtime.model.StepStatus;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

public record MissionProgressEventView(
        String missionId,
        MissionStatus status,
        String stageId,
        String stageName,
        String stepId,
        String stepName,
        StepStatus stepStatus,
        ComponentInvocation currentComponent,
        String message,
        double progressPercent,
        String resumeToken,
        Instant emittedAt,
        Map<String, String> metadata
) {
    public MissionProgressEventView {
        if (missionId == null || missionId.isBlank()) {
            throw new IllegalArgumentException("Mission ID is required.");
        }

        missionId = missionId.trim();
        status = Objects.requireNonNull(status, "status");
        stageId = normalize(stageId);
        stageName = normalize(stageName);
        stepId = normalize(stepId);
        stepName = normalize(stepName);
        stepStatus = Objects.requireNonNull(stepStatus, "stepStatus");
        message = message == null ? "" : message;
        resumeToken = normalize(resumeToken);
        emittedAt = Objects.requireNonNull(emittedAt, "emittedAt");
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);

        if (!Double.isFinite(progressPercent)
                || progressPercent < 0.0
                || progressPercent > 100.0) {
            throw new IllegalArgumentException(
                    "Progress percent must be between 0 and 100."
            );
        }
    }

    public static MissionProgressEventView from(
            MissionProgressEvent event
    ) {
        Objects.requireNonNull(event, "event");

        return new MissionProgressEventView(
                event.missionId(),
                event.status(),
                event.stageId(),
                event.stageName(),
                event.stepId(),
                event.stepName(),
                event.stepStatus(),
                event.currentComponent(),
                event.message(),
                event.progressPercent(),
                event.resumeToken(),
                event.emittedAt(),
                event.metadata()
        );
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}