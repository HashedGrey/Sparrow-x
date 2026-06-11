package com.sparrowx.internal.valueobjects;

import java.util.UUID;

public record EngineerOnboardingTaskProgressId(
        String value
) {
    public EngineerOnboardingTaskProgressId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("taskProgressId is required");
        }

        value = value.trim();
    }

    public static EngineerOnboardingTaskProgressId of(String value) {
        return new EngineerOnboardingTaskProgressId(value);
    }

    public static EngineerOnboardingTaskProgressId newId() {
        return new EngineerOnboardingTaskProgressId(UUID.randomUUID().toString());
    }

    @Override
    public String toString() {
        return value;
    }
}