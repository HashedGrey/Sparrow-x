package com.sparrowx.internal.valueobjects;

import java.time.Instant;

public record UpdatedAt(
        Instant value
) {
    public UpdatedAt {
        if (value == null) {
            throw new IllegalArgumentException("updatedAt is required");
        }
    }

    public static UpdatedAt now() {
        return new UpdatedAt(Instant.now());
    }

    public static UpdatedAt of(Instant value) {
        return new UpdatedAt(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}