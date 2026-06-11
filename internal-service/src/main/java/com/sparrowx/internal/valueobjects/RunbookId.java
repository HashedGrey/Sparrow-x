package com.sparrowx.internal.valueobjects;

import java.util.UUID;

public record RunbookId(
        String value
) {
    public RunbookId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("runbookId is required");
        }

        value = value.trim();
    }

    public static RunbookId of(String value) {
        return new RunbookId(value);
    }

    public static RunbookId newId() {
        return new RunbookId(UUID.randomUUID().toString());
    }

    @Override
    public String toString() {
        return value;
    }
}