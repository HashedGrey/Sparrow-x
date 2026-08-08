package com.sparrowx.agentic.goals;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

public record HumanReviewGoal(
        String missionId,
        String title,
        String reason,
        Set<String> requiredReviewerRoles,
        Map<String, Object> reviewPayload,
        Instant expiresAt) {

    public HumanReviewGoal {
        missionId = requireText(missionId, "missionId");
        title = requireText(title, "title");
        reason = requireText(reason, "reason");
        requiredReviewerRoles = requiredReviewerRoles == null
                ? Set.of()
                : Set.copyOf(requiredReviewerRoles);
        reviewPayload = reviewPayload == null
                ? Map.of()
                : Map.copyOf(reviewPayload);
    }

    private static String requireText(
            String value,
            String field) {

        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    field + " must not be blank");
        }

        return value;
    }
}