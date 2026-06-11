package com.sparrowx.internal.features.onboardingpath.getonboardingpath;

import buildingblocks.core.queries.Query;

public record GetOnboardingPathQuery(
        String tenantId,
        String actorId,
        String requestId,
        String onboardingPathId
) implements Query<GetOnboardingPathResult> {
}