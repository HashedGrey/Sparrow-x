package com.sparrowx.document.features.processingestionjob;

import buildingblocks.core.commands.CommandHandler;
import com.sparrowx.document.domain.valueobjects.IngestionStatus;
import com.sparrowx.document.exceptions.InvalidDocumentException;
import com.sparrowx.document.ingestion.DocumentProcessingResult;
import com.sparrowx.document.ingestion.DocumentProcessor;
import com.sparrowx.document.observability.IngestionLifecycleLogger;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

@Component
public class ProcessIngestionJobCommandHandler
        implements CommandHandler<ProcessIngestionJobCommand, ProcessIngestionJobResult> {

    private final DocumentProcessor documentProcessor;
    private final IngestionJobLifecycleService lifecycleService;
    private final IngestionLifecycleLogger ingestionLifecycleLogger;

    private final Timer ingestionDurationTimer;
    private final Counter ingestionCompletedCounter;
    private final Counter ingestionFailedCounter;
    private final Counter ingestionAlreadyCompletedCounter;
    private final Counter chunksCreatedCounter;
    private final Counter chunksIndexedCounter;

    public ProcessIngestionJobCommandHandler(
            DocumentProcessor documentProcessor,
            IngestionJobLifecycleService lifecycleService,
            IngestionLifecycleLogger ingestionLifecycleLogger,
            MeterRegistry meterRegistry
    ) {
        this.documentProcessor = documentProcessor;
        this.lifecycleService = lifecycleService;
        this.ingestionLifecycleLogger = ingestionLifecycleLogger;

        this.ingestionDurationTimer = Timer.builder("document.ingestion.duration")
                .description("Time taken to process a document ingestion job")
                .register(meterRegistry);

        this.ingestionCompletedCounter = Counter.builder("document.ingestion.completed.count")
                .description("Number of successfully completed document ingestion jobs")
                .register(meterRegistry);

        this.ingestionFailedCounter = Counter.builder("document.ingestion.failed.count")
                .description("Number of failed document ingestion jobs")
                .register(meterRegistry);

        this.ingestionAlreadyCompletedCounter = Counter.builder("document.ingestion.already_completed.count")
                .description("Number of ingestion jobs skipped because they were already completed")
                .register(meterRegistry);

        this.chunksCreatedCounter = Counter.builder("document.ingestion.chunks.created.count")
                .description("Number of document chunks created during ingestion")
                .register(meterRegistry);

        this.chunksIndexedCounter = Counter.builder("document.ingestion.chunks.indexed.count")
                .description("Number of document chunks indexed during ingestion")
                .register(meterRegistry);
    }

    @Override
    public ProcessIngestionJobResult handle(ProcessIngestionJobCommand command) {
        return ingestionDurationTimer.record(() -> handleTimed(command));
    }

    private ProcessIngestionJobResult handleTimed(ProcessIngestionJobCommand command) {
        validate(command);

        IngestionJobLifecycleService.StartState startState = lifecycleService.start(
                command.tenantId(),
                command.documentId(),
                command.ingestionJobId()
        );

        if (startState.alreadyCompleted()) {
            ingestionAlreadyCompletedCounter.increment();

            return new ProcessIngestionJobResult(
                    command.ingestionJobId(),
                    command.documentId(),
                    startState.status(),
                    startState.chunksCreated(),
                    startState.chunksIndexed()
            );
        }

        ingestionLifecycleLogger.started(
                command.tenantId(),
                command.documentId(),
                command.ingestionJobId()
        );

        try {
            DocumentProcessingResult processingResult = documentProcessor.process(
                    new DocumentProcessor.ProcessDocumentRequest(
                            command.ingestionJobId(),
                            command.documentId(),
                            command.tenantId(),
                            command.projectId(),
                            command.teamId(),
                            command.objectKey(),
                            command.fileName(),
                            command.mimeType()
                    )
            );

            lifecycleService.complete(
                    command.tenantId(),
                    command.documentId(),
                    command.ingestionJobId(),
                    processingResult.chunksCreated(),
                    processingResult.chunksIndexed()
            );

            ingestionCompletedCounter.increment();
            chunksCreatedCounter.increment(processingResult.chunksCreated());
            chunksIndexedCounter.increment(processingResult.chunksIndexed());

            ingestionLifecycleLogger.completed(
                    command.tenantId(),
                    command.documentId(),
                    command.ingestionJobId(),
                    processingResult.chunksCreated(),
                    processingResult.chunksIndexed()
            );

            return new ProcessIngestionJobResult(
                    command.ingestionJobId(),
                    command.documentId(),
                    IngestionStatus.COMPLETED,
                    processingResult.chunksCreated(),
                    processingResult.chunksIndexed()
            );

        } catch (RuntimeException exception) {
            try {
                lifecycleService.fail(
                        command.tenantId(),
                        command.documentId(),
                        command.ingestionJobId(),
                        exception.getMessage()
                );
            } catch (RuntimeException persistenceException) {
                exception.addSuppressed(persistenceException);
            }

            ingestionFailedCounter.increment();

            ingestionLifecycleLogger.failed(
                    command.tenantId(),
                    command.documentId(),
                    command.ingestionJobId(),
                    exception.getMessage()
            );

            throw exception;
        }
    }

    private void validate(ProcessIngestionJobCommand command) {
        if (command == null) {
            throw InvalidDocumentException.nullCommand("ProcessIngestionJobCommand");
        }

        if (command.ingestionJobId() == null) {
            throw InvalidDocumentException.blankField("ingestionJobId");
        }

        if (command.documentId() == null) {
            throw InvalidDocumentException.blankField("documentId");
        }

        if (command.tenantId() == null) {
            throw InvalidDocumentException.blankField("tenantId");
        }

        if (command.objectKey() == null) {
            throw InvalidDocumentException.blankField("objectKey");
        }

        if (command.fileName() == null) {
            throw InvalidDocumentException.blankField("fileName");
        }

        if (command.mimeType() == null) {
            throw InvalidDocumentException.blankField("mimeType");
        }
    }
}