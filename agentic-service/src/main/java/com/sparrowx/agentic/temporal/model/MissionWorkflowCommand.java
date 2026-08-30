package com.sparrowx.agentic.temporal.model;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

/** Stable payload for approve, reject and cancel Workflow Updates. */
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
    public MissionWorkflowCommand {
        updateId = requireText(updateId, "updateId");
        type = java.util.Objects.requireNonNull(
                type,
                "type must not be null"
        );
        tenantId = requireText(tenantId, "tenantId");
        missionId = requireText(missionId, "missionId");
        actorUserId = requireText(actorUserId, "actorUserId");
        gateId = normalize(gateId);
        reason = normalize(reason);
        actorRoles = immutableRoles(actorRoles);

        if (type == CommandType.CANCEL) {
            if (!gateId.isEmpty()) {
                throw new IllegalArgumentException(
                        "cancel must not contain gateId"
                );
            }
            reason = requireText(reason, "reason");
        } else {
            gateId = requireText(gateId, "gateId");
            if (type == CommandType.REJECT) {
                reason = requireText(reason, "reason");
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
                    .digest(canonical.getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder(64);
            for (byte value : digest) {
                result.append(String.format("%02x", value & 0xff));
            }
            return result.toString();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "SHA-256 is unavailable",
                    exception
            );
        }
    }

    private static Set<String> immutableRoles(Set<String> values) {
        TreeSet<String> roles = new TreeSet<>();
        if (values != null) {
            values.stream()
                    .map(MissionWorkflowCommand::normalize)
                    .filter(value -> !value.isEmpty())
                    .forEach(roles::add);
        }
        return Collections.unmodifiableSet(roles);
    }

    private static String requireText(String value, String field) {
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

    public enum CommandType {
        APPROVE,
        REJECT,
        CANCEL
    }
}
