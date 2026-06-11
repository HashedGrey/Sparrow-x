package com.sparrowx.document.domain.valueobjects;

import java.util.Set;

public record MimeType(String value) {

    private static final Set<String> SUPPORTED_TYPES = Set.of(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "text/plain",
            "image/png",
            "image/jpeg",
            "image/tiff"
    );

    public MimeType {
        requireText(value, "mimeType");

        value = value.toLowerCase();

        if (!SUPPORTED_TYPES.contains(value)) {
            throw new IllegalArgumentException("Unsupported mimeType: " + value);
        }
    }

    public static MimeType of(String value) {
        return new MimeType(value);
    }

    public boolean isPdf() {
        return "application/pdf".equals(value);
    }

    public boolean isDocx() {
        return "application/vnd.openxmlformats-officedocument.wordprocessingml.document".equals(value);
    }

    public boolean isXlsx() {
        return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet".equals(value);
    }

    public boolean isImage() {
        return value.startsWith("image/");
    }

    private static void requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
    }
}