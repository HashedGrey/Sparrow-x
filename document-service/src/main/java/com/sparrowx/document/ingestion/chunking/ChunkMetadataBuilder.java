package com.sparrowx.document.ingestion.chunking;

import com.sparrowx.document.ingestion.extraction.ExtractedDocumentText;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class ChunkMetadataBuilder {

    public Map<String, String> build(
            ExtractedDocumentText extractedText,
            int chunkIndex,
            int pageStart,
            int pageEnd
    ) {
        if (extractedText == null) {
            return Map.of();
        }

        Map<String, String> metadata = new LinkedHashMap<>();

        if (extractedText.documentId() != null) {
            metadata.put("documentId", extractedText.documentId().value());
        }

        if (extractedText.objectKey() != null) {
            metadata.put("objectKey", extractedText.objectKey().value());
        }

        if (extractedText.fileName() != null) {
            metadata.put("fileName", extractedText.fileName().value());
        }

        if (extractedText.mimeType() != null) {
            metadata.put("mimeType", extractedText.mimeType().value());
        }

        metadata.put("chunkIndex", String.valueOf(chunkIndex));
        metadata.put("pageStart", String.valueOf(pageStart));
        metadata.put("pageEnd", String.valueOf(pageEnd));
        metadata.put("pageCount", String.valueOf(Math.max(1, extractedText.pageCount())));

        return Map.copyOf(metadata);
    }
}