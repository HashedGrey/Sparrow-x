package com.sparrowx.internal.features.team.getteam;

import buildingblocks.core.queries.Query;

public record GetTeamQuery(
        String tenantId,
        String actorId,
        String requestId,
        String teamId
) implements Query<GetTeamResult> {
}