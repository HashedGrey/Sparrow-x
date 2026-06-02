package com.sparrowx.document.ingestion.indexing;

import com.sparrowx.document.domain.valueobjects.DocumentId;
import com.sparrowx.document.domain.valueobjects.IngestionJobId;

public record DocumentChunkIndexResult(
        IngestionJobId ingestionJobId,
        DocumentId documentId,
        int chunksRequested,
        int chunksIndexed
) {
}