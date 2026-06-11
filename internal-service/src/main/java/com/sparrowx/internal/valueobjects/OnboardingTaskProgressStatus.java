package com.sparrowx.internal.valueobjects;

public enum OnboardingTaskProgressStatus {
    NOT_STARTED,
    IN_PROGRESS,
    COMPLETED,
    SKIPPED;

    public static OnboardingTaskProgressStatus from(String value) {
        if (value == null || value.isBlank()) {
            return NOT_STARTED;
        }

        return switch (value.trim().toUpperCase()) {
            case "ONBOARDING_TASK_PROGRESS_STATUS_NOT_STARTED", "NOT_STARTED" -> NOT_STARTED;
            case "ONBOARDING_TASK_PROGRESS_STATUS_IN_PROGRESS", "IN_PROGRESS" -> IN_PROGRESS;
            case "ONBOARDING_TASK_PROGRESS_STATUS_COMPLETED", "COMPLETED" -> COMPLETED;
            case "ONBOARDING_TASK_PROGRESS_STATUS_SKIPPED", "SKIPPED" -> SKIPPED;
            default -> NOT_STARTED;
        };
    }
}