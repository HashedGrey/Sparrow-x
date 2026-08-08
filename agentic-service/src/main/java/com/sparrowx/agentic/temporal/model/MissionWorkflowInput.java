package com.sparrowx.agentic.temporal.model;

import com.sparrowx.agentic.mission.model.MissionBudget;
import com.sparrowx.agentic.mission.model.MissionPath;
import com.sparrowx.agentic.runtime.checkpoint.CheckpointRef;

import java.util.Objects;

/**
 * Small immutable Workflow input. Payloads remain outside Temporal history.
 */
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

    private static final int MAX_REFERENCE_METADATA_ENTRIES = 128;
    private static final int MAX_REFERENCE_METADATA_KEY_LENGTH = 256;
    private static final int MAX_REFERENCE_METADATA_VALUE_LENGTH = 4_096;

    public MissionWorkflowInput {
        missionId = requireText(missionId, "missionId");
        tenantId = requireText(tenantId, "tenantId");
        requestId = requireText(requestId, "requestId");
        frozenVersionRef = requireText(
                frozenVersionRef,
                "frozenVersionRef"
        );
        traceId = normalize(traceId);
        selectedPath = Objects.requireNonNull(
                selectedPath,
                "selectedPath must not be null"
        );
        budget = Objects.requireNonNull(
                budget,
                "budget must not be null"
        );

        if (selectedPath == MissionPath.UNSPECIFIED) {
            throw new IllegalArgumentException(
                    "selectedPath must be specified"
            );
        }

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
        requireNormalizedBudget(budget);
    }

    private static void requireReference(
            CheckpointRef reference,
            String tenantId,
            String missionId,
            CheckpointRef.CheckpointType expectedType,
            String field
    ) {
        Objects.requireNonNull(
                reference,
                field + " must not be null"
        );

        if (!tenantId.equals(reference.tenantId())
                || !missionId.equals(reference.missionId())
                || expectedType != reference.checkpointType()) {
            throw new IllegalArgumentException(
                    field + " has the wrong mission scope or type"
            );
        }

        requireText(
                reference.checkpointId(),
                field + ".checkpointId"
        );
        requireText(
                reference.contentType(),
                field + ".contentType"
        );
        requireText(
                reference.sha256(),
                field + ".sha256"
        );

        if (reference.schemaVersion() < 1
                || reference.sizeBytes() < 0L) {
            throw new IllegalArgumentException(
                    field + " is not a valid durable reference"
            );
        }

        if (reference.metadata().size()
                > MAX_REFERENCE_METADATA_ENTRIES) {
            throw new IllegalArgumentException(
                    field + " metadata is too large"
            );
        }
        reference.metadata().forEach((key, value) -> {
            if (key == null
                    || key.isBlank()
                    || key.length()
                    > MAX_REFERENCE_METADATA_KEY_LENGTH
                    || value == null
                    || value.length()
                    > MAX_REFERENCE_METADATA_VALUE_LENGTH) {
                throw new IllegalArgumentException(
                        field + " metadata is not history-safe"
                );
            }
        });
    }

    private static void requireNormalizedBudget(
            MissionBudget budget
    ) {
        if (budget.maxLlmCalls() < 0
                || budget.maxToolCalls() < 0
                || budget.maxRetrievalQueries() < 0
                || budget.maxItemsToHydrate() < 0
                || budget.maxInputTokens() < 0L
                || budget.maxOutputTokens() < 0L
                || budget.maxCostMicros() < 0L) {
            throw new IllegalArgumentException(
                    "budget dimensions must not be negative"
            );
        }
    }

    private static String requireText(
            String value,
            String field
    ) {
        String normalized = normalize(value);
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(
                    field + " must not be blank"
            );
        }
        return normalized;
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}