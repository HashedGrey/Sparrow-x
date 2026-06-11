package com.sparrowx.document.domain.valueobjects;

public record ProjectId(String value) {

    public ProjectId {
        // projectId is optional in your proto/context, so allow null/blank.
        if (value != null && value.isBlank()) {
            value = null;
        }
    }

    public static ProjectId of(String value) {
        return new ProjectId(value);
    }

    public boolean isPresent() {
        return value != null && !value.isBlank();
    }
}