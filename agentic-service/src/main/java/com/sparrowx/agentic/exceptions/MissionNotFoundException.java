package com.sparrowx.agentic.exceptions;

import java.util.LinkedHashMap;
import java.util.Map;

public final class MissionNotFoundException
        extends AgenticServiceException {

    private final String tenantId;
    private final String missionId;

    public MissionNotFoundException(String missionId) {
        this("", missionId);
    }

    public MissionNotFoundException(
            String tenantId,
            String missionId
    ) {
        super(
                "MISSION_NOT_FOUND",
                message(tenantId, missionId),
                false,
                details(tenantId, missionId)
        );
        this.tenantId = normalize(tenantId);
        this.missionId = normalize(missionId);
    }

    public String tenantId() {
        return tenantId;
    }

    public String missionId() {
        return missionId;
    }

    private static String message(String tenantId, String missionId) {
        String normalizedTenantId = normalize(tenantId);
        String normalizedMissionId = normalize(missionId);

        if (normalizedTenantId.isEmpty()) {
            return "Mission not found: " + normalizedMissionId;
        }

        return "Mission " + normalizedMissionId
                + " was not found for tenant "
                + normalizedTenantId + ".";
    }

    private static Map<String, Object> details(
            String tenantId,
            String missionId
    ) {
        Map<String, Object> details = new LinkedHashMap<>();

        String normalizedTenantId = normalize(tenantId);
        String normalizedMissionId = normalize(missionId);

        if (!normalizedTenantId.isEmpty()) {
            details.put("tenantId", normalizedTenantId);
        }

        if (!normalizedMissionId.isEmpty()) {
            details.put("missionId", normalizedMissionId);
        }

        return details;
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}