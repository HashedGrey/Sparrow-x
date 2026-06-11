package com.sparrowx.internal.valueobjects;

import java.util.UUID;

public record OnboardingPathId(
        String value
) {
    public OnboardingPathId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("onboardingPathId is required");
        }

        value = value.trim();
    }

    public static OnboardingPathId of(String value) {
        return new OnboardingPathId(value);
    }

    public static OnboardingPathId newId() {
        return new OnboardingPathId(UUID.randomUUID().toString());
    }

    @Override
    public String toString() {
        return value;
    }
}