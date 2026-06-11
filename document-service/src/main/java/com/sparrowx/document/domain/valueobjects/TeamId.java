package com.sparrowx.document.domain.valueobjects;

public record TeamId(String value) {

    public TeamId {
        // teamId is optional in your proto/context, so allow null/blank.
        if (value != null && value.isBlank()) {
            value = null;
        }
    }

    public static TeamId of(String value) {
        return new TeamId(value);
    }

    public boolean isPresent() {
        return value != null && !value.isBlank();
    }
}