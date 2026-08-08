package com.sparrowx.agentic.goals;

import java.util.Map;
import java.util.Set;

public record InternalContextGoal(
        String missionId,
        Set<String> entityNames,
        Set<String> entityTypes,
        int graphDepth,
        boolean includeLearningGraph,
        Map<String, Object> attributes) {

    public InternalContextGoal {
        missionId = requireText(missionId, "missionId");
        entityNames = entityNames == null
                ? Set.of()
                : Set.copyOf(entityNames);
        entityTypes = entityTypes == null
                ? Set.of()
                : Set.copyOf(entityTypes);

        if (graphDepth < 0) {
            throw new IllegalArgumentException(
                    "graphDepth must not be negative");
        }

        attributes = attributes == null
                ? Map.of()
                : Map.copyOf(attributes);
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