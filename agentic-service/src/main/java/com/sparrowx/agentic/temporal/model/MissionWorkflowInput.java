package com.sparrowx.agentic.temporal.model;

import com.sparrowx.agentic.mission.model.MissionBudget;
import com.sparrowx.agentic.mission.model.MissionPath;
import com.sparrowx.agentic.runtime.checkpoint.CheckpointRef;

import java.util.Objects;

/** Small immutable input containing references, never Embabel state. */
public record MissionWorkflowInput(
        String missionId,
        String tenantId,
        String requestId,
        CheckpointRef missionInputRef,
        CheckpointRef preparedArtifactsRef,
        String frozenVersionRef,
        MissionPath selectedPath,
        MissionBudget budget,
        String traceId
) {
    public MissionWorkflowInput {
        missionId = requireText(missionId, "missionId");
        tenantId = requireText(tenantId, "tenantId");
        requestId = requireText(requestId, "requestId");
        frozenVersionRef = requireText(
                frozenVersionRef,
                "frozenVersionRef"
        );
        traceId = traceId == null ? "" : traceId.trim();
        selectedPath = Objects.requireNonNull(
                selectedPath,
                "selectedPath must not be null"
        );
        budget = Objects.requireNonNull(
                budget,
                "budget must not be null"
        );
        requireReference(
                missionInputRef,
                tenantId,
                missionId,
                CheckpointRef.CheckpointType.MISSION_INPUT,
                "missionInputRef"
        );
        requireReference(
                preparedArtifactsRef,
                tenantId,
                missionId,
                CheckpointRef.CheckpointType.PREPARED_ARTIFACTS,
                "preparedArtifactsRef"
        );
    }

    private static void requireReference(
            CheckpointRef reference,
            String tenantId,
            String missionId,
            CheckpointRef.CheckpointType type,
            String field
    ) {
        Objects.requireNonNull(reference, field + " must not be null");
        if (!tenantId.equals(reference.tenantId())
                || !missionId.equals(reference.missionId())
                || type != reference.checkpointType()) {
            throw new IllegalArgumentException(
                    field + " has the wrong mission scope or type"
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
