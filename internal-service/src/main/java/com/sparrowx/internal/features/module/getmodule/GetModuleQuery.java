package com.sparrowx.internal.features.module.getmodule;

import buildingblocks.core.queries.Query;

public record GetModuleQuery(
        String tenantId,
        String actorId,
        String requestId,
        String moduleId
) implements Query<GetModuleResult> {
}