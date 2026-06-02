package com.sparrowx.document.grpc.health;

import com.sparrowx.document.data.postgres.repositories.DocumentRepository;
import com.sparrowx.document.data.minio.DocumentStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class DocumentServiceStartupCheck {

    private static final Logger log =
            LoggerFactory.getLogger(DocumentServiceStartupCheck.class);

    private final DocumentRepository documentRepository;
    private final DocumentStorage documentStorage;

    public DocumentServiceStartupCheck(
            DocumentRepository documentRepository,
            DocumentStorage documentStorage
    ) {
        this.documentRepository = documentRepository;
        this.documentStorage = documentStorage;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void verifyStartup() {
        long documentCount = documentRepository.count();

        log.info(
                "Document service startup check complete documentCount={} storageProvider={}",
                documentCount,
                documentStorage.getClass().getSimpleName()
        );
    }
}