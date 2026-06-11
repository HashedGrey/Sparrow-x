package com.sparrowx.internal.valueobjects;

public record TenantId(
        String value
) {
    public TenantId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("tenantId is required");
        }

        value = value.trim();
    }

    public static TenantId of(String value) {
        return new TenantId(value);
    }

    @Override
    public String toString() {
        return value;
    }
}