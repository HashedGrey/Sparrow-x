package com.sparrowx.internal.models;

import com.sparrowx.internal.features.getengineeronboardingprogress.OnboardingProgressTaskView;
import com.sparrowx.internal.valueobjects.EngineerId;
import com.sparrowx.internal.valueobjects.EngineerOnboardingAssignmentId;
import com.sparrowx.internal.valueobjects.OnboardingAssignmentStatus;
import com.sparrowx.internal.valueobjects.OnboardingPathId;

import java.util.List;

public record EngineerOnboardingProgress(
        EngineerOnboardingAssignmentId assignmentId,
        EngineerId engineerId,
        OnboardingPathId onboardingPathId,
        OnboardingAssignmentStatus assignmentStatus,
        int totalTasks,
        int completedTasks,
        double completionPercentage,
        List<OnboardingProgressTaskView> tasks
) {
    public EngineerOnboardingProgress {
        if (assignmentId == null) {
            throw new IllegalArgumentException("assignmentId is required");
        }

        if (engineerId == null) {
            throw new IllegalArgumentException("engineerId is required");
        }

        if (onboardingPathId == null) {
            throw new IllegalArgumentException("onboardingPathId is required");
        }

        if (assignmentStatus == null) {
            assignmentStatus = OnboardingAssignmentStatus.ASSIGNED;
        }

        if (totalTasks < 0) {
            throw new IllegalArgumentException("totalTasks must not be negative");
        }

        if (completedTasks < 0) {
            throw new IllegalArgumentException("completedTasks must not be negative");
        }

        if (completedTasks > totalTasks) {
            throw new IllegalArgumentException("completedTasks must not exceed totalTasks");
        }

        tasks = tasks == null ? List.of() : List.copyOf(tasks);
    }

    public static EngineerOnboardingProgress of(
            EngineerOnboardingAssignmentId assignmentId,
            EngineerId engineerId,
            OnboardingPathId onboardingPathId,
            OnboardingAssignmentStatus assignmentStatus,
            int totalTasks,
            int completedTasks,
            List<OnboardingProgressTaskView> tasks
    ) {
        var percentage = totalTasks == 0
                ? 0.0
                : ((double) completedTasks / (double) totalTasks) * 100.0;

        return new EngineerOnboardingProgress(
                assignmentId,
                engineerId,
                onboardingPathId,
                assignmentStatus,
                totalTasks,
                completedTasks,
                percentage,
                tasks
        );
    }
}