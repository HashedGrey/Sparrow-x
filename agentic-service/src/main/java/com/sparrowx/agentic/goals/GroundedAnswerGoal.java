package com.sparrowx.agentic.goals;

import java.util.List;
import java.util.Map;

public record GroundedAnswerGoal(
        String missionId,
        List<String> requiredSections,
        boolean citationsRequired,
        double minimumConfidence,
        Map<String, Object> attributes) {

    public GroundedAnswerGoal {
        missionId = requireText(missionId, "missionId");
        requiredSections = requiredSections == null
                ? List.of()
                : List.copyOf(requiredSections);

        requireScore(minimumConfidence, "minimumConfidence");

        attributes = attributes == null
                ? Map.of()
                : Map.copyOf(attributes);
    }

    private static void requireScore(
            double value,
            String field) {

        if (!Double.isFinite(value)
                || value < 0.0
                || value > 1.0) {
            throw new IllegalArgumentException(
                    field + " must be between 0.0 and 1.0");
        }
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