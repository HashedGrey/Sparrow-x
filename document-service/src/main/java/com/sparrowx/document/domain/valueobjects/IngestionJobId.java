package com.sparrowx.document.domain.valueobjects;

import java.util.UUID;

public record IngestionJobId(String value) {

    public IngestionJobId {
        requireText(value, "ingestionJobId");
    }

    public static IngestionJobId newId() {
        return new IngestionJobId(UUID.randomUUID().toString());
    }

    public static IngestionJobId of(String value) {
        return new IngestionJobId(value);
    }

    private static void requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
    }
}