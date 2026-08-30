package com.sparrowx.agentic.temporal.model;

import com.sparrowx.agentic.mission.model.MissionStatus;
import com.sparrowx.agentic.runtime.checkpoint.CheckpointRef;

import java.time.Instant;
import java.util.Objects;

/** Terminal Workflow output containing references, not Embabel state. */
public record MissionWorkflowOutcome(
        String missionId,
        String tenantId,
        MissionStatus status,
        CheckpointRef resultRef,
        String errorReference,
        Instant startedAt,
        Instant completedAt,
        Instant cancelledAt
) {
    public MissionWorkflowOutcome {
        missionId = requireText(missionId, "missionId");
        tenantId = requireText(tenantId, "tenantId");
        status = Objects.requireNonNull(status, "status must not be null");
        errorReference = errorReference == null ? "" : errorReference;
        startedAt = Objects.requireNonNull(
                startedAt,
                "startedAt must not be null"
        );
        completedAt = Objects.requireNonNull(
                completedAt,
                "completedAt must not be null"
        );

        if (status == MissionStatus.COMPLETED) {
            Objects.requireNonNull(
                    resultRef,
                    "completed outcome requires resultRef"
            );
        } else if (status == MissionStatus.CANCELLED) {
            Objects.requireNonNull(
                    cancelledAt,
                    "cancelled outcome requires cancelledAt"
            );
        } else if (status != MissionStatus.FAILED_TERMINAL) {
            throw new IllegalArgumentException(
                    "outcome status must be terminal"
            );
        }
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    field + " must not be blank"
            );
        }
        return value.trim();
    }
}
