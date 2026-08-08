package com.sparrowx.agentic.exceptions;

import java.util.LinkedHashMap;
import java.util.Map;

public final class HumanGateException extends AgenticServiceException {

    private final String tenantId;
    private final String missionId;
    private final String gateId;

    public HumanGateException(String message) {
        this("", "", "", message, null);
    }

    public HumanGateException(String message, Throwable cause) {
        this("", "", "", message, cause);
    }

    public HumanGateException(String gateId, String message) {
        this("", "", gateId, message, null);
    }

    public HumanGateException(
            String tenantId,
            String missionId,
            String gateId,
            String message
    ) {
        this(tenantId, missionId, gateId, message, null);
    }

    public HumanGateException(
            String tenantId,
            String missionId,
            String gateId,
            String message,
            Throwable cause
    ) {
        super(
                "HUMAN_GATE_ERROR",
                message,
                false,
                details(tenantId, missionId, gateId),
                cause
        );
        this.tenantId = normalize(tenantId);
        this.missionId = normalize(missionId);
        this.gateId = normalize(gateId);
    }

    public String tenantId() {
        return tenantId;
    }

    public String missionId() {
        return missionId;
    }

    public String gateId() {
        return gateId;
    }

    private static Map<String, Object> details(
            String tenantId,
            String missionId,
            String gateId
    ) {
        Map<String, Object> details = new LinkedHashMap<>();

        putIfPresent(details, "tenantId", tenantId);
        putIfPresent(details, "missionId", missionId);
        putIfPresent(details, "gateId", gateId);

        return details;
    }

    private static void putIfPresent(
            Map<String, Object> target,
            String key,
            String value
    ) {
        String normalized = normalize(value);

        if (!normalized.isEmpty()) {
            target.put(key, normalized);
        }
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}