package com.sparrowx.document.features.uploaddocument;

import buildingblocks.core.commands.CommandHandler;
import com.sparrowx.document.data.minio.DocumentStorage;
import com.sparrowx.document.data.postgres.entities.DocumentEntity;
import com.sparrowx.document.data.postgres.entities.IngestionJobEntity;
import com.sparrowx.document.data.postgres.repositories.DocumentRepository;
import com.sparrowx.document.data.postgres.repositories.IngestionJobRepository;
import com.sparrowx.document.domain.valueobjects.ContentHash;
import com.sparrowx.document.domain.valueobjects.DocumentId;
import com.sparrowx.document.domain.valueobjects.DocumentStatus;
import com.sparrowx.document.domain.valueobjects.IngestionJobId;
import com.sparrowx.document.domain.valueobjects.IngestionStatus;
import com.sparrowx.document.domain.valueobjects.ObjectKey;
import com.sparrowx.document.exceptions.InvalidDocumentException;
import com.sparrowx.document.ingestion.queue.IngestionQueue;
import com.sparrowx.document.ingestion.queue.IngestionQueueMessage;
import com.sparrowx.document.observability.DocumentLifecycleLogger;
import com.sparrowx.document.observability.IngestionLifecycleLogger;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class UploadDocumentCommandHandler
        implements CommandHandler<UploadDocumentCommand, UploadDocumentResult> {

    private final DocumentRepository documentRepository;
    private final IngestionJobRepository ingestionJobRepository;
    private final DocumentStorage documentStorage;
    private final IngestionQueue ingestionQueue;
    private final DocumentLifecycleLogger documentLifecycleLogger;
    private final IngestionLifecycleLogger ingestionLifecycleLogger;

    private final Timer uploadDurationTimer;
    private final Counter uploadAcceptedCounter;
    private final Counter uploadRejectedCounter;
    private final Counter uploadFailedCounter;
    private final DistributionSummary uploadSizeBytesSummary;

    public UploadDocumentCommandHandler(
            DocumentRepository documentRepository,
            IngestionJobRepository ingestionJobRepository,
            DocumentStorage documentStorage,
            IngestionQueue ingestionQueue,
            DocumentLifecycleLogger documentLifecycleLogger,
            IngestionLifecycleLogger ingestionLifecycleLogger,
            MeterRegistry meterRegistry
    ) {
        this.documentRepository = documentRepository;
        this.ingestionJobRepository = ingestionJobRepository;
        this.documentStorage = documentStorage;
        this.ingestionQueue = ingestionQueue;
        this.documentLifecycleLogger = documentLifecycleLogger;
        this.ingestionLifecycleLogger = ingestionLifecycleLogger;

        this.uploadDurationTimer = Timer.builder("document.upload.duration")
                .description("Time taken to accept and persist a document upload")
                .register(meterRegistry);

        this.uploadAcceptedCounter = Counter.builder("document.upload.accepted.count")
                .description("Number of accepted document uploads")
                .register(meterRegistry);

        this.uploadRejectedCounter = Counter.builder("document.upload.rejected.count")
                .description("Number of rejected document uploads")
                .register(meterRegistry);

        this.uploadFailedCounter = Counter.builder("document.upload.failed.count")
                .description("Number of failed document uploads")
                .register(meterRegistry);

        this.uploadSizeBytesSummary = DistributionSummary.builder("document.upload.size.bytes")
                .description("Uploaded document size in bytes")
                .baseUnit("bytes")
                .register(meterRegistry);
    }

    @Override
    public UploadDocumentResult handle(UploadDocumentCommand command) {
        return uploadDurationTimer.record(() -> handleTimed(command));
    }

    private UploadDocumentResult handleTimed(UploadDocumentCommand command) {
        try {
            validate(command);

            DocumentId documentId = DocumentId.newId();
            IngestionJobId ingestionJobId = IngestionJobId.newId();
            ContentHash contentHash = ContentHash.sha256(command.content());

            ObjectKey objectKey = ObjectKey.forDocument(
                    command.tenantId(),
                    documentId,
                    command.fileName()
            );

            Instant now = Instant.now();
            long sizeBytes = command.content().length;

            documentStorage.store(new DocumentStorage.StoreDocumentObjectRequest(
                    command.tenantId(),
                    documentId,
                    objectKey,
                    command.fileName(),
                    command.mimeType(),
                    command.content()
            ));

            documentLifecycleLogger.uploadStored(
                    command.tenantId(),
                    documentId,
                    objectKey,
                    contentHash
            );

            DocumentEntity documentEntity = new DocumentEntity();
            documentEntity.setDocumentId(documentId.value());
            documentEntity.setTenantId(command.tenantId().value());
            documentEntity.setProjectId(command.projectId() == null ? null : command.projectId().value());
            documentEntity.setTeamId(command.teamId() == null ? null : command.teamId().value());
            documentEntity.setTitle(resolveTitle(command));
            documentEntity.setFileName(command.fileName().value());
            documentEntity.setMimeType(command.mimeType().value());
            documentEntity.setSizeBytes(sizeBytes);
            documentEntity.setObjectKey(objectKey.value());
            documentEntity.setContentHash(contentHash.value());
            documentEntity.setStatus(DocumentStatus.UPLOADED);
            documentEntity.setCreatedAt(now);
            documentEntity.setUpdatedAt(now);
            documentEntity.setCreatedByUserId(command.userId().value());

            IngestionJobEntity ingestionJobEntity = new IngestionJobEntity();
            ingestionJobEntity.setIngestionJobId(ingestionJobId.value());
            ingestionJobEntity.setDocumentId(documentId.value());
            ingestionJobEntity.setTenantId(command.tenantId().value());
            ingestionJobEntity.setStatus(IngestionStatus.QUEUED);
            ingestionJobEntity.setFailureReason(null);
            ingestionJobEntity.setChunksCreated(0);
            ingestionJobEntity.setChunksIndexed(0);
            ingestionJobEntity.setCreatedAt(now);
            ingestionJobEntity.setCompletedAt(null);

            documentRepository.save(documentEntity);
            ingestionJobRepository.save(ingestionJobEntity);

            SpanContext spanContext = Span.current().getSpanContext();

            String parentTraceId = spanContext.isValid() ? spanContext.getTraceId() : null;
            String parentSpanId = spanContext.isValid() ? spanContext.getSpanId() : null;
            String traceFlags = spanContext.isValid() ? spanContext.getTraceFlags().asHex() : null;

            ingestionQueue.enqueue(new IngestionQueueMessage(
                    ingestionJobId,
                    documentId,
                    command.tenantId(),
                    command.projectId(),
                    command.teamId(),
                    objectKey,
                    command.fileName(),
                    command.mimeType(),
                    now,
                    command.requestId(),
                    command.traceId(),
                    parentTraceId,
                    parentSpanId,
                    traceFlags
            ));

            documentLifecycleLogger.uploadAccepted(
                    command.tenantId(),
                    command.userId(),
                    documentId,
                    ingestionJobId,
                    command.fileName(),
                    command.mimeType(),
                    sizeBytes
            );

            ingestionLifecycleLogger.queued(
                    command.tenantId(),
                    documentId,
                    ingestionJobId
            );

            uploadAcceptedCounter.increment();
            uploadSizeBytesSummary.record(sizeBytes);

            return new UploadDocumentResult(
                    documentId,
                    ingestionJobId,
                    DocumentStatus.UPLOADED
            );

        } catch (InvalidDocumentException exception) {
            uploadRejectedCounter.increment();
            throw exception;
        } catch (RuntimeException exception) {
            uploadFailedCounter.increment();
            throw exception;
        }
    }

    private void validate(UploadDocumentCommand command) {
        if (command == null) {
            throw InvalidDocumentException.nullCommand("UploadDocumentCommand");
        }

        if (command.tenantId() == null) {
            throw InvalidDocumentException.blankField("tenantId");
        }

        if (command.userId() == null) {
            throw InvalidDocumentException.blankField("userId");
        }

        if (command.fileName() == null) {
            throw InvalidDocumentException.blankField("fileName");
        }

        if (command.mimeType() == null) {
            throw InvalidDocumentException.blankField("mimeType");
        }

        if (command.content() == null || command.content().length == 0) {
            documentLifecycleLogger.uploadRejected(
                    command.tenantId(),
                    command.userId(),
                    command.fileName(),
                    "content must not be empty"
            );

            throw InvalidDocumentException.emptyContent();
        }
    }

    private String resolveTitle(UploadDocumentCommand command) {
        if (command.title() != null && command.title().isPresent()) {
            return command.title().value();
        }

        return command.fileName().value();
    }
}