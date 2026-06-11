package com.sparrowx.document.domain.valueobjects;

public record FileName(String value) {

    public FileName {
        requireText(value, "fileName");

        if (value.contains("..") || value.contains("/") || value.contains("\\")) {
            throw new IllegalArgumentException("fileName must not contain path traversal or path separators");
        }
    }

    public static FileName of(String value) {
        return new FileName(value);
    }

    public String extension() {
        int dotIndex = value.lastIndexOf('.');

        if (dotIndex < 0 || dotIndex == value.length() - 1) {
            return "";
        }

        return value.substring(dotIndex + 1).toLowerCase();
    }

    private static void requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
    }
}