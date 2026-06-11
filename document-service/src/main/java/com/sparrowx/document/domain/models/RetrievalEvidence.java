package com.sparrowx.document.domain.models;

import com.sparrowx.document.domain.valueobjects.ChunkId;
import com.sparrowx.document.domain.valueobjects.DocumentId;

public record RetrievalEvidence(
        String evidenceId,
        DocumentId documentId,
        ChunkId chunkId,
        String title,
        String fileName,
        String text,
        int pageStart,
        int pageEnd,
        double relevanceScore,
        String citation
) {
}