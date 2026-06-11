package com.sparrowx.internal.models;

import com.sparrowx.internal.valueobjects.EngineerOnboardingAssignmentId;
import com.sparrowx.internal.valueobjects.EngineerOnboardingTaskProgressId;
import com.sparrowx.internal.valueobjects.OnboardingTaskId;
import com.sparrowx.internal.valueobjects.OnboardingTaskProgressStatus;
import com.sparrowx.internal.valueobjects.TenantId;

import java.time.Instant;

public record EngineerOnboardingTaskProgress(
        EngineerOnboardingTaskProgressId taskProgressId,
        TenantId tenantId,
        EngineerOnboardingAssignmentId assignmentId,
        OnboardingTaskId onboardingTaskId,
        OnboardingTaskProgressStatus status,
        String completionNote,
        Instant startedAt,
        Instant completedAt
) {
    public EngineerOnboardingTaskProgress {
        if (taskProgressId == null) {
            throw new IllegalArgumentException("taskProgressId is required");
        }

        if (tenantId == null) {
            throw new IllegalArgumentException("tenantId is required");
        }

        if (assignmentId == null) {
            throw new IllegalArgumentException("assignmentId is required");
        }

        if (onboardingTaskId == null) {
            throw new IllegalArgumentException("onboardingTaskId is required");
        }

        if (status == null) {
            status = OnboardingTaskProgressStatus.NOT_STARTED;
        }

        if (completionNote == null) {
            completionNote = "";
        }

        completionNote = completionNote.trim();
    }

    public static EngineerOnboardingTaskProgress completed(
            EngineerOnboardingTaskProgressId taskProgressId,
            TenantId tenantId,
            EngineerOnboardingAssignmentId assignmentId,
            OnboardingTaskId onboardingTaskId,
            String completionNote,
            Instant completedAt
    ) {
        return new EngineerOnboardingTaskProgress(
                taskProgressId,
                tenantId,
                assignmentId,
                onboardingTaskId,
                OnboardingTaskProgressStatus.COMPLETED,
                completionNote,
                completedAt,
                completedAt
        );
    }
}