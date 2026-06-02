package com.sparrowx.document.ingestion.indexing;

import com.sparrowx.document.domain.valueobjects.DocumentId;
import com.sparrowx.document.domain.valueobjects.IngestionJobId;
import com.sparrowx.document.domain.valueobjects.ProjectId;
import com.sparrowx.document.domain.valueobjects.TeamId;
import com.sparrowx.document.domain.valueobjects.TenantId;
import com.sparrowx.document.ingestion.chunking.DocumentChunkDraft;

import java.util.List;

public record DocumentChunkIndexRequest(
        IngestionJobId ingestionJobId,
        TenantId tenantId,
        ProjectId projectId,
        TeamId teamId,
        DocumentId documentId,
        List<DocumentChunkDraft> chunks
) {
    public DocumentChunkIndexRequest {
        chunks = chunks == null ? List.of() : List.copyOf(chunks);
    }
}