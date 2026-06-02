package com.sparrowx.document.ingestion.indexing;

import com.sparrowx.document.data.qdrant.QdrantChunkIndexer;
import com.sparrowx.document.exceptions.DocumentIndexingException;
import com.sparrowx.document.exceptions.InvalidDocumentException;
import com.sparrowx.document.ingestion.chunking.DocumentChunkDraft;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
public class QdrantDocumentChunkIndexer {

    private final QdrantChunkIndexer qdrantChunkIndexer;
    private final EmbeddingService embeddingService;

    public QdrantDocumentChunkIndexer(
            QdrantChunkIndexer qdrantChunkIndexer,
            EmbeddingService embeddingService
    ) {
        this.qdrantChunkIndexer = qdrantChunkIndexer;
        this.embeddingService = embeddingService;
    }

    public int index(DocumentChunkIndexRequest request) {
        validate(request);

        try {
            List<DocumentChunkDraft> chunks = request.chunks();

            if (chunks.isEmpty()) {
                return 0;
            }

            int indexed = 0;

            for (DocumentChunkDraft chunk : chunks) {
                List<Float> vector = embeddingService.embedDocument(chunk.text());

                qdrantChunkIndexer.indexChunk(
                        request.tenantId(),
                        request.projectId(),
                        request.teamId(),
                        request.documentId(),
                        chunk.chunkId(),
                        chunk.text(),
                        vector,
                        chunk.chunkIndex(),
                        chunk.pageStart(),
                        chunk.pageEnd(),
                        chunk.metadata()
                );

                indexed++;
            }

            return indexed;

        } catch (RuntimeException exception) {
            if (exception instanceof DocumentIndexingException) {
                throw exception;
            }

            throw new DocumentIndexingException(
                    "Failed to index chunks into Qdrant for documentId="
                            + request.documentId().value(),
                    exception
            );
        }
    }

    private void validate(DocumentChunkIndexRequest request) {
        if (request == null) {
            throw InvalidDocumentException.nullCommand("DocumentChunkIndexRequest");
        }

        Objects.requireNonNull(request.tenantId(), "tenantId must not be null");
        Objects.requireNonNull(request.documentId(), "documentId must not be null");
        Objects.requireNonNull(request.chunks(), "chunks must not be null");
    }
}