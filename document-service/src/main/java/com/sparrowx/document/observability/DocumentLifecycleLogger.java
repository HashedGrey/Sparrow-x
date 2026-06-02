package com.sparrowx.document.observability;

import com.sparrowx.document.domain.valueobjects.ContentHash;
import com.sparrowx.document.domain.valueobjects.DocumentId;
import com.sparrowx.document.domain.valueobjects.FileName;
import com.sparrowx.document.domain.valueobjects.IngestionJobId;
import com.sparrowx.document.domain.valueobjects.MimeType;
import com.sparrowx.document.domain.valueobjects.ObjectKey;
import com.sparrowx.document.domain.valueobjects.TenantId;
import com.sparrowx.document.domain.valueobjects.UserId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class DocumentLifecycleLogger {

    private static final Logger log =
            LoggerFactory.getLogger(DocumentLifecycleLogger.class);

    public void uploadAccepted(
            TenantId tenantId,
            UserId userId,
            DocumentId documentId,
            IngestionJobId ingestionJobId,
            FileName fileName,
            MimeType mimeType,
            long sizeBytes
    ) {
        log.info(
                "Document upload accepted tenantId={} userId={} documentId={} ingestionJobId={} fileName={} mimeType={} sizeBytes={}",
                value(tenantId),
                value(userId),
                value(documentId),
                value(ingestionJobId),
                value(fileName),
                value(mimeType),
                sizeBytes
        );
    }

    public void uploadStored(
            TenantId tenantId,
            DocumentId documentId,
            ObjectKey objectKey,
            ContentHash contentHash
    ) {
        log.info(
                "Document binary stored tenantId={} documentId={} objectKey={} contentHash={}",
                value(tenantId),
                value(documentId),
                value(objectKey),
                value(contentHash)
        );
    }

    public void uploadRejected(
            TenantId tenantId,
            UserId userId,
            FileName fileName,
            String reason
    ) {
        log.warn(
                "Document upload rejected tenantId={} userId={} fileName={} reason={}",
                value(tenantId),
                value(userId),
                value(fileName),
                reason
        );
    }

    private String value(TenantId value) {
        return value == null ? null : value.value();
    }

    private String value(UserId value) {
        return value == null ? null : value.value();
    }

    private String value(DocumentId value) {
        return value == null ? null : value.value();
    }

    private String value(IngestionJobId value) {
        return value == null ? null : value.value();
    }

    private String value(FileName value) {
        return value == null ? null : value.value();
    }

    private String value(MimeType value) {
        return value == null ? null : value.value();
    }

    private String value(ObjectKey value) {
        return value == null ? null : value.value();
    }

    private String value(ContentHash value) {
        return value == null ? null : value.value();
    }
}