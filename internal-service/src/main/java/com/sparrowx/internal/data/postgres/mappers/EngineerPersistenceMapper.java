package com.sparrowx.internal.data.postgres.mappers;

import com.sparrowx.internal.data.postgres.entities.EngineerEntity;
import com.sparrowx.internal.models.Engineer;
import com.sparrowx.internal.valueobjects.CreatedAt;
import com.sparrowx.internal.valueobjects.EmailAddress;
import com.sparrowx.internal.valueobjects.EngineerId;
import com.sparrowx.internal.valueobjects.EngineerRole;
import com.sparrowx.internal.valueobjects.TenantId;
import com.sparrowx.internal.valueobjects.UpdatedAt;

public final class EngineerPersistenceMapper {

    private EngineerPersistenceMapper() {
    }

    public static EngineerEntity toEntity(Engineer engineer) {
        return new EngineerEntity(
                engineer.engineerId().value(),
                engineer.tenantId().value(),
                engineer.fullName(),
                engineer.email().value(),
                engineer.role().name(),
                engineer.createdAt().value(),
                engineer.updatedAt().value()
        );
    }

    public static Engineer toDomain(EngineerEntity entity) {
        return new Engineer(
                EngineerId.of(entity.getEngineerId()),
                TenantId.of(entity.getTenantId()),
                entity.getFullName(),
                EmailAddress.of(entity.getEmail()),
                EngineerRole.from(entity.getRole()),
                CreatedAt.of(entity.getCreatedAt()),
                UpdatedAt.of(entity.getUpdatedAt())
        );
    }
}