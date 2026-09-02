package com.sparrowx.document.retrieval;

import com.sparrowx.document.domain.valueobjects.DocumentId;
import com.sparrowx.document.domain.valueobjects.TenantId;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface DocumentMetadataLookup {

    Map<DocumentId, DocumentMetadata> findByTenantIdAndDocumentIds(
            TenantId tenantId,
            Set<DocumentId> documentIds
    );

    List<DocumentMetadata> findReadyByTenantIdAndFileName(
            TenantId tenantId,
            String fileName
    );

    List<DocumentMetadata> findReadyByTenantIdAndTitle(
            TenantId tenantId,
            String title
    );

    record DocumentMetadata(
            DocumentId documentId,
            String title,
            String fileName
    ) {
    }
}