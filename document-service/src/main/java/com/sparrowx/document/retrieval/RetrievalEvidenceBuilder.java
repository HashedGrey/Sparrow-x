package com.sparrowx.document.retrieval;

import com.sparrowx.document.domain.models.RetrievalEvidence;
import com.sparrowx.document.domain.valueobjects.ChunkId;
import com.sparrowx.document.domain.valueobjects.DocumentId;
import com.sparrowx.document.exceptions.InvalidDocumentException;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class RetrievalEvidenceBuilder {

    public RetrievalEvidence build(
            DocumentId documentId,
            ChunkId chunkId,
            String title,
            String fileName,
            String text,
            int pageStart,
            int pageEnd,
            double relevanceScore
    ) {
        if (documentId == null) {
            throw InvalidDocumentException.blankField("documentId");
        }

        if (chunkId == null) {
            throw InvalidDocumentException.blankField("chunkId");
        }

        if (text == null || text.isBlank()) {
            throw InvalidDocumentException.blankField("text");
        }

        int safePageStart = pageStart <= 0 ? 1 : pageStart;
        int safePageEnd = pageEnd < safePageStart ? safePageStart : pageEnd;

        return new RetrievalEvidence(
                UUID.randomUUID().toString(),
                documentId,
                chunkId,
                title,
                fileName,
                text,
                safePageStart,
                safePageEnd,
                relevanceScore,
                buildCitation(documentId, safePageStart, safePageEnd)
        );
    }

    public String buildCitation(
            DocumentId documentId,
            int pageStart,
            int pageEnd
    ) {
        if (documentId == null) {
            throw InvalidDocumentException.blankField("documentId");
        }

        int safePageStart = pageStart <= 0 ? 1 : pageStart;
        int safePageEnd = pageEnd < safePageStart ? safePageStart : pageEnd;

        return "document:%s pages:%d-%d"
                .formatted(documentId.value(), safePageStart, safePageEnd);
    }
}