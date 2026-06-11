package com.sparrowx.internal.features.runbook.getrunbook;

import buildingblocks.core.queries.Query;

public record GetRunbookQuery(
        String tenantId,
        String actorId,
        String requestId,
        String runbookId
) implements Query<GetRunbookResult> {
}