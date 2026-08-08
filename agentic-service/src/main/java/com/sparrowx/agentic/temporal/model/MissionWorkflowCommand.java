package com.sparrowx.agentic.temporal.model;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

/**
 * Stable, tenant-scoped payload for a synchronous Temporal Workflow Update.
 */
public record MissionWorkflowCommand(
        String updateId,
        CommandType type,
        String tenantId,
        String missionId,
        String gateId,
        String actorUserId,
        Set<String> actorRoles,
        String reason
) {

    private static final int MAX_ID_LENGTH = 256;
    private static final int MAX_REASON_LENGTH = 4_000;

    public MissionWorkflowCommand {
        updateId = requireText(
                updateId,
                "updateId",
                MAX_ID_LENGTH
        );
        type = java.util.Objects.requireNonNull(
                type,
                "type must not be null"
        );
        tenantId = requireText(
                tenantId,
                "tenantId",
                MAX_ID_LENGTH
        );
        missionId = requireText(
                missionId,
                "missionId",
                MAX_ID_LENGTH
        );
        actorUserId = requireText(
                actorUserId,
                "actorUserId",
                MAX_ID_LENGTH
        );
        gateId = normalize(gateId);
        reason = normalize(reason);
        actorRoles = immutableRoles(actorRoles);

        if (reason.length() > MAX_REASON_LENGTH) {
            throw new IllegalArgumentException(
                    "reason must not exceed "
                            + MAX_REASON_LENGTH
                            + " characters"
            );
        }

        if (type == CommandType.CANCEL) {
            if (!gateId.isEmpty()) {
                throw new IllegalArgumentException(
                        "cancel must not contain gateId"
                );
            }
            if (reason.isEmpty()) {
                throw new IllegalArgumentException(
                        "cancel reason must not be blank"
                );
            }
        } else {
            gateId = requireText(
                    gateId,
                    "gateId",
                    MAX_ID_LENGTH
            );
            if (type == CommandType.REJECT
                    && reason.isEmpty()) {
                throw new IllegalArgumentException(
                        "reject reason must not be blank"
                );
            }
        }
    }

    public static MissionWorkflowCommand approve(
            String updateId,
            String tenantId,
            String missionId,
            String gateId,
            String reviewerUserId,
            Set<String> reviewerRoles,
            String note
    ) {
        return new MissionWorkflowCommand(
                updateId,
                CommandType.APPROVE,
                tenantId,
                missionId,
                gateId,
                reviewerUserId,
                reviewerRoles,
                note
        );
    }

    public static MissionWorkflowCommand reject(
            String updateId,
            String tenantId,
            String missionId,
            String gateId,
            String reviewerUserId,
            Set<String> reviewerRoles,
            String reason
    ) {
        return new MissionWorkflowCommand(
                updateId,
                CommandType.REJECT,
                tenantId,
                missionId,
                gateId,
                reviewerUserId,
                reviewerRoles,
                reason
        );
    }

    public static MissionWorkflowCommand cancel(
            String updateId,
            String tenantId,
            String missionId,
            String actorUserId,
            String reason
    ) {
        return new MissionWorkflowCommand(
                updateId,
                CommandType.CANCEL,
                tenantId,
                missionId,
                "",
                actorUserId,
                Set.of(),
                reason
        );
    }

    /**
     * Canonical content fingerprint used to detect conflicting Update-ID reuse.
     */
    public String fingerprint() {
        String canonical = String.join(
                "\u001f",
                type.name(),
                tenantId,
                missionId,
                gateId,
                actorUserId,
                String.join("\u001e", actorRoles),
                reason
        );

        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(
                            canonical.getBytes(
                                    StandardCharsets.UTF_8
                            )
                    );

            StringBuilder result = new StringBuilder(64);
            for (byte value : digest) {
                result.append(
                        Character.forDigit(
                                (value >>> 4) & 0x0f,
                                16
                        )
                );
                result.append(
                        Character.forDigit(value & 0x0f, 16)
                );
            }
            return result.toString();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "SHA-256 is unavailable",
                    exception
            );
        }
    }

    private static Set<String> immutableRoles(
            Set<String> values
    ) {
        TreeSet<String> normalized = new TreeSet<>();

        if (values != null) {
            for (String value : values) {
                String role = normalize(value);
                if (!role.isEmpty()) {
                    if (role.length() > MAX_ID_LENGTH) {
                        throw new IllegalArgumentException(
                                "actor role is too long"
                        );
                    }
                    normalized.add(role);
                }
            }
        }

        return Collections.unmodifiableSet(normalized);
    }

    private static String requireText(
            String value,
            String field,
            int maximumLength
    ) {
        String normalized = normalize(value);
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(
                    field + " must not be blank"
            );
        }
        if (normalized.length() > maximumLength) {
            throw new IllegalArgumentException(
                    field + " is too long"
            );
        }
        return normalized;
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    public enum CommandType {
        APPROVE,
        REJECT,
        CANCEL
    }
}