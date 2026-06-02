package com.sparrowx.document.observability;

import com.sparrowx.document.domain.valueobjects.DocumentId;
import com.sparrowx.document.domain.valueobjects.IngestionJobId;
import com.sparrowx.document.domain.valueobjects.TenantId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class IngestionLifecycleLogger {

    private static final Logger log =
            LoggerFactory.getLogger(IngestionLifecycleLogger.class);

    public void queued(
            TenantId tenantId,
            DocumentId documentId,
            IngestionJobId ingestionJobId
    ) {
        log.info(
                "Ingestion job queued tenantId={} documentId={} ingestionJobId={}",
                value(tenantId),
                value(documentId),
                value(ingestionJobId)
        );
    }

    public void started(
            TenantId tenantId,
            DocumentId documentId,
            IngestionJobId ingestionJobId
    ) {
        log.info(
                "Ingestion job started tenantId={} documentId={} ingestionJobId={}",
                value(tenantId),
                value(documentId),
                value(ingestionJobId)
        );
    }

    public void textExtracted(
            TenantId tenantId,
            DocumentId documentId,
            IngestionJobId ingestionJobId,
            int extractedCharacters,
            int pageCount
    ) {
        log.info(
                "Document text extracted tenantId={} documentId={} ingestionJobId={} extractedCharacters={} pageCount={}",
                value(tenantId),
                value(documentId),
                value(ingestionJobId),
                extractedCharacters,
                pageCount
        );
    }

    public void chunksCreated(
            TenantId tenantId,
            DocumentId documentId,
            IngestionJobId ingestionJobId,
            int chunksCreated
    ) {
        log.info(
                "Document chunks created tenantId={} documentId={} ingestionJobId={} chunksCreated={}",
                value(tenantId),
                value(documentId),
                value(ingestionJobId),
                chunksCreated
        );
    }

    public void completed(
            TenantId tenantId,
            DocumentId documentId,
            IngestionJobId ingestionJobId,
            int chunksCreated,
            int chunksIndexed
    ) {
        log.info(
                "Ingestion job completed tenantId={} documentId={} ingestionJobId={} chunksCreated={} chunksIndexed={}",
                value(tenantId),
                value(documentId),
                value(ingestionJobId),
                chunksCreated,
                chunksIndexed
        );
    }

    public void failed(
            TenantId tenantId,
            DocumentId documentId,
            IngestionJobId ingestionJobId,
            String reason
    ) {
        log.error(
                "Ingestion job failed tenantId={} documentId={} ingestionJobId={} reason={}",
                value(tenantId),
                value(documentId),
                value(ingestionJobId),
                reason
        );
    }

    private String value(TenantId value) {
        return value == null ? null : value.value();
    }

    private String value(DocumentId value) {
        return value == null ? null : value.value();
    }

    private String value(IngestionJobId value) {
        return value == null ? null : value.value();
    }
}