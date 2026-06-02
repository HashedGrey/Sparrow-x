package com.sparrowx.document.ingestion.pipeline;

public enum IngestionPipelineStep {
    READ_OBJECT,
    EXTRACT_TEXT,
    CHUNK_TEXT,
    PERSIST_CHUNKS,
    INDEX_CHUNKS,
    COMPLETED,
    FAILED
}