package com.sparrowx.document.ingestion.indexing;

import java.util.List;

public record GeminiEmbeddingResponse(
        List<GeminiEmbedding> embeddings
) {
    public record GeminiEmbedding(
            List<Float> values
    ) {
    }
}