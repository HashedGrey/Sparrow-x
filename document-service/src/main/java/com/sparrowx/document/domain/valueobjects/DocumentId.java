package com.sparrowx.document.domain.valueobjects;

import java.util.UUID;

public record DocumentId(String value) {

    public DocumentId {
        requireText(value, "documentId");
    }

    public static DocumentId newId() {
        return new DocumentId(UUID.randomUUID().toString());
    }

    public static DocumentId of(String value) {
        return new DocumentId(value);
    }

    private static void requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
    }
}