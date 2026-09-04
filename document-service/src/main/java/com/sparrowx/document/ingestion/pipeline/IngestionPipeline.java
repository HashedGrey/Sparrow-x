package com.sparrowx.document.ingestion.pipeline;

import com.sparrowx.document.data.minio.DocumentStorage;
import com.sparrowx.document.data.postgres.entities.DocumentChunkEntity;
import com.sparrowx.document.data.postgres.repositories.DocumentChunkRepository;
import com.sparrowx.document.domain.valueobjects.*;
import com.sparrowx.document.exceptions.InvalidDocumentException;
import com.sparrowx.document.ingestion.chunking.DocumentChunkDraft;
import com.sparrowx.document.ingestion.embabel.EmbabelRagIngestionAdapter;
import com.sparrowx.document.ingestion.indexing.DocumentChunkIndexRequest;
import com.sparrowx.document.ingestion.indexing.DocumentChunkIndexResult;
import com.sparrowx.document.ingestion.indexing.DocumentChunkIndexer;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class IngestionPipeline {

    private final DocumentStorage documentStorage;
    private final EmbabelRagIngestionAdapter embabelRagIngestionAdapter;
    private final DocumentChunkRepository documentChunkRepository;
    private final DocumentChunkIndexer documentChunkIndexer;

    public IngestionPipeline(
            DocumentStorage documentStorage,
            EmbabelRagIngestionAdapter embabelRagIngestionAdapter,
            DocumentChunkRepository documentChunkRepository,
            DocumentChunkIndexer documentChunkIndexer
    ) {
        this.documentStorage = documentStorage;
        this.embabelRagIngestionAdapter = embabelRagIngestionAdapter;
        this.documentChunkRepository = documentChunkRepository;
        this.documentChunkIndexer = documentChunkIndexer;
    }

    public IngestionPipelineResult execute(IngestionPipelineRequest request) {
        validate(request);

        List<IngestionPipelineStep> completedSteps = new ArrayList<>();

        byte[] content = documentStorage.read(
                request.objectKey().value()
        );
        completedSteps.add(IngestionPipelineStep.READ_OBJECT);

        EmbabelRagIngestionAdapter.EmbabelRagIngestionResult ingestion =
                embabelRagIngestionAdapter.ingest(
                        request.documentId(),
                        request.objectKey(),
                        request.fileName(),
                        request.mimeType(),
                        content
                );

        /*
         * Embabel performs both parsing and chunking inside one adapter
         * invocation, but retain the existing pipeline lifecycle steps so
         * callers/metrics do not change.
         */
        completedSteps.add(IngestionPipelineStep.EXTRACT_TEXT);
        completedSteps.add(IngestionPipelineStep.CHUNK_TEXT);

        List<DocumentChunkDraft> chunkDrafts = ingestion.chunks();

        List<DocumentChunkEntity> chunkEntities = chunkDrafts.stream()
                .map(chunkDraft -> toEntity(request, chunkDraft))
                .toList();

        documentChunkRepository.saveAll(chunkEntities);
        completedSteps.add(IngestionPipelineStep.PERSIST_CHUNKS);

        DocumentChunkIndexResult indexResult =
                documentChunkIndexer.index(
                        new DocumentChunkIndexRequest(
                                request.ingestionJobId(),
                                request.tenantId(),
                                request.projectId(),
                                request.teamId(),
                                request.documentId(),
                                chunkDrafts
                        )
                );

        completedSteps.add(IngestionPipelineStep.INDEX_CHUNKS);
        completedSteps.add(IngestionPipelineStep.COMPLETED);

        String text = ingestion.extractedText();

        return new IngestionPipelineResult(
                request.ingestionJobId(),
                request.documentId(),
                text,
                text.length(),
                ingestion.pageCount(),
                chunkDrafts.size(),
                indexResult.chunksIndexed(),
                List.copyOf(completedSteps)
        );
    }

    private DocumentChunkEntity toEntity(
            IngestionPipelineRequest request,
            DocumentChunkDraft chunkDraft
    ) {
        DocumentChunkEntity entity = new DocumentChunkEntity();

        entity.setChunkId(chunkDraft.chunkId().value());
        entity.setDocumentId(request.documentId().value());
        entity.setTenantId(request.tenantId().value());
        entity.setProjectId(request.projectId() == null ? null : request.projectId().value());
        entity.setTeamId(request.teamId() == null ? null : request.teamId().value());
        entity.setText(chunkDraft.text());
        entity.setContentHash(ContentHash.sha256(chunkDraft.text()).value());
        entity.setChunkIndex(chunkDraft.chunkIndex());
        entity.setPageStart(chunkDraft.pageStart());
        entity.setPageEnd(chunkDraft.pageEnd());
        entity.setCreatedAt(Instant.now());

        return entity;
    }

    private void validate(IngestionPipelineRequest request) {
        if (request == null) {
            throw InvalidDocumentException.nullCommand(
                    "IngestionPipelineRequest"
            );
        }

        Objects.requireNonNull(
                request.ingestionJobId(),
                "ingestionJobId must not be null"
        );
        Objects.requireNonNull(
                request.documentId(),
                "documentId must not be null"
        );
        Objects.requireNonNull(
                request.tenantId(),
                "tenantId must not be null"
        );
        Objects.requireNonNull(
                request.objectKey(),
                "objectKey must not be null"
        );
        Objects.requireNonNull(
                request.fileName(),
                "fileName must not be null"
        );
        Objects.requireNonNull(
                request.mimeType(),
                "mimeType must not be null"
        );
    }

    public record IngestionPipelineRequest(
            IngestionJobId ingestionJobId,
            DocumentId documentId,
            TenantId tenantId,
            ProjectId projectId,
            TeamId teamId,
            ObjectKey objectKey,
            FileName fileName,
            MimeType mimeType
    ) {
    }
}