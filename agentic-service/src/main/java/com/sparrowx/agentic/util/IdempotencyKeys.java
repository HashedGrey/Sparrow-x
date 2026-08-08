package com.sparrowx.agentic.util;

import java.util.Arrays;

public final class IdempotencyKeys {

    private static final int MAX_EXTERNAL_ID_LENGTH = 256;

    private IdempotencyKeys() {
    }

    public static String missionWorkflowId(
            String tenantId,
            String missionId
    ) {
        return key("mission-workflow", tenantId, missionId);
    }

    public static String workflowId(
            String tenantId,
            String missionId
    ) {
        return missionWorkflowId(tenantId, missionId);
    }

    public static String updateId(String requestId) {
        return requiredPart("requestId", requestId);
    }

    public static String missionSubmission(
            String tenantId,
            String requestId
    ) {
        return key("mission-submission", tenantId, requestId);
    }

    public static String activityEffect(
            String tenantId,
            String missionId,
            String logicalStepId,
            String operation
    ) {
        return key(
                "activity-effect",
                tenantId,
                missionId,
                logicalStepId,
                operation
        );
    }

    public static String activityEffectId(
            String tenantId,
            String missionId,
            String logicalStepId,
            String operation
    ) {
        return activityEffect(
                tenantId,
                missionId,
                logicalStepId,
                operation
        );
    }

    public static String eventId(
            String tenantId,
            String missionId,
            String logicalEventId
    ) {
        return key(
                "mission-event",
                tenantId,
                missionId,
                logicalEventId
        );
    }

    public static String toolCallId(
            String tenantId,
            String missionId,
            String logicalStepId,
            String toolName
    ) {
        return key(
                "tool-call",
                tenantId,
                missionId,
                logicalStepId,
                toolName
        );
    }

    public static String checkpointId(
            String tenantId,
            String missionId,
            String checkpointKind,
            String logicalStepId
    ) {
        return key(
                "checkpoint",
                tenantId,
                missionId,
                checkpointKind,
                logicalStepId
        );
    }

    public static String humanGateId(
            String tenantId,
            String missionId,
            String logicalStepId
    ) {
        return key(
                "human-gate",
                tenantId,
                missionId,
                logicalStepId
        );
    }

    public static String gateDecisionId(
            String tenantId,
            String missionId,
            String gateId,
            String updateId
    ) {
        return key(
                "gate-decision",
                tenantId,
                missionId,
                gateId,
                updateId
        );
    }

    public static String key(String namespace, String... parts) {
        String normalizedNamespace =
                requiredPart("namespace", namespace);

        if (parts == null || parts.length == 0) {
            throw new IllegalArgumentException(
                    "At least one key part is required."
            );
        }

        String[] normalized = Arrays.copyOf(
                parts,
                parts.length + 1
        );

        System.arraycopy(
                normalized,
                0,
                normalized,
                1,
                parts.length
        );

        normalized[0] = normalizedNamespace;

        for (int index = 0; index < parts.length; index++) {
            normalized[index + 1] = requiredPart(
                    "parts[" + index + "]",
                    parts[index]
            );
        }

        return normalizedNamespace
                + ":"
                + Hashing.fingerprint(normalized);
    }

    private static String requiredPart(String name, String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    name + " is required."
            );
        }

        String normalized = value.trim();

        if (normalized.length() > MAX_EXTERNAL_ID_LENGTH) {
            throw new IllegalArgumentException(
                    name + " must not exceed "
                            + MAX_EXTERNAL_ID_LENGTH
                            + " characters."
            );
        }

        return normalized;
    }
}