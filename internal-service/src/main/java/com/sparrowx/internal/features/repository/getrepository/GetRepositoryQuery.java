package com.sparrowx.internal.features.repository.getrepository;

import buildingblocks.core.queries.Query;

public record GetRepositoryQuery(
        String tenantId,
        String actorId,
        String requestId,
        String repositoryId
) implements Query<GetRepositoryResult> {
}