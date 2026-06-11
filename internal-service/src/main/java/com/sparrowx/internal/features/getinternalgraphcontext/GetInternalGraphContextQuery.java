package com.sparrowx.internal.features.getinternalgraphcontext;

import buildingblocks.core.queries.Query;

public record GetInternalGraphContextQuery(
        String tenantId,
        String actorId,
        String requestId,
        String graphType,
        String rootEntityId,
        String rootNodeType,
        int depth,
        int limit
) implements Query<GetInternalGraphContextResult> {
}