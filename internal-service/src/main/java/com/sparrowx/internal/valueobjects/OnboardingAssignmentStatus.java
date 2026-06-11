package com.sparrowx.internal.valueobjects;

public enum OnboardingAssignmentStatus {
    ASSIGNED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED;

    public static OnboardingAssignmentStatus from(String value) {
        if (value == null || value.isBlank()) {
            return ASSIGNED;
        }

        return switch (value.trim().toUpperCase()) {
            case "ONBOARDING_ASSIGNMENT_STATUS_ASSIGNED", "ASSIGNED" -> ASSIGNED;
            case "ONBOARDING_ASSIGNMENT_STATUS_IN_PROGRESS", "IN_PROGRESS" -> IN_PROGRESS;
            case "ONBOARDING_ASSIGNMENT_STATUS_COMPLETED", "COMPLETED" -> COMPLETED;
            case "ONBOARDING_ASSIGNMENT_STATUS_CANCELLED", "CANCELLED" -> CANCELLED;
            default -> ASSIGNED;
        };
    }
}