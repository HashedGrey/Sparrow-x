package com.sparrowx.document.ingestion.pipeline;

import com.sparrowx.document.data.minio.DocumentStorage;
import com.sparrowx.document.data.postgres.entities.DocumentChunkEntity;
import com.sparrowx.document.data.postgres.repositories.DocumentChunkRepository;
import com.sparrowx.document.domain.valueobjects.ContentHash;
import com.sparrowx.document.domain.valueobjects.DocumentId;
import com.sparrowx.document.domain.valueobjects.FileName;
import com.sparrowx.document.domain.valueobjects.IngestionJobId;
import com.sparrowx.document.domain.valueobjects.MimeType;
import com.sparrowx.document.domain.valueobjects.ObjectKey;
import com.sparrowx.document.domain.valueobjects.ProjectId;
import com.sparrowx.document.domain.valueobjects.TeamId;
import com.sparrowx.document.domain.valueobjects.TenantId;
import com.sparrowx.document.exceptions.InvalidDocumentException;
import com.sparrowx.document.ingestion.chunking.DocumentChunkDraft;
import com.sparrowx.document.ingestion.chunking.DocumentChunker;
import com.sparrowx.document.ingestion.extraction.DocumentTextExtractor;
import com.sparrowx.document.ingestion.extraction.ExtractedDocumentText;
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
    private final DocumentTextExtractor documentTextExtractor;
    private final DocumentChunker documentChunker;
    private final DocumentChunkRepository documentChunkRepository;
    private final DocumentChunkIndexer documentChunkIndexer;

    public IngestionPipeline(
            DocumentStorage documentStorage,
            DocumentTextExtractor documentTextExtractor,
            DocumentChunker documentChunker,
            DocumentChunkRepository documentChunkRepository,
            DocumentChunkIndexer documentChunkIndexer
    ) {
        this.documentStorage = documentStorage;
        this.documentTextExtractor = documentTextExtractor;
        this.documentChunker = documentChunker;
        this.documentChunkRepository = documentChunkRepository;
        this.documentChunkIndexer = documentChunkIndexer;
    }

    public IngestionPipelineResult execute(IngestionPipelineRequest request) {
        validate(request);

        List<IngestionPipelineStep> completedSteps = new ArrayList<>();

        byte[] content = documentStorage.read(request.objectKey().value());
        completedSteps.add(IngestionPipelineStep.READ_OBJECT);

        ExtractedDocumentText extractedText = documentTextExtractor.extract(
                new DocumentTextExtractor.ExtractTextRequest(
                        request.documentId(),
                        request.objectKey(),
                        request.fileName(),
                        request.mimeType(),
                        content
                )
        );
        completedSteps.add(IngestionPipelineStep.EXTRACT_TEXT);

        List<DocumentChunkDraft> chunkDrafts = documentChunker.chunk(extractedText);
        completedSteps.add(IngestionPipelineStep.CHUNK_TEXT);

        List<DocumentChunkEntity> chunkEntities = chunkDrafts.stream()
                .map(chunkDraft -> toEntity(request, chunkDraft))
                .toList();

        documentChunkRepository.saveAll(chunkEntities);
        completedSteps.add(IngestionPipelineStep.PERSIST_CHUNKS);

        DocumentChunkIndexResult indexResult = documentChunkIndexer.index(
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

        String text = extractedText.text();

        return new IngestionPipelineResult(
                request.ingestionJobId(),
                request.documentId(),
                text,
                text == null ? 0 : text.length(),
                extractedText.pageCount(),
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

        String chunkText = chunkDraft.text();

        entity.setChunkId(chunkDraft.chunkId().value());
        entity.setDocumentId(request.documentId().value());
        entity.setTenantId(request.tenantId().value());
        entity.setProjectId(request.projectId() == null ? null : request.projectId().value());
        entity.setTeamId(request.teamId() == null ? null : request.teamId().value());
        entity.setText(chunkText);
        entity.setChunkIndex(chunkDraft.chunkIndex());
        entity.setPageStart(chunkDraft.pageStart());
        entity.setPageEnd(chunkDraft.pageEnd());
        entity.setTokenCount(estimateTokenCount(chunkText));
        entity.setContentHash(ContentHash.sha256(chunkText).value());
        entity.setCreatedAt(Instant.now());

        return entity;
    }

    private int estimateTokenCount(String text) {
        if (text == null || text.isBlank()) {
            return 0;
        }

        return Math.max(1, text.trim().split("\\s+").length);
    }

    private void validate(IngestionPipelineRequest request) {
        if (request == null) {
            throw InvalidDocumentException.nullCommand("IngestionPipelineRequest");
        }

        Objects.requireNonNull(request.ingestionJobId(), "ingestionJobId must not be null");
        Objects.requireNonNull(request.documentId(), "documentId must not be null");
        Objects.requireNonNull(request.tenantId(), "tenantId must not be null");
        Objects.requireNonNull(request.objectKey(), "objectKey must not be null");
        Objects.requireNonNull(request.fileName(), "fileName must not be null");
        Objects.requireNonNull(request.mimeType(), "mimeType must not be null");
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