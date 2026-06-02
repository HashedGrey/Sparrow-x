package com.sparrowx.document.ingestion.chunking;

import com.sparrowx.document.domain.valueobjects.ChunkId;
import com.sparrowx.document.ingestion.extraction.ExtractedDocumentText;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class TokenDocumentChunker implements DocumentChunker {

    private static final int DEFAULT_MAX_CHARS_PER_CHUNK = 1_500;
    private static final int DEFAULT_OVERLAP_CHARS = 200;

    private final ChunkBoundaryDetector chunkBoundaryDetector;
    private final ChunkMetadataBuilder chunkMetadataBuilder;

    public TokenDocumentChunker(
            ChunkBoundaryDetector chunkBoundaryDetector,
            ChunkMetadataBuilder chunkMetadataBuilder
    ) {
        this.chunkBoundaryDetector = chunkBoundaryDetector;
        this.chunkMetadataBuilder = chunkMetadataBuilder;
    }

    @Override
    public List<DocumentChunkDraft> chunk(ExtractedDocumentText extractedText) {
        validate(extractedText);

        String text = extractedText.text();
        if (text == null || text.isBlank()) {
            return List.of();
        }

        List<ChunkBoundaryDetector.ChunkBoundary> boundaries =
                chunkBoundaryDetector.detect(
                        text,
                        DEFAULT_MAX_CHARS_PER_CHUNK,
                        DEFAULT_OVERLAP_CHARS
                );

        List<DocumentChunkDraft> chunks = new ArrayList<>();

        for (int i = 0; i < boundaries.size(); i++) {
            ChunkBoundaryDetector.ChunkBoundary boundary = boundaries.get(i);

            String chunkText = text.substring(
                    boundary.startInclusive(),
                    boundary.endExclusive()
            ).trim();

            if (chunkText.isBlank()) {
                continue;
            }

            int pageStart = estimatePage(i, boundaries.size(), extractedText.pageCount());
            int pageEnd = pageStart;
            int chunkIndex = chunks.size();


            chunks.add(new DocumentChunkDraft(
                    extractedText.documentId(),
                    ChunkId.newId(),
                    chunkText,
                    chunkIndex,
                    pageStart,
                    pageEnd,
                    chunkMetadataBuilder.build(
                            extractedText,
                            chunkIndex,
                            pageStart,
                            pageEnd
                    )
            ));
        }

        return chunks;
    }

    private int estimatePage(
            int chunkIndex,
            int totalChunks,
            int pageCount
    ) {
        int safeTotalChunks = Math.max(1, totalChunks);
        int safePageCount = Math.max(1, pageCount);

        return Math.min(
                safePageCount,
                Math.max(1, ((chunkIndex * safePageCount) / safeTotalChunks) + 1)
        );
    }

    private void validate(ExtractedDocumentText extractedText) {
        if (extractedText == null) {
            throw new IllegalArgumentException("ExtractedDocumentText must not be null");
        }

        if (extractedText.documentId() == null) {
            throw new IllegalArgumentException("documentId must not be null");
        }

        if (extractedText.objectKey() == null) {
            throw new IllegalArgumentException("objectKey must not be null");
        }

        if (extractedText.fileName() == null) {
            throw new IllegalArgumentException("fileName must not be null");
        }

        if (extractedText.mimeType() == null) {
            throw new IllegalArgumentException("mimeType must not be null");
        }
    }
}