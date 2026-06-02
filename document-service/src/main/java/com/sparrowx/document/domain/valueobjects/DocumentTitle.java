package com.sparrowx.document.domain.valueobjects;

public record DocumentTitle(String value) {

    public DocumentTitle {
        if (value != null) {
            value = value.trim();
        }

        if (value != null && value.isBlank()) {
            value = null;
        }

        if (value != null && value.length() > 255) {
            throw new IllegalArgumentException("title must not exceed 255 characters");
        }
    }

    public static DocumentTitle of(String value) {
        return new DocumentTitle(value);
    }

    public boolean isPresent() {
        return value != null && !value.isBlank();
    }

    public String orElse(String fallback) {
        return isPresent() ? value : fallback;
    }
}