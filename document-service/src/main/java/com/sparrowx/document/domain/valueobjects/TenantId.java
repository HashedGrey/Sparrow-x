package com.sparrowx.document.domain.valueobjects;

public record TenantId(String value) {

    public TenantId {
        requireText(value, "tenantId");
    }

    public static TenantId of(String value) {
        return new TenantId(value);
    }

    private static void requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
    }
}