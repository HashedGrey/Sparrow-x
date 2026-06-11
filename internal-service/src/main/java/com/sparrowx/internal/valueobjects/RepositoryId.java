package com.sparrowx.internal.valueobjects;

import java.util.UUID;

public record RepositoryId(
        String value
) {
    public RepositoryId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("repositoryId is required");
        }

        value = value.trim();
    }

    public static RepositoryId of(String value) {
        return new RepositoryId(value);
    }

    public static RepositoryId newId() {
        return new RepositoryId(UUID.randomUUID().toString());
    }

    @Override
    public String toString() {
        return value;
    }
}