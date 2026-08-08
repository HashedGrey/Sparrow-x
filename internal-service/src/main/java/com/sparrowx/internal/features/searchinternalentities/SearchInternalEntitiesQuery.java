package com.sparrowx.internal.features.searchinternalentities;

import buildingblocks.core.queries.Query;

import java.util.List;
import java.util.Map;

public record SearchInternalEntitiesQuery(
        String tenantId,
        String actorId,
        String requestId,
        String query,
        List<String> allowedNodeTypes,
        String rootEntityId,
        String rootNodeType,
        int depth,
        int limit,
        boolean includeFuzzyMatches,
        Map<String, String> filters
) implements Query<SearchInternalEntitiesResult> {
}