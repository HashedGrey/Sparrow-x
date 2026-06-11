package com.sparrowx.internal.models;

import com.sparrowx.internal.valueobjects.EngineerId;
import com.sparrowx.internal.valueobjects.EngineerOnboardingAssignmentId;
import com.sparrowx.internal.valueobjects.OnboardingAssignmentStatus;
import com.sparrowx.internal.valueobjects.OnboardingPathId;
import com.sparrowx.internal.valueobjects.TenantId;

import java.time.Instant;

public record EngineerOnboardingAssignment(
        EngineerOnboardingAssignmentId assignmentId,
        TenantId tenantId,
        EngineerId engineerId,
        OnboardingPathId onboardingPathId,
        OnboardingAssignmentStatus status,
        Instant assignedAt,
        Instant startedAt,
        Instant completedAt
) {
    public EngineerOnboardingAssignment {
        if (assignmentId == null) {
            throw new IllegalArgumentException("assignmentId is required");
        }

        if (tenantId == null) {
            throw new IllegalArgumentException("tenantId is required");
        }

        if (engineerId == null) {
            throw new IllegalArgumentException("engineerId is required");
        }

        if (onboardingPathId == null) {
            throw new IllegalArgumentException("onboardingPathId is required");
        }

        if (status == null) {
            status = OnboardingAssignmentStatus.ASSIGNED;
        }

        if (assignedAt == null) {
            throw new IllegalArgumentException("assignedAt is required");
        }
    }

    public static EngineerOnboardingAssignment assign(
            EngineerOnboardingAssignmentId assignmentId,
            TenantId tenantId,
            EngineerId engineerId,
            OnboardingPathId onboardingPathId,
            Instant assignedAt
    ) {
        return new EngineerOnboardingAssignment(
                assignmentId,
                tenantId,
                engineerId,
                onboardingPathId,
                OnboardingAssignmentStatus.ASSIGNED,
                assignedAt,
                null,
                null
        );
    }

    public EngineerOnboardingAssignment markInProgress(
            Instant startedAt
    ) {
        return new EngineerOnboardingAssignment(
                assignmentId,
                tenantId,
                engineerId,
                onboardingPathId,
                OnboardingAssignmentStatus.IN_PROGRESS,
                assignedAt,
                startedAt,
                completedAt
        );
    }

    public EngineerOnboardingAssignment markCompleted(
            Instant completedAt
    ) {
        return new EngineerOnboardingAssignment(
                assignmentId,
                tenantId,
                engineerId,
                onboardingPathId,
                OnboardingAssignmentStatus.COMPLETED,
                assignedAt,
                startedAt,
                completedAt
        );
    }
}