package com.sparrowx.document.ingestion;

import com.sparrowx.document.domain.valueobjects.DocumentId;
import com.sparrowx.document.domain.valueobjects.FileName;
import com.sparrowx.document.domain.valueobjects.IngestionJobId;
import com.sparrowx.document.domain.valueobjects.MimeType;
import com.sparrowx.document.domain.valueobjects.ObjectKey;
import com.sparrowx.document.domain.valueobjects.ProjectId;
import com.sparrowx.document.domain.valueobjects.TeamId;
import com.sparrowx.document.domain.valueobjects.TenantId;
import com.sparrowx.document.exceptions.InvalidDocumentException;
import com.sparrowx.document.ingestion.pipeline.IngestionPipeline;
import com.sparrowx.document.ingestion.pipeline.IngestionPipelineResult;
import com.sparrowx.document.observability.IngestionLifecycleLogger;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class DocumentProcessor {

    private final IngestionPipeline ingestionPipeline;
    private final IngestionLifecycleLogger ingestionLifecycleLogger;

    public DocumentProcessor(
            IngestionPipeline ingestionPipeline,
            IngestionLifecycleLogger ingestionLifecycleLogger
    ) {
        this.ingestionPipeline = ingestionPipeline;
        this.ingestionLifecycleLogger = ingestionLifecycleLogger;
    }

    public DocumentProcessingResult process(ProcessDocumentRequest request) {
        validate(request);

        IngestionPipelineResult result = ingestionPipeline.execute(
                new IngestionPipeline.IngestionPipelineRequest(
                        request.ingestionJobId(),
                        request.documentId(),
                        request.tenantId(),
                        request.projectId(),
                        request.teamId(),
                        request.objectKey(),
                        request.fileName(),
                        request.mimeType()
                )
        );

        ingestionLifecycleLogger.textExtracted(
                request.tenantId(),
                request.documentId(),
                request.ingestionJobId(),
                result.extractedCharacters(),
                result.pageCount()
        );

        ingestionLifecycleLogger.chunksCreated(
                request.tenantId(),
                request.documentId(),
                request.ingestionJobId(),
                result.chunksCreated()
        );

        return new DocumentProcessingResult(
                result.ingestionJobId(),
                result.documentId(),
                result.extractedText(),
                result.chunksCreated(),
                result.chunksIndexed()
        );
    }

    private void validate(ProcessDocumentRequest request) {
        if (request == null) {
            throw InvalidDocumentException.nullCommand("ProcessDocumentRequest");
        }

        Objects.requireNonNull(request.ingestionJobId(), "ingestionJobId must not be null");
        Objects.requireNonNull(request.documentId(), "documentId must not be null");
        Objects.requireNonNull(request.tenantId(), "tenantId must not be null");
        Objects.requireNonNull(request.objectKey(), "objectKey must not be null");
        Objects.requireNonNull(request.fileName(), "fileName must not be null");
        Objects.requireNonNull(request.mimeType(), "mimeType must not be null");
    }

    public record ProcessDocumentRequest(
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