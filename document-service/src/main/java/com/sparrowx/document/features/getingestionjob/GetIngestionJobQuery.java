package com.sparrowx.document.features.getingestionjob;

import buildingblocks.core.queries.Query;
import com.sparrowx.document.domain.valueobjects.CallerService;
import com.sparrowx.document.domain.valueobjects.IngestionJobId;
import com.sparrowx.document.domain.valueobjects.ProjectId;
import com.sparrowx.document.domain.valueobjects.RequestId;
import com.sparrowx.document.domain.valueobjects.TeamId;
import com.sparrowx.document.domain.valueobjects.TenantId;
import com.sparrowx.document.domain.valueobjects.TraceId;
import com.sparrowx.document.domain.valueobjects.UserId;

public record GetIngestionJobQuery(
        RequestId requestId,
        TenantId tenantId,
        UserId userId,
        ProjectId projectId,
        TeamId teamId,
        TraceId traceId,
        CallerService callerService,
        IngestionJobId ingestionJobId
) implements Query<GetIngestionJobResult> {
}