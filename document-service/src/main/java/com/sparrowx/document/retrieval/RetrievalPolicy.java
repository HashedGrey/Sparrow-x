package com.sparrowx.document.retrieval;

import com.sparrowx.document.domain.valueobjects.DocumentId;
import com.sparrowx.document.domain.valueobjects.ProjectId;
import com.sparrowx.document.domain.valueobjects.TeamId;
import com.sparrowx.document.domain.valueobjects.TenantId;
import com.sparrowx.document.domain.valueobjects.UserId;

import java.util.Set;

public record RetrievalPolicy(
        TenantId tenantId,
        UserId userId,
        ProjectId projectId,
        TeamId teamId,
        Set<DocumentId> allowedDocumentIds
) {

    public boolean canReadDocument(DocumentId documentId) {
        if (documentId == null) {
            return false;
        }

        if (allowedDocumentIds == null || allowedDocumentIds.isEmpty()) {
            return true;
        }

        return allowedDocumentIds.contains(documentId);
    }
}