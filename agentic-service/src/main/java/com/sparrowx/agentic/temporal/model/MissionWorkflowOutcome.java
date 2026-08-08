package com.sparrowx.agentic.temporal.model;

import com.sparrowx.agentic.mission.model.MissionStatus;
import com.sparrowx.agentic.runtime.checkpoint.CheckpointRef;

import java.time.Instant;
import java.util.Objects;
import java.util.Set;

/**
 * Terminal Workflow result containing references rather than large payloads.
 */
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

    private static final Set<MissionStatus> TERMINAL_STATUSES =
            Set.of(
                    MissionStatus.COMPLETED,
                    MissionStatus.FAILED_TERMINAL,
                    MissionStatus.CANCELLED
            );

    public MissionWorkflowOutcome {
        missionId = requireText(missionId, "missionId");
        tenantId = requireText(tenantId, "tenantId");
        status = Objects.requireNonNull(
                status,
                "status must not be null"
        );
        errorReference = normalize(errorReference);
        startedAt = Objects.requireNonNull(
                startedAt,
                "startedAt must not be null"
        );
        completedAt = Objects.requireNonNull(
                completedAt,
                "completedAt must not be null"
        );

        if (!TERMINAL_STATUSES.contains(status)) {
            throw new IllegalArgumentException(
                    "outcome status must be terminal"
            );
        }

        if (completedAt.isBefore(startedAt)) {
            throw new IllegalArgumentException(
                    "completedAt must not precede startedAt"
            );
        }

        switch (status) {
            case COMPLETED -> {
                requireResultRef(
                        resultRef,
                        tenantId,
                        missionId
                );

                if (!errorReference.isEmpty()
                        || cancelledAt != null) {
                    throw new IllegalArgumentException(
                            "completed outcome accepts only resultRef"
                    );
                }
            }

            case FAILED_TERMINAL -> {
                if (resultRef != null
                        || errorReference.isEmpty()
                        || cancelledAt != null) {
                    throw new IllegalArgumentException(
                            "failed outcome requires only errorReference"
                    );
                }
            }

            case CANCELLED -> {
                if (resultRef != null
                        || !errorReference.isEmpty()
                        || cancelledAt == null) {
                    throw new IllegalArgumentException(
                            "cancelled outcome requires cancelledAt"
                    );
                }

                if (cancelledAt.isBefore(startedAt)
                        || cancelledAt.isAfter(completedAt)) {
                    throw new IllegalArgumentException(
                            "cancelledAt is outside Workflow lifetime"
                    );
                }
            }

            default -> throw new IllegalArgumentException(
                    "outcome status must be terminal"
            );
        }
    }

    private static void requireResultRef(
            CheckpointRef reference,
            String tenantId,
            String missionId
    ) {
        Objects.requireNonNull(
                reference,
                "completed outcome requires resultRef"
        );

        if (!tenantId.equals(reference.tenantId())
                || !missionId.equals(reference.missionId())
                || reference.checkpointType()
                != CheckpointRef.CheckpointType.MISSION_RESULT) {
            throw new IllegalArgumentException(
                    "resultRef has the wrong mission scope or type"
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