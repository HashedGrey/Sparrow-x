package com.sparrowx.document.data;

import com.sparrowx.document.data.postgres.entities.DocumentEntity;
import com.sparrowx.document.data.postgres.entities.IngestionJobEntity;
import com.sparrowx.document.domain.models.Document;
import com.sparrowx.document.domain.models.IngestionJob;
import com.sparrowx.document.domain.valueobjects.ContentHash;
import com.sparrowx.document.domain.valueobjects.DocumentId;
import com.sparrowx.document.domain.valueobjects.DocumentTitle;
import com.sparrowx.document.domain.valueobjects.FileName;
import com.sparrowx.document.domain.valueobjects.IngestionJobId;
import com.sparrowx.document.domain.valueobjects.MimeType;
import com.sparrowx.document.domain.valueobjects.ObjectKey;
import com.sparrowx.document.domain.valueobjects.ProjectId;
import com.sparrowx.document.domain.valueobjects.TeamId;
import com.sparrowx.document.domain.valueobjects.TenantId;
import com.sparrowx.document.domain.valueobjects.UserId;
import org.springframework.stereotype.Component;

@Component
public class DocumentPersistenceMapper {

    public Document toDomain(DocumentEntity entity) {
        if (entity == null) {
            return null;
        }

        return new Document(
                DocumentId.of(entity.getDocumentId()),
                TenantId.of(entity.getTenantId()),
                ProjectId.of(entity.getProjectId()),
                TeamId.of(entity.getTeamId()),
                DocumentTitle.of(entity.getTitle()),
                FileName.of(entity.getFileName()),
                MimeType.of(entity.getMimeType()),
                entity.getSizeBytes(),
                ObjectKey.of(entity.getObjectKey()),
                ContentHash.of(entity.getContentHash()),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                UserId.of(entity.getCreatedByUserId())
        );
    }

    public IngestionJob toDomain(IngestionJobEntity entity) {
        if (entity == null) {
            return null;
        }

        return new IngestionJob(
                IngestionJobId.of(entity.getIngestionJobId()),
                DocumentId.of(entity.getDocumentId()),
                TenantId.of(entity.getTenantId()),
                entity.getStatus(),
                entity.getFailureReason(),
                entity.getChunksCreated(),
                entity.getChunksIndexed(),
                entity.getCreatedAt(),
                entity.getCompletedAt()
        );
    }
}