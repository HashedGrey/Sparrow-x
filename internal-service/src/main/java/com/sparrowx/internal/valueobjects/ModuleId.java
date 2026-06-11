package com.sparrowx.internal.valueobjects;

import java.util.UUID;

public record ModuleId(
        String value
) {
    public ModuleId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("moduleId is required");
        }

        value = value.trim();
    }

    public static ModuleId of(String value) {
        return new ModuleId(value);
    }

    public static ModuleId newId() {
        return new ModuleId(UUID.randomUUID().toString());
    }

    @Override
    public String toString() {
        return value;
    }
}