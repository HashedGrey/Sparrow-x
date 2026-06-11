package com.sparrowx.internal.data.postgres.mappers;

import com.sparrowx.internal.data.postgres.entities.RunbookEntity;
import com.sparrowx.internal.models.Runbook;
import com.sparrowx.internal.valueobjects.CreatedAt;
import com.sparrowx.internal.valueobjects.InternalDocumentId;
import com.sparrowx.internal.valueobjects.ModuleId;
import com.sparrowx.internal.valueobjects.RunbookId;
import com.sparrowx.internal.valueobjects.TenantId;
import com.sparrowx.internal.valueobjects.UpdatedAt;

public final class RunbookPersistenceMapper {

    private RunbookPersistenceMapper() {
    }

    public static RunbookEntity toEntity(Runbook runbook) {
        return new RunbookEntity(
                runbook.runbookId().value(),
                runbook.tenantId().value(),
                runbook.title(),
                runbook.slug(),
                runbook.summary(),
                runbook.moduleId().value(),
                runbook.documentId().value(),
                runbook.createdAt().value(),
                runbook.updatedAt().value()
        );
    }

    public static Runbook toDomain(RunbookEntity entity) {
        return new Runbook(
                RunbookId.of(entity.getRunbookId()),
                TenantId.of(entity.getTenantId()),
                entity.getTitle(),
                entity.getSlug(),
                entity.getSummary(),
                ModuleId.of(entity.getModuleId()),
                InternalDocumentId.of(entity.getDocumentId()),
                CreatedAt.of(entity.getCreatedAt()),
                UpdatedAt.of(entity.getUpdatedAt())
        );
    }
}