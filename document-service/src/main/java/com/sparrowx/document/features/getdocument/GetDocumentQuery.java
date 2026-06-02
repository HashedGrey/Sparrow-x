package com.sparrowx.document.features.getdocument;

import buildingblocks.core.queries.Query;
import com.sparrowx.document.domain.valueobjects.CallerService;
import com.sparrowx.document.domain.valueobjects.DocumentId;
import com.sparrowx.document.domain.valueobjects.ProjectId;
import com.sparrowx.document.domain.valueobjects.RequestId;
import com.sparrowx.document.domain.valueobjects.TeamId;
import com.sparrowx.document.domain.valueobjects.TenantId;
import com.sparrowx.document.domain.valueobjects.TraceId;
import com.sparrowx.document.domain.valueobjects.UserId;

public record GetDocumentQuery(
        RequestId requestId,
        TenantId tenantId,
        UserId userId,
        ProjectId projectId,
        TeamId teamId,
        TraceId traceId,
        CallerService callerService,
        DocumentId documentId
) implements Query<GetDocumentResult> {
}