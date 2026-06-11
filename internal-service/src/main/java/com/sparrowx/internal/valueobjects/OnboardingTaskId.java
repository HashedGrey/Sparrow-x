package com.sparrowx.internal.valueobjects;

import java.util.UUID;

public record OnboardingTaskId(
        String value
) {
    public OnboardingTaskId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("onboardingTaskId is required");
        }

        value = value.trim();
    }

    public static OnboardingTaskId of(String value) {
        return new OnboardingTaskId(value);
    }

    public static OnboardingTaskId newId() {
        return new OnboardingTaskId(UUID.randomUUID().toString());
    }

    @Override
    public String toString() {
        return value;
    }
}