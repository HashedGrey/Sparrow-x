package com.sparrowx.internal.data.postgres.mappers;

import com.sparrowx.internal.data.postgres.entities.OnboardingPathEntity;
import com.sparrowx.internal.models.OnboardingPath;
import com.sparrowx.internal.valueobjects.CreatedAt;
import com.sparrowx.internal.valueobjects.ModuleId;
import com.sparrowx.internal.valueobjects.OnboardingPathId;
import com.sparrowx.internal.valueobjects.TenantId;
import com.sparrowx.internal.valueobjects.UpdatedAt;

public final class OnboardingPathPersistenceMapper {

    private OnboardingPathPersistenceMapper() {
    }

    public static OnboardingPathEntity toEntity(
            OnboardingPath onboardingPath
    ) {
        return new OnboardingPathEntity(
                onboardingPath.onboardingPathId().value(),
                onboardingPath.tenantId().value(),
                onboardingPath.name(),
                onboardingPath.slug(),
                onboardingPath.description(),
                onboardingPath.targetModuleId().value(),
                onboardingPath.createdAt().value(),
                onboardingPath.updatedAt().value()
        );
    }

    public static OnboardingPath toDomain(
            OnboardingPathEntity entity
    ) {
        return new OnboardingPath(
                OnboardingPathId.of(entity.getOnboardingPathId()),
                TenantId.of(entity.getTenantId()),
                entity.getName(),
                entity.getSlug(),
                entity.getDescription(),
                ModuleId.of(entity.getTargetModuleId()),
                CreatedAt.of(entity.getCreatedAt()),
                UpdatedAt.of(entity.getUpdatedAt())
        );
    }
}