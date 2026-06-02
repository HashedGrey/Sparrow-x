package com.sparrowx.document.ingestion;

import com.sparrowx.document.domain.valueobjects.DocumentId;
import com.sparrowx.document.domain.valueobjects.IngestionJobId;

public record DocumentProcessingResult(
        IngestionJobId ingestionJobId,
        DocumentId documentId,
        String extractedText,
        int chunksCreated,
        int chunksIndexed
) {
}