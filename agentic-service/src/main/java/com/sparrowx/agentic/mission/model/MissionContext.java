package com.sparrowx.agentic.mission.model;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Tenant, caller, project, session and tracing context
 * propagated through a mission.
 */
public record MissionContext(
        String requestId,
        String tenantId,
        String userId,
        String username,
        String projectId,
        String teamId,
        String traceId,
        String callerService,
        String sessionId,
        String conversationId,
        String clientChannel,
        Map<String, String> metadata
) {

    public MissionContext {
        requestId = normalize(requestId);
        tenantId = normalize(tenantId);
        userId = normalize(userId);
        username = normalize(username);
        projectId = normalize(projectId);
        teamId = normalize(teamId);
        traceId = normalize(traceId);
        callerService = normalize(callerService);
        sessionId = normalize(sessionId);
        conversationId = normalize(conversationId);
        clientChannel = normalize(clientChannel);
        metadata = immutableMetadata(metadata);
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private static Map<String, String> immutableMetadata(
            Map<String, String> source
    ) {
        if (source == null || source.isEmpty()) {
            return Map.of();
        }

        Map<String, String> copy = new LinkedHashMap<>();

        source.forEach((key, value) -> {
            if (key != null && value != null) {
                copy.put(key, value);
            }
        });

        return Map.copyOf(copy);
    }
}