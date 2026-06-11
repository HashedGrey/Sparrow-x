package com.sparrowx.document.domain.valueobjects;

public enum IngestionStatus {
    QUEUED,
    EXTRACTING,
    CHUNKING,
    EMBEDDING,
    INDEXING,
    COMPLETED,
    FAILED
}