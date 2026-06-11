package com.sparrowx.document.domain.valueobjects;

import java.util.UUID;

public record ChunkId(String value) {

    public ChunkId {
        requireText(value, "chunkId");
    }

    public static ChunkId newId() {
        return new ChunkId(UUID.randomUUID().toString());
    }

    public static ChunkId of(String value) {
        return new ChunkId(value);
    }

    private static void requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
    }
}