package com.sparrowx.agentic.runtime.gate;

import com.sparrowx.agentic.mission.model.HumanGateState;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

public record HumanGate(
        String gateId,
        String tenantId,
        String missionId,
        String title,
        String reason,
        List<String> requiredReviewerRoles,
        Map<String, Object> reviewPayload,
        HumanGateStatus status,
        String decidedByUserId,
        String decisionNote,
        Instant createdAt,
        Instant expiresAt,
        Instant decidedAt
) {

    public HumanGate {
        gateId = requireText(gateId, "gateId");
        tenantId = requireText(tenantId, "tenantId");
        missionId = requireText(missionId, "missionId");
        title = requireText(title, "title");
        reason = requireText(reason, "reason");
        requiredReviewerRoles = normalizeRoles(requiredReviewerRoles);
        reviewPayload = immutablePayload(reviewPayload);
        status = Objects.requireNonNull(
                status,
                "status must not be null"
        );
        decidedByUserId = normalizeOptionalText(decidedByUserId);
        decisionNote = normalizeOptionalText(decisionNote);
        createdAt = Objects.requireNonNull(
                createdAt,
                "createdAt must not be null"
        );
        expiresAt = Objects.requireNonNull(
                expiresAt,
                "expiresAt must not be null"
        );

        if (!createdAt.isBefore(expiresAt)) {
            throw new IllegalArgumentException(
                    "HUMAN_GATE_INVALID_EXPIRY: "
                            + "expiresAt must be after createdAt"
            );
        }

        switch (status) {
            case OPEN -> validateOpenState(
                    decidedByUserId,
                    decisionNote,
                    decidedAt
            );
            case APPROVED -> validateReviewerDecision(
                    decidedByUserId,
                    decidedAt,
                    createdAt,
                    expiresAt,
                    "APPROVED"
            );
            case REJECTED -> {
                validateReviewerDecision(
                        decidedByUserId,
                        decidedAt,
                        createdAt,
                        expiresAt,
                        "REJECTED"
                );
                if (decisionNote.isBlank()) {
                    throw new IllegalArgumentException(
                            "HUMAN_GATE_REJECTION_REASON_REQUIRED"
                    );
                }
            }
            case EXPIRED -> validateExpiredState(
                    decidedByUserId,
                    decisionNote,
                    decidedAt,
                    expiresAt
            );
        }
    }

    public static HumanGate open(
            String gateId,
            String tenantId,
            String missionId,
            String title,
            String reason,
            List<String> requiredReviewerRoles,
            Map<String, Object> reviewPayload,
            Instant createdAt,
            Instant expiresAt
    ) {
        return new HumanGate(
                gateId,
                tenantId,
                missionId,
                title,
                reason,
                requiredReviewerRoles,
                reviewPayload,
                HumanGateStatus.OPEN,
                "",
                "",
                createdAt,
                expiresAt,
                null
        );
    }

    public HumanGate approve(
            String reviewerUserId,
            Set<String> reviewerRoles,
            String note,
            Instant approvedAt
    ) {
        String normalizedReviewer = requireText(
                reviewerUserId,
                "reviewerUserId"
        );
        validateReviewerRoles(reviewerRoles);

        String normalizedNote = normalizeOptionalText(note);
        Objects.requireNonNull(
                approvedAt,
                "approvedAt must not be null"
        );

        if (status == HumanGateStatus.APPROVED) {
            if (decidedByUserId.equals(normalizedReviewer)
                    && decisionNote.equals(normalizedNote)) {
                return this;
            }
            throw conflictingDecision(HumanGateStatus.APPROVED);
        }

        requireOpen(HumanGateStatus.APPROVED);
        requireBeforeExpiry(approvedAt, "approvedAt");

        return terminal(
                HumanGateStatus.APPROVED,
                normalizedReviewer,
                normalizedNote,
                approvedAt
        );
    }

    public HumanGate reject(
            String reviewerUserId,
            Set<String> reviewerRoles,
            String rejectionReason,
            Instant rejectedAt
    ) {
        String normalizedReviewer = requireText(
                reviewerUserId,
                "reviewerUserId"
        );
        validateReviewerRoles(reviewerRoles);

        String normalizedReason = requireText(
                rejectionReason,
                "rejectionReason"
        );
        Objects.requireNonNull(
                rejectedAt,
                "rejectedAt must not be null"
        );

        if (status == HumanGateStatus.REJECTED) {
            if (decidedByUserId.equals(normalizedReviewer)
                    && decisionNote.equals(normalizedReason)) {
                return this;
            }
            throw conflictingDecision(HumanGateStatus.REJECTED);
        }

        requireOpen(HumanGateStatus.REJECTED);
        requireBeforeExpiry(rejectedAt, "rejectedAt");

        return terminal(
                HumanGateStatus.REJECTED,
                normalizedReviewer,
                normalizedReason,
                rejectedAt
        );
    }

    public HumanGate expire(Instant expiredAt) {
        Objects.requireNonNull(
                expiredAt,
                "expiredAt must not be null"
        );

        if (status == HumanGateStatus.EXPIRED) {
            return this;
        }

        requireOpen(HumanGateStatus.EXPIRED);
        if (expiredAt.isBefore(expiresAt)) {
            throw new IllegalStateException(
                    "HUMAN_GATE_NOT_EXPIRED: expiredAt precedes expiresAt"
            );
        }

        return terminal(
                HumanGateStatus.EXPIRED,
                "",
                "",
                expiredAt
        );
    }

    public boolean isTerminal() {
        return status != HumanGateStatus.OPEN;
    }

    public boolean isExpiredAt(Instant instant) {
        Objects.requireNonNull(
                instant,
                "instant must not be null"
        );
        return !instant.isBefore(expiresAt);
    }

    public HumanGateState toPublicState() {
        return new HumanGateState(
                gateId,
                missionId,
                title,
                reason,
                requiredReviewerRoles,
                reviewPayload,
                createdAt,
                expiresAt
        );
    }

    private HumanGate terminal(
            HumanGateStatus terminalStatus,
            String reviewerUserId,
            String note,
            Instant terminalAt
    ) {
        return new HumanGate(
                gateId,
                tenantId,
                missionId,
                title,
                reason,
                requiredReviewerRoles,
                reviewPayload,
                terminalStatus,
                reviewerUserId,
                note,
                createdAt,
                expiresAt,
                terminalAt
        );
    }

    private void requireOpen(HumanGateStatus requestedStatus) {
        if (status != HumanGateStatus.OPEN) {
            throw conflictingDecision(requestedStatus);
        }
    }

    private void requireBeforeExpiry(
            Instant decisionAt,
            String field
    ) {
        if (decisionAt.isBefore(createdAt)) {
            throw new IllegalArgumentException(
                    "HUMAN_GATE_INVALID_DECISION_TIME: "
                            + field + " precedes createdAt"
            );
        }

        if (!decisionAt.isBefore(expiresAt)) {
            throw new IllegalStateException(
                    "HUMAN_GATE_EXPIRED: reviewer decision is not allowed "
                            + "at or after expiresAt"
            );
        }
    }

    private void validateReviewerRoles(
            Collection<String> reviewerRoles
    ) {
        Set<String> normalizedReviewerRoles = new TreeSet<>(
                normalizeRoles(
                        reviewerRoles == null
                                ? List.of()
                                : reviewerRoles
                )
        );

        if (!requiredReviewerRoles.isEmpty()
                && Collections.disjoint(
                requiredReviewerRoles,
                normalizedReviewerRoles
        )) {
            throw new IllegalArgumentException(
                    "HUMAN_GATE_REVIEWER_ROLE_REQUIRED"
            );
        }
    }

    private IllegalStateException conflictingDecision(
            HumanGateStatus requestedStatus
    ) {
        return new IllegalStateException(
                "HUMAN_GATE_CONFLICTING_TERMINAL_DECISION: gate "
                        + gateId + " is " + status
                        + " and cannot become " + requestedStatus
        );
    }

    private static void validateOpenState(
            String reviewerUserId,
            String note,
            Instant decidedAt
    ) {
        if (!reviewerUserId.isBlank()
                || !note.isBlank()
                || decidedAt != null) {
            throw new IllegalArgumentException(
                    "HUMAN_GATE_INVALID_OPEN_STATE"
            );
        }
    }

    private static void validateReviewerDecision(
            String reviewerUserId,
            Instant decidedAt,
            Instant createdAt,
            Instant expiresAt,
            String status
    ) {
        if (reviewerUserId.isBlank()) {
            throw new IllegalArgumentException(
                    "HUMAN_GATE_" + status + "_REVIEWER_REQUIRED"
            );
        }

        if (decidedAt == null) {
            throw new IllegalArgumentException(
                    "HUMAN_GATE_" + status + "_TIMESTAMP_REQUIRED"
            );
        }

        if (decidedAt.isBefore(createdAt)
                || !decidedAt.isBefore(expiresAt)) {
            throw new IllegalArgumentException(
                    "HUMAN_GATE_" + status + "_TIMESTAMP_INVALID"
            );
        }
    }

    private static void validateExpiredState(
            String reviewerUserId,
            String note,
            Instant decidedAt,
            Instant expiresAt
    ) {
        if (!reviewerUserId.isBlank() || !note.isBlank()) {
            throw new IllegalArgumentException(
                    "HUMAN_GATE_EXPIRED_STATE_MUST_NOT_NAME_A_REVIEWER"
            );
        }

        if (decidedAt == null || decidedAt.isBefore(expiresAt)) {
            throw new IllegalArgumentException(
                    "HUMAN_GATE_EXPIRED_TIMESTAMP_INVALID"
            );
        }
    }

    private static List<String> normalizeRoles(
            Collection<String> roles
    ) {
        if (roles == null || roles.isEmpty()) {
            return List.of();
        }

        TreeSet<String> normalized = new TreeSet<>();
        for (String role : roles) {
            String value = requireText(
                    role,
                    "requiredReviewerRole"
            ).toLowerCase(Locale.ROOT);
            normalized.add(value);
        }

        return List.copyOf(normalized);
    }

    private static Map<String, Object> immutablePayload(
            Map<String, Object> payload
    ) {
        if (payload == null || payload.isEmpty()) {
            return Map.of();
        }

        List<Map.Entry<String, Object>> entries =
                new ArrayList<>(payload.entrySet());
        entries.sort(Comparator.comparing(entry ->
                requireText(entry.getKey(), "reviewPayload key")
        ));

        Map<String, Object> copy = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : entries) {
            copy.put(
                    requireText(
                            entry.getKey(),
                            "reviewPayload key"
                    ),
                    immutableValue(entry.getValue())
            );
        }

        return Collections.unmodifiableMap(copy);
    }

    private static Object immutableValue(Object value) {
        if (value == null
                || value instanceof String
                || value instanceof Number
                || value instanceof Boolean) {
            return value;
        }

        if (value instanceof Map<?, ?> nestedMap) {
            Map<String, Object> typed = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : nestedMap.entrySet()) {
                if (!(entry.getKey() instanceof String key)) {
                    throw new IllegalArgumentException(
                            "HUMAN_GATE_PAYLOAD_KEYS_MUST_BE_STRINGS"
                    );
                }
                typed.put(key, entry.getValue());
            }
            return immutablePayload(typed);
        }

        if (value instanceof Collection<?> collection) {
            List<Object> copy = new ArrayList<>(collection.size());
            for (Object item : collection) {
                copy.add(immutableValue(item));
            }
            return Collections.unmodifiableList(copy);
        }

        throw new IllegalArgumentException(
                "HUMAN_GATE_PAYLOAD_VALUE_NOT_JSON_SAFE: "
                        + value.getClass().getName()
        );
    }

    private static String normalizeOptionalText(String value) {
        return value == null ? "" : value.trim();
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    "HUMAN_GATE_INVALID_IDENTITY: "
                            + field + " must not be blank"
            );
        }
        return value.trim();
    }
}