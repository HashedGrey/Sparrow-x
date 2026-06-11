package com.sparrowx.internal.valueobjects;

import java.util.UUID;

public record EngineerOnboardingAssignmentId(
        String value
) {
    public EngineerOnboardingAssignmentId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("assignmentId is required");
        }

        value = value.trim();
    }

    public static EngineerOnboardingAssignmentId of(String value) {
        return new EngineerOnboardingAssignmentId(value);
    }

    public static EngineerOnboardingAssignmentId newId() {
        return new EngineerOnboardingAssignmentId(UUID.randomUUID().toString());
    }

    @Override
    public String toString() {
        return value;
    }
}