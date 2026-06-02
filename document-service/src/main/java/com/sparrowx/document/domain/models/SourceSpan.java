package com.sparrowx.document.domain.models;

import com.sparrowx.document.domain.valueobjects.ChunkId;
import com.sparrowx.document.domain.valueobjects.DocumentId;

import java.util.Map;

public record SourceSpan(
        String sourceSpanId,
        SourceKind sourceKind,
        DocumentId documentId,
        ChunkId chunkId,
        String claimId,
        String title,
        String fileName,
        int pageStart,
        int pageEnd,
        String citation,
        String excerpt,
        double relevanceScore,
        Map<String, String> metadata
) {
    public SourceSpan {
        sourceKind = sourceKind == null ? SourceKind.CHUNK : sourceKind;
        claimId = claimId == null ? "" : claimId;
        title = title == null ? "" : title;
        fileName = fileName == null ? "" : fileName;
        citation = citation == null ? "" : citation;
        excerpt = excerpt == null ? "" : excerpt;
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);

        int safePageStart = pageStart <= 0 ? 1 : pageStart;
        int safePageEnd = Math.max(pageEnd, safePageStart);

        pageStart = safePageStart;
        pageEnd = safePageEnd;
    }

    public enum SourceKind {
        DOCUMENT_METADATA,
        CHUNK,
        CLAIM
    }
}