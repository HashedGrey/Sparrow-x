package com.sparrowx.document.domain.valueobjects;

public record SearchQueryText(String value) {

    public SearchQueryText {
        requireText(value, "query");

        value = value.trim();

        if (value.length() > 2_000) {
            throw new IllegalArgumentException("query must not exceed 2000 characters");
        }
    }

    public static SearchQueryText of(String value) {
        return new SearchQueryText(value);
    }

    private static void requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
    }
}