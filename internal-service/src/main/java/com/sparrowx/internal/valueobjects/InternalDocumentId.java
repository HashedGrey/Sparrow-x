package com.sparrowx.internal.valueobjects;

import java.util.UUID;

public record InternalDocumentId(
        String value
) {
    public InternalDocumentId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("documentId is required");
        }

        value = value.trim();
    }

    public static InternalDocumentId of(String value) {
        return new InternalDocumentId(value);
    }

    public static InternalDocumentId newId() {
        return new InternalDocumentId(UUID.randomUUID().toString());
    }

    @Override
    public String toString() {
        return value;
    }
}