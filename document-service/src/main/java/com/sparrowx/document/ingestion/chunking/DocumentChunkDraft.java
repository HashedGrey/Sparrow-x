package com.sparrowx.document.ingestion.chunking;

import com.sparrowx.document.domain.valueobjects.ChunkId;
import com.sparrowx.document.domain.valueobjects.DocumentId;

import java.util.Map;

public record DocumentChunkDraft(
        DocumentId documentId,
        ChunkId chunkId,
        String text,
        int chunkIndex,
        int pageStart,
        int pageEnd,
        Map<String, String> metadata
) {
    public DocumentChunkDraft {
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}