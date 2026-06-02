package com.sparrowx.document.ingestion.indexing;

public interface DocumentChunkIndexer {

    DocumentChunkIndexResult index(DocumentChunkIndexRequest request);
}