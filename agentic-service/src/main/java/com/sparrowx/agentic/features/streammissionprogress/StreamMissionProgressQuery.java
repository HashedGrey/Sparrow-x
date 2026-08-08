package com.sparrowx.agentic.features.streammissionprogress;

import buildingblocks.core.queries.Query;

public record StreamMissionProgressQuery(
        String requestId,
        String tenantId,
        String userId,
        String missionId,
        String resumeToken
) implements Query<MissionEventCursor> {

    public StreamMissionProgressQuery {
        requestId = normalize(requestId);
        tenantId = normalize(tenantId);
        userId = normalize(userId);
        missionId = normalize(missionId);
        resumeToken = normalize(resumeToken);
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}
