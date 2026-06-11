package com.sparrowx.document.domain.valueobjects;

public record UserId(String value) {

    public UserId {
        requireText(value, "userId");
    }

    public static UserId of(String value) {
        return new UserId(value);
    }

    private static void requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
    }
}