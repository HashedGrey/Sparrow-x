package com.sparrowx.tweet.valueobjects;

import java.util.UUID;

public record UserId(UUID value) {

    public UserId {
        if (value == null) {
            throw new IllegalArgumentException("UserId cannot be null or blank");
        }
    }

    public UUID getValue() {
        return value;
    }
}