package com.sparrowx.document.domain.valueobjects;

import java.util.UUID;

public record RequestId(String value) {

    public RequestId {
        requireText(value, "requestId");
    }

    public static RequestId of(String value) {
        return new RequestId(value);
    }

    public static RequestId newId() {
        return new RequestId(UUID.randomUUID().toString());
    }

    private static void requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
    }
}