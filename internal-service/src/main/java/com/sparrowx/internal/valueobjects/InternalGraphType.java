package com.sparrowx.internal.valueobjects;

public enum InternalGraphType {
    COMPANY,
    LEARNING;

    public static InternalGraphType from(String value) {
        if (value == null || value.isBlank()) {
            return COMPANY;
        }

        return switch (value.trim().toUpperCase()) {
            case "INTERNAL_GRAPH_TYPE_COMPANY", "COMPANY" -> COMPANY;
            case "INTERNAL_GRAPH_TYPE_LEARNING", "LEARNING" -> LEARNING;
            default -> COMPANY;
        };
    }
}