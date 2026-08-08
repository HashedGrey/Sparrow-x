package com.sparrowx.agentic.goals;

import com.sparrowx.agentic.mission.model.MissionPath;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

public record MissionGoal(
        String missionId,
        String objective,
        MissionPath selectedPath,
        Set<String> requiredGoalTypes,
        Map<String, Object> attributes) {

    public MissionGoal {
        missionId = requireText(missionId, "missionId");
        objective = requireText(objective, "objective");
        selectedPath = Objects.requireNonNull(
                selectedPath,
                "selectedPath must not be null");
        requiredGoalTypes = requiredGoalTypes == null
                ? Set.of()
                : Set.copyOf(requiredGoalTypes);
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