package com.sparrowx.agentic.observability;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Logs business mission lifecycle transitions.
 */
@Component
public final class MissionLifecycleLogger {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(MissionLifecycleLogger.class);

    public void submitted(
            String tenantId,
            String missionId,
            String requestId,
            String selectedPath
    ) {
        LOGGER.atInfo()
                .addKeyValue(MissionTraceTags.TENANT_ID, safe(tenantId))
                .addKeyValue(MissionTraceTags.MISSION_ID, safe(missionId))
                .addKeyValue(MissionTraceTags.REQUEST_ID, safe(requestId))
                .addKeyValue(
                        MissionTraceTags.MISSION_PATH,
                        safe(selectedPath)
                )
                .log("Mission submitted");
    }

    public void started(
            String tenantId,
            String missionId,
            String workflowId,
            String runId
    ) {
        LOGGER.atInfo()
                .addKeyValue(MissionTraceTags.TENANT_ID, safe(tenantId))
                .addKeyValue(MissionTraceTags.MISSION_ID, safe(missionId))
                .addKeyValue(MissionTraceTags.WORKFLOW_ID, safe(workflowId))
                .addKeyValue(MissionTraceTags.RUN_ID, safe(runId))
                .log("Mission started");
    }

    public void waitingForApproval(
            String tenantId,
            String missionId,
            String gateId
    ) {
        LOGGER.atInfo()
                .addKeyValue(MissionTraceTags.TENANT_ID, safe(tenantId))
                .addKeyValue(MissionTraceTags.MISSION_ID, safe(missionId))
                .addKeyValue(MissionTraceTags.GATE_ID, safe(gateId))
                .log("Mission waiting for approval");
    }

    public void resumed(
            String tenantId,
            String missionId,
            String gateId,
            String reviewerId
    ) {
        LOGGER.atInfo()
                .addKeyValue(MissionTraceTags.TENANT_ID, safe(tenantId))
                .addKeyValue(MissionTraceTags.MISSION_ID, safe(missionId))
                .addKeyValue(MissionTraceTags.GATE_ID, safe(gateId))
                .addKeyValue(MissionTraceTags.ACTOR_ID, safe(reviewerId))
                .log("Mission resumed after approval");
    }

    public void completed(
            String tenantId,
            String missionId,
            Duration elapsed
    ) {
        LOGGER.atInfo()
                .addKeyValue(MissionTraceTags.TENANT_ID, safe(tenantId))
                .addKeyValue(MissionTraceTags.MISSION_ID, safe(missionId))
                .addKeyValue(
                        MissionTraceTags.ELAPSED_MILLIS,
                        millis(elapsed)
                )
                .log("Mission completed");
    }

    public void cancelled(
            String tenantId,
            String missionId,
            String actorId,
            String reason
    ) {
        LOGGER.atInfo()
                .addKeyValue(MissionTraceTags.TENANT_ID, safe(tenantId))
                .addKeyValue(MissionTraceTags.MISSION_ID, safe(missionId))
                .addKeyValue(MissionTraceTags.ACTOR_ID, safe(actorId))
                .addKeyValue(
                        MissionTraceTags.REASON_PRESENT,
                        reason != null && !reason.isBlank()
                )
                .log("Mission cancelled");
    }

    public void failed(
            String tenantId,
            String missionId,
            String errorCode,
            boolean retryable,
            Throwable cause
    ) {
        var event = LOGGER.atError()
                .addKeyValue(MissionTraceTags.TENANT_ID, safe(tenantId))
                .addKeyValue(MissionTraceTags.MISSION_ID, safe(missionId))
                .addKeyValue(MissionTraceTags.ERROR_CODE, safe(errorCode))
                .addKeyValue(MissionTraceTags.RETRYABLE, retryable);

        if (cause != null) {
            event.setCause(cause);
        }

        event.log("Mission failed");
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private static long millis(Duration duration) {
        return duration == null
                ? 0L
                : Math.max(0L, duration.toMillis());
    }
}