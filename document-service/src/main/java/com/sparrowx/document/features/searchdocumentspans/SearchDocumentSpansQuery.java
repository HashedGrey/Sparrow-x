package com.sparrowx.document.features.searchdocumentspans;

import buildingblocks.core.queries.Query;
import com.sparrowx.document.domain.valueobjects.CallerService;
import com.sparrowx.document.domain.valueobjects.DocumentId;
import com.sparrowx.document.domain.valueobjects.ProjectId;
import com.sparrowx.document.domain.valueobjects.RequestId;
import com.sparrowx.document.domain.valueobjects.RetrievalMode;
import com.sparrowx.document.domain.valueobjects.SearchQueryText;
import com.sparrowx.document.domain.valueobjects.TeamId;
import com.sparrowx.document.domain.valueobjects.TenantId;
import com.sparrowx.document.domain.valueobjects.TraceId;
import com.sparrowx.document.domain.valueobjects.UserId;

import java.util.List;
import java.util.Map;

public record SearchDocumentSpansQuery(
        RequestId requestId,
        TenantId tenantId,
        UserId userId,
        ProjectId projectId,
        TeamId teamId,
        TraceId traceId,
        CallerService callerService,
        SearchQueryText query,
        RetrievalMode retrievalMode,
        DocumentScope scope,
        int limit,
        boolean includeExcerpts
) implements Query<SearchDocumentSpansResult> {

    public record DocumentScope(
            List<DocumentId> documentIds,
            List<String> fileNames,
            List<String> collectionIds,
            List<String> tags,
            Map<String, String> metadataFilters
    ) {
        public DocumentScope {
            documentIds = documentIds == null ? List.of() : List.copyOf(documentIds);
            fileNames = fileNames == null ? List.of() : List.copyOf(fileNames);
            collectionIds = collectionIds == null ? List.of() : List.copyOf(collectionIds);
            tags = tags == null ? List.of() : List.copyOf(tags);
            metadataFilters = metadataFilters == null ? Map.of() : Map.copyOf(metadataFilters);
        }
    }
}