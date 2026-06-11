package com.sparrowx.internal.features.getengineeronboardingprogress;

import buildingblocks.core.queries.Query;

public record GetEngineerOnboardingProgressQuery(
        String tenantId,
        String actorId,
        String requestId,
        String assignmentId
) implements Query<GetEngineerOnboardingProgressResult> {
}