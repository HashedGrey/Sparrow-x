package com.sparrowx.internal.valueobjects;

import java.util.UUID;

public record EngineerId(
        String value
) {
    public EngineerId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("engineerId is required");
        }

        value = value.trim();
    }

    public static EngineerId of(String value) {
        return new EngineerId(value);
    }

    public static EngineerId newId() {
        return new EngineerId(UUID.randomUUID().toString());
    }

    @Override
    public String toString() {
        return value;
    }
}