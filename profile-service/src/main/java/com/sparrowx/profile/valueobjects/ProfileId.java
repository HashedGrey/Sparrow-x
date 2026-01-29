package com.sparrowx.profile.valueobjects;

import java.util.UUID;

public record ProfileId(UUID value) {

    public static ProfileId newId() {
        return new ProfileId(UUID.randomUUID());
    }

    public String toString() {
        return value.toString();
    }
}
