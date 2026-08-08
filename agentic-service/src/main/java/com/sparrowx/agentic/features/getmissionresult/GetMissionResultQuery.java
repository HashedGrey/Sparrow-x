package com.sparrowx.agentic.features.getmissionresult;

import buildingblocks.core.queries.Query;

public record GetMissionResultQuery(
        String requestId,
        String tenantId,
        String userId,
        String missionId
) implements Query<GetMissionResultView> {

    public GetMissionResultQuery {
        requestId = normalize(requestId);
        tenantId = normalize(tenantId);
        userId = normalize(userId);
        missionId = normalize(missionId);
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}