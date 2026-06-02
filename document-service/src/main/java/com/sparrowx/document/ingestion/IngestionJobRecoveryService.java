package com.sparrowx.document.ingestion;

import com.sparrowx.document.data.postgres.entities.DocumentEntity;
import com.sparrowx.document.data.postgres.entities.IngestionJobEntity;
import com.sparrowx.document.data.postgres.repositories.DocumentRepository;
import com.sparrowx.document.data.postgres.repositories.IngestionJobRepository;
import com.sparrowx.document.domain.valueobjects.DocumentId;
import com.sparrowx.document.domain.valueobjects.FileName;
import com.sparrowx.document.domain.valueobjects.IngestionJobId;
import com.sparrowx.document.domain.valueobjects.IngestionStatus;
import com.sparrowx.document.domain.valueobjects.MimeType;
import com.sparrowx.document.domain.valueobjects.ObjectKey;
import com.sparrowx.document.domain.valueobjects.ProjectId;
import com.sparrowx.document.domain.valueobjects.TeamId;
import com.sparrowx.document.domain.valueobjects.TenantId;
import com.sparrowx.document.ingestion.queue.IngestionQueue;
import com.sparrowx.document.ingestion.queue.IngestionQueueMessage;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
public class IngestionJobRecoveryService {

    private final IngestionJobRepository ingestionJobRepository;
    private final DocumentRepository documentRepository;
    private final IngestionQueue ingestionQueue;

    public IngestionJobRecoveryService(
            IngestionJobRepository ingestionJobRepository,
            DocumentRepository documentRepository,
            IngestionQueue ingestionQueue
    ) {
        this.ingestionJobRepository = ingestionJobRepository;
        this.documentRepository = documentRepository;
        this.ingestionQueue = ingestionQueue;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void recoverQueuedJobs() {
        List<IngestionJobEntity> recoverableJobs = ingestionJobRepository.findByStatusIn(List.of(
                IngestionStatus.QUEUED,
                IngestionStatus.EXTRACTING,
                IngestionStatus.CHUNKING,
                IngestionStatus.EMBEDDING,
                IngestionStatus.INDEXING
        ));

        for (IngestionJobEntity job : recoverableJobs) {
            documentRepository.findByDocumentId(job.getDocumentId())
                    .ifPresent(document -> enqueue(job, document));
        }
    }

    private void enqueue(
            IngestionJobEntity job,
            DocumentEntity document
    ) {
        ingestionQueue.enqueue(new IngestionQueueMessage(
                IngestionJobId.of(job.getIngestionJobId()),
                DocumentId.of(document.getDocumentId()),
                TenantId.of(document.getTenantId()),
                ProjectId.of(document.getProjectId()),
                TeamId.of(document.getTeamId()),
                ObjectKey.of(document.getObjectKey()),
                FileName.of(document.getFileName()),
                MimeType.of(document.getMimeType()),
                Instant.now(),

                null,
                null,

                null,
                null,
                null
        ));
    }
}