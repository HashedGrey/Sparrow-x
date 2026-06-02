package com.sparrowx.document.ingestion.indexing;

public record GeminiEmbeddingRequest(
        String model,
        String content,
        String taskType,
        Integer outputDimensionality
) {
}