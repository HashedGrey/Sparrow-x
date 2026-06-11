package com.sparrowx.internal.features.onboardingtask.getonboardingtask;

import buildingblocks.core.queries.Query;

public record GetOnboardingTaskQuery(
        String tenantId,
        String actorId,
        String requestId,
        String onboardingTaskId
) implements Query<GetOnboardingTaskResult> {
}