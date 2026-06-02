package com.sparrowx.document.ingestion.queue;

import com.sparrowx.document.domain.valueobjects.DocumentId;
import com.sparrowx.document.domain.valueobjects.FileName;
import com.sparrowx.document.domain.valueobjects.IngestionJobId;
import com.sparrowx.document.domain.valueobjects.MimeType;
import com.sparrowx.document.domain.valueobjects.ObjectKey;
import com.sparrowx.document.domain.valueobjects.ProjectId;
import com.sparrowx.document.domain.valueobjects.RequestId;
import com.sparrowx.document.domain.valueobjects.TeamId;
import com.sparrowx.document.domain.valueobjects.TenantId;
import com.sparrowx.document.domain.valueobjects.TraceId;

import java.time.Instant;
import java.util.Objects;

public record IngestionQueueMessage(
        IngestionJobId ingestionJobId,
        DocumentId documentId,
        TenantId tenantId,
        ProjectId projectId,
        TeamId teamId,
        ObjectKey objectKey,
        FileName fileName,
        MimeType mimeType,
        Instant enqueuedAt,

        RequestId requestId,
        TraceId businessTraceId,

        String parentTraceId,
        String parentSpanId,
        String traceFlags
) {

    public IngestionQueueMessage {
        Objects.requireNonNull(ingestionJobId, "ingestionJobId must not be null");
        Objects.requireNonNull(documentId, "documentId must not be null");
        Objects.requireNonNull(tenantId, "tenantId must not be null");
        Objects.requireNonNull(objectKey, "objectKey must not be null");
        Objects.requireNonNull(fileName, "fileName must not be null");
        Objects.requireNonNull(mimeType, "mimeType must not be null");

        enqueuedAt = Objects.requireNonNullElseGet(enqueuedAt, Instant::now);
    }
}