package com.sparrowx.document.retrieval;

import com.sparrowx.document.domain.models.RetrievalEvidence;
import com.sparrowx.document.domain.valueobjects.DocumentId;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Set;

@Component
public class ContextAccessFilter {

    public List<RetrievalEvidence> filterByDocumentIds(
            List<RetrievalEvidence> evidence,
            Set<DocumentId> allowedDocumentIds
    ) {
        if (evidence == null || evidence.isEmpty()) {
            return List.of();
        }

        if (allowedDocumentIds == null || allowedDocumentIds.isEmpty()) {
            return evidence.stream()
                    .filter(Objects::nonNull)
                    .toList();
        }

        return evidence.stream()
                .filter(Objects::nonNull)
                .filter(item -> item.documentId() != null)
                .filter(item -> allowedDocumentIds.contains(item.documentId()))
                .toList();
    }
}