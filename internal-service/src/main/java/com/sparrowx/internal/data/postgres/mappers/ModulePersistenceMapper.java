package com.sparrowx.internal.data.postgres.mappers;

import com.sparrowx.internal.data.postgres.entities.ModuleEntity;
import com.sparrowx.internal.models.Module;
import com.sparrowx.internal.valueobjects.CreatedAt;
import com.sparrowx.internal.valueobjects.ModuleId;
import com.sparrowx.internal.valueobjects.TeamId;
import com.sparrowx.internal.valueobjects.TenantId;
import com.sparrowx.internal.valueobjects.UpdatedAt;

public final class ModulePersistenceMapper {

    private ModulePersistenceMapper() {
    }

    public static ModuleEntity toEntity(Module module) {
        return new ModuleEntity(
                module.moduleId().value(),
                module.tenantId().value(),
                module.name(),
                module.slug(),
                module.description(),
                module.owningTeamId().value(),
                module.createdAt().value(),
                module.updatedAt().value()
        );
    }

    public static Module toDomain(ModuleEntity entity) {
        return new Module(
                ModuleId.of(entity.getModuleId()),
                TenantId.of(entity.getTenantId()),
                entity.getName(),
                entity.getSlug(),
                entity.getDescription(),
                TeamId.of(entity.getOwningTeamId()),
                CreatedAt.of(entity.getCreatedAt()),
                UpdatedAt.of(entity.getUpdatedAt())
        );
    }
}