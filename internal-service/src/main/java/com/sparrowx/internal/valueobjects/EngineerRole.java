package com.sparrowx.internal.valueobjects;

public enum EngineerRole {
    LEARNER,
    ENGINEER,
    AGENTIC_ENGINEER,
    ADMIN;

    public static EngineerRole from(String value) {
        if (value == null || value.isBlank()) {
            return LEARNER;
        }

        return switch (value.trim().toUpperCase()) {
            case "ENGINEER_ROLE_LEARNER", "LEARNER" -> LEARNER;
            case "ENGINEER_ROLE_ENGINEER", "ENGINEER" -> ENGINEER;
            case "ENGINEER_ROLE_AGENTIC_ENGINEER", "AGENTIC_ENGINEER" -> AGENTIC_ENGINEER;
            case "ENGINEER_ROLE_ADMIN", "ADMIN" -> ADMIN;
            default -> LEARNER;
        };
    }
}