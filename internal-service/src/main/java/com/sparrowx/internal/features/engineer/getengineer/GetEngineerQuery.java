package com.sparrowx.internal.features.engineer.getengineer;

import buildingblocks.core.queries.Query;

public record GetEngineerQuery(
        String tenantId,
        String actorId,
        String requestId,
        String engineerId
) implements Query<GetEngineerResult> {
}