package com.sparrowx.document.retrieval;

import com.sparrowx.document.domain.models.RetrievalEvidence;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
public class RetrievalPermissionFilter {

    public List<RetrievalEvidence> filter(
            RetrievalPolicy policy,
            List<RetrievalEvidence> evidence
    ) {
        if (evidence == null || evidence.isEmpty()) {
            return List.of();
        }

        if (policy == null) {
            return List.of();
        }

        return evidence.stream()
                .filter(Objects::nonNull)
                .filter(item -> item.documentId() != null)
                .filter(item -> policy.canReadDocument(item.documentId()))
                .toList();
    }
}