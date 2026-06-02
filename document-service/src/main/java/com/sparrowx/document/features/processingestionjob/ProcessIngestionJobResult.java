package com.sparrowx.document.features.processingestionjob;

import com.sparrowx.document.domain.valueobjects.DocumentId;
import com.sparrowx.document.domain.valueobjects.IngestionJobId;
import com.sparrowx.document.domain.valueobjects.IngestionStatus;

public record ProcessIngestionJobResult(
        IngestionJobId ingestionJobId,
        DocumentId documentId,
        IngestionStatus status,
        int chunksCreated,
        int chunksIndexed
) {
}