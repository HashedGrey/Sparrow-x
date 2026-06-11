package com.sparrowx.internal.valueobjects;

import java.util.UUID;

public record TeamId(
        String value
) {
    public TeamId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("teamId is required");
        }

        value = value.trim();
    }

    public static TeamId of(String value) {
        return new TeamId(value);
    }

    public static TeamId newId() {
        return new TeamId(UUID.randomUUID().toString());
    }

    @Override
    public String toString() {
        return value;
    }
}