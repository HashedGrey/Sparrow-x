package com.sparrowx.internal.data.postgres.mappers;

import com.sparrowx.internal.data.postgres.entities.EngineerOnboardingTaskProgressEntity;
import com.sparrowx.internal.models.EngineerOnboardingTaskProgress;
import com.sparrowx.internal.valueobjects.EngineerOnboardingAssignmentId;
import com.sparrowx.internal.valueobjects.EngineerOnboardingTaskProgressId;
import com.sparrowx.internal.valueobjects.OnboardingTaskId;
import com.sparrowx.internal.valueobjects.OnboardingTaskProgressStatus;
import com.sparrowx.internal.valueobjects.TenantId;

public final class EngineerOnboardingTaskProgressPersistenceMapper {

    private EngineerOnboardingTaskProgressPersistenceMapper() {
    }

    public static EngineerOnboardingTaskProgressEntity toEntity(
            EngineerOnboardingTaskProgress progress
    ) {
        return new EngineerOnboardingTaskProgressEntity(
                progress.taskProgressId().value(),
                progress.tenantId().value(),
                progress.assignmentId().value(),
                progress.onboardingTaskId().value(),
                progress.status().name(),
                progress.completionNote(),
                progress.startedAt(),
                progress.completedAt()
        );
    }

    public static EngineerOnboardingTaskProgress toDomain(
            EngineerOnboardingTaskProgressEntity entity
    ) {
        return new EngineerOnboardingTaskProgress(
                EngineerOnboardingTaskProgressId.of(entity.getTaskProgressId()),
                TenantId.of(entity.getTenantId()),
                EngineerOnboardingAssignmentId.of(entity.getAssignmentId()),
                OnboardingTaskId.of(entity.getOnboardingTaskId()),
                OnboardingTaskProgressStatus.from(entity.getStatus()),
                entity.getCompletionNote(),
                entity.getStartedAt(),
                entity.getCompletedAt()
        );
    }
}