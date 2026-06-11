package com.sparrowx.internal.valueobjects;

import java.util.UUID;

public record PermissionId(
        String value
) {
    public PermissionId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("permissionId is required");
        }

        value = value.trim();
    }

    public static PermissionId of(String value) {
        return new PermissionId(value);
    }

    public static PermissionId newId() {
        return new PermissionId(UUID.randomUUID().toString());
    }

    @Override
    public String toString() {
        return value;
    }
}