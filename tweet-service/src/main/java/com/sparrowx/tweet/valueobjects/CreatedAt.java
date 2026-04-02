package com.sparrowx.tweet.valueobjects;

import java.time.Instant;

public record CreatedAt(Instant value) {

    public CreatedAt {
        if (value == null) {
            throw new IllegalArgumentException("CreatedAt cannot be null");
        }
    }

    public static CreatedAt now() {
        return new CreatedAt(Instant.now());
    }

    public Instant getValue() {
        return value;
    }
}