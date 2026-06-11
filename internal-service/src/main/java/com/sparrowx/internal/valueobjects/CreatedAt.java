package com.sparrowx.internal.valueobjects;

import java.time.Instant;

public record CreatedAt(
        Instant value
) {
    public CreatedAt {
        if (value == null) {
            throw new IllegalArgumentException("createdAt is required");
        }
    }

    public static CreatedAt now() {
        return new CreatedAt(Instant.now());
    }

    public static CreatedAt of(Instant value) {
        return new CreatedAt(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}