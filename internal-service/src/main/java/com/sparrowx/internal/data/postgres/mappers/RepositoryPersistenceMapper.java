package com.sparrowx.internal.data.postgres.mappers;

import com.sparrowx.internal.data.postgres.entities.RepositoryEntity;
import com.sparrowx.internal.models.Repository;
import com.sparrowx.internal.valueobjects.CreatedAt;
import com.sparrowx.internal.valueobjects.ModuleId;
import com.sparrowx.internal.valueobjects.RepositoryId;
import com.sparrowx.internal.valueobjects.TenantId;
import com.sparrowx.internal.valueobjects.UpdatedAt;

public final class RepositoryPersistenceMapper {

    private RepositoryPersistenceMapper() {
    }

    public static RepositoryEntity toEntity(Repository repository) {
        return new RepositoryEntity(
                repository.repositoryId().value(),
                repository.tenantId().value(),
                repository.name(),
                repository.url(),
                repository.moduleId().value(),
                repository.createdAt().value(),
                repository.updatedAt().value()
        );
    }

    public static Repository toDomain(RepositoryEntity entity) {
        return new Repository(
                RepositoryId.of(entity.getRepositoryId()),
                TenantId.of(entity.getTenantId()),
                entity.getName(),
                entity.getUrl(),
                ModuleId.of(entity.getModuleId()),
                CreatedAt.of(entity.getCreatedAt()),
                UpdatedAt.of(entity.getUpdatedAt())
        );
    }
}