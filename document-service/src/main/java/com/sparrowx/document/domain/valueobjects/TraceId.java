package com.sparrowx.document.domain.valueobjects;

public record TraceId(String value) {

    public TraceId {
        if (value != null && value.isBlank()) {
            value = null;
        }
    }

    public static TraceId of(String value) {
        return new TraceId(value);
    }

    public boolean isPresent() {
        return value != null && !value.isBlank();
    }
}