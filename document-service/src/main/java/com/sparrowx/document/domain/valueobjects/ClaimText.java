package com.sparrowx.document.domain.valueobjects;

public record ClaimText(String value) {

    public ClaimText {
        requireText(value, "claim");

        value = value.trim();

        if (value.length() > 4_000) {
            throw new IllegalArgumentException("claim must not exceed 4000 characters");
        }
    }

    public static ClaimText of(String value) {
        return new ClaimText(value);
    }

    private static void requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
    }
}