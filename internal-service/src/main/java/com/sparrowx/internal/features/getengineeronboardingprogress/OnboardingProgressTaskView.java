package com.sparrowx.internal.features.getengineeronboardingprogress;

import com.sparrowx.internal.valueobjects.OnboardingTaskId;
import com.sparrowx.internal.valueobjects.OnboardingTaskProgressStatus;

import java.time.Instant;

public record OnboardingProgressTaskView(
        OnboardingTaskId onboardingTaskId,
        String title,
        String description,
        OnboardingTaskProgressStatus status,
        int sortOrder,
        Instant completedAt
) {
    public OnboardingProgressTaskView {
        if (onboardingTaskId == null) {
            throw new IllegalArgumentException("onboardingTaskId is required");
        }

        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("title is required");
        }

        if (description == null) {
            description = "";
        }

        if (status == null) {
            status = OnboardingTaskProgressStatus.NOT_STARTED;
        }

        title = title.trim();
        description = description.trim();
    }
}