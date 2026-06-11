package com.sparrowx.internal.data.postgres.mappers;

import com.sparrowx.internal.data.postgres.entities.InternalDocumentEntity;
import com.sparrowx.internal.models.InternalDocument;
import com.sparrowx.internal.valueobjects.CreatedAt;
import com.sparrowx.internal.valueobjects.InternalDocumentId;
import com.sparrowx.internal.valueobjects.ModuleId;
import com.sparrowx.internal.valueobjects.RepositoryId;
import com.sparrowx.internal.valueobjects.TenantId;
import com.sparrowx.internal.valueobjects.UpdatedAt;

public final class InternalDocumentPersistenceMapper {

    private InternalDocumentPersistenceMapper() {
    }

    public static InternalDocumentEntity toEntity(
            InternalDocument document
    ) {
        return new InternalDocumentEntity(
                document.documentId().value(),
                document.tenantId().value(),
                document.title(),
                document.slug(),
                document.summary(),
                document.moduleId().value(),
                document.repositoryId().value(),
                document.externalRef(),
                document.createdAt().value(),
                document.updatedAt().value()
        );
    }

    public static InternalDocument toDomain(
            InternalDocumentEntity entity
    ) {
        return new InternalDocument(
                InternalDocumentId.of(entity.getDocumentId()),
                TenantId.of(entity.getTenantId()),
                entity.getTitle(),
                entity.getSlug(),
                entity.getSummary(),
                ModuleId.of(entity.getModuleId()),
                RepositoryId.of(entity.getRepositoryId()),
                entity.getExternalRef(),
                CreatedAt.of(entity.getCreatedAt()),
                UpdatedAt.of(entity.getUpdatedAt())
        );
    }
}