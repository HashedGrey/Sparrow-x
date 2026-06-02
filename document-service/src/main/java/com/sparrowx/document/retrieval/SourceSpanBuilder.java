package com.sparrowx.document.retrieval;

import com.sparrowx.document.domain.models.RetrievalEvidence;
import com.sparrowx.document.domain.models.SourceSpan;
import com.sparrowx.document.exceptions.InvalidDocumentException;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
public class SourceSpanBuilder {

    public SourceSpan fromRetrievalEvidence(
            RetrievalEvidence evidence,
            boolean includeExcerpt
    ) {
        if (evidence == null) {
            throw InvalidDocumentException.blankField("retrievalEvidence");
        }

        if (evidence.documentId() == null) {
            throw InvalidDocumentException.blankField("documentId");
        }

        if (evidence.chunkId() == null) {
            throw InvalidDocumentException.blankField("chunkId");
        }

        String sourceSpanId = evidence.evidenceId();

        if (sourceSpanId == null || sourceSpanId.isBlank()) {
            sourceSpanId = UUID.randomUUID().toString();
        }

        return new SourceSpan(
                sourceSpanId,
                SourceSpan.SourceKind.CHUNK,
                evidence.documentId(),
                evidence.chunkId(),
                "",
                evidence.title(),
                evidence.fileName(),
                evidence.pageStart(),
                evidence.pageEnd(),
                evidence.citation(),
                includeExcerpt ? evidence.text() : "",
                evidence.relevanceScore(),
                Map.of()
        );
    }
}