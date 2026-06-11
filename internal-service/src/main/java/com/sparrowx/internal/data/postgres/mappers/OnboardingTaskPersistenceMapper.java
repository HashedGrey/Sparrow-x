package com.sparrowx.internal.data.postgres.mappers;

import com.sparrowx.internal.data.postgres.entities.OnboardingTaskEntity;
import com.sparrowx.internal.models.OnboardingTask;
import com.sparrowx.internal.valueobjects.CreatedAt;
import com.sparrowx.internal.valueobjects.InternalDocumentId;
import com.sparrowx.internal.valueobjects.OnboardingPathId;
import com.sparrowx.internal.valueobjects.OnboardingTaskId;
import com.sparrowx.internal.valueobjects.RunbookId;
import com.sparrowx.internal.valueobjects.TenantId;
import com.sparrowx.internal.valueobjects.UpdatedAt;

public final class OnboardingTaskPersistenceMapper {

    private OnboardingTaskPersistenceMapper() {
    }

    public static OnboardingTaskEntity toEntity(
            OnboardingTask task
    ) {
        return new OnboardingTaskEntity(
                task.onboardingTaskId().value(),
                task.tenantId().value(),
                task.onboardingPathId().value(),
                task.title(),
                task.description(),
                task.documentId().value(),
                task.runbookId().value(),
                task.sortOrder(),
                task.createdAt().value(),
                task.updatedAt().value()
        );
    }

    public static OnboardingTask toDomain(
            OnboardingTaskEntity entity
    ) {
        return new OnboardingTask(
                OnboardingTaskId.of(entity.getOnboardingTaskId()),
                TenantId.of(entity.getTenantId()),
                OnboardingPathId.of(entity.getOnboardingPathId()),
                entity.getTitle(),
                entity.getDescription(),
                InternalDocumentId.of(entity.getDocumentId()),
                RunbookId.of(entity.getRunbookId()),
                entity.getSortOrder(),
                CreatedAt.of(entity.getCreatedAt()),
                UpdatedAt.of(entity.getUpdatedAt())
        );
    }
}