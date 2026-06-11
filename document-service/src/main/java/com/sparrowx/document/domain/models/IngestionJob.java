package com.sparrowx.document.domain.models;

import com.sparrowx.document.domain.valueobjects.DocumentId;
import com.sparrowx.document.domain.valueobjects.IngestionJobId;
import com.sparrowx.document.domain.valueobjects.IngestionStatus;
import com.sparrowx.document.domain.valueobjects.TenantId;

import java.time.Instant;

public record IngestionJob(
        IngestionJobId ingestionJobId,
        DocumentId documentId,
        TenantId tenantId,
        IngestionStatus status,
        String failureReason,
        int chunksCreated,
        int chunksIndexed,
        Instant createdAt,
        Instant completedAt
) {
}