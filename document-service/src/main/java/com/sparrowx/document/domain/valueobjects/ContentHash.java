package com.sparrowx.document.domain.valueobjects;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public record ContentHash(String value) {

    public ContentHash {
        requireText(value, "contentHash");

        if (!value.matches("^[a-fA-F0-9]{64}$")) {
            throw new IllegalArgumentException("contentHash must be a valid SHA-256 hex value");
        }

        value = value.toLowerCase();
    }

    public static ContentHash of(String value) {
        return new ContentHash(value);
    }

    public static ContentHash sha256(byte[] content) {
        if (content == null || content.length == 0) {
            throw new IllegalArgumentException("content must not be empty");
        }

        return hash(content);
    }

    public static ContentHash sha256(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("text must not be blank");
        }

        return hash(text.getBytes(StandardCharsets.UTF_8));
    }

    private static ContentHash hash(byte[] content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(content);
            return new ContentHash(HexFormat.of().formatHex(hash));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 algorithm is not available", exception);
        }
    }

    private static void requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
    }
}