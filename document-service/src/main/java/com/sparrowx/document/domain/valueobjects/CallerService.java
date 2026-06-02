package com.sparrowx.document.domain.valueobjects;

public record CallerService(String value) {

    public CallerService {
        if (value != null && value.isBlank()) {
            value = null;
        }
    }

    public static CallerService of(String value) {
        return new CallerService(value);
    }

    public boolean isPresent() {
        return value != null && !value.isBlank();
    }
}