package com.sparrowx.agentic.observability;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Logs Temporal Activity and mission-step outcomes.
 */
@Component
public final class MissionStepLogger {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(MissionStepLogger.class);

    public void started(
            String tenantId,
            String missionId,
            String stageId,
            String stepId,
            String stepKind,
            int attempt
    ) {
        LOGGER.atInfo()
                .addKeyValue(MissionTraceTags.TENANT_ID, safe(tenantId))
                .addKeyValue(MissionTraceTags.MISSION_ID, safe(missionId))
                .addKeyValue(MissionTraceTags.STAGE_ID, safe(stageId))
                .addKeyValue(MissionTraceTags.STEP_ID, safe(stepId))
                .addKeyValue(MissionTraceTags.STEP_KIND, safe(stepKind))
                .addKeyValue(
                        MissionTraceTags.ATTEMPT,
                        Math.max(1, attempt)
                )
                .log("Mission step started");
    }

    public void succeeded(
            String tenantId,
            String missionId,
            String stageId,
            String stepId,
            String stepKind,
            Duration elapsed
    ) {
        LOGGER.atInfo()
                .addKeyValue(MissionTraceTags.TENANT_ID, safe(tenantId))
                .addKeyValue(MissionTraceTags.MISSION_ID, safe(missionId))
                .addKeyValue(MissionTraceTags.STAGE_ID, safe(stageId))
                .addKeyValue(MissionTraceTags.STEP_ID, safe(stepId))
                .addKeyValue(MissionTraceTags.STEP_KIND, safe(stepKind))
                .addKeyValue(
                        MissionTraceTags.ELAPSED_MILLIS,
                        millis(elapsed)
                )
                .log("Mission step succeeded");
    }

    public void retryableFailure(
            String tenantId,
            String missionId,
            String stageId,
            String stepId,
            String stepKind,
            int attempt,
            Throwable cause
    ) {
        failure(
                tenantId,
                missionId,
                stageId,
                stepId,
                stepKind,
                attempt,
                true,
                cause
        );
    }

    public void terminalFailure(
            String tenantId,
            String missionId,
            String stageId,
            String stepId,
            String stepKind,
            int attempt,
            Throwable cause
    ) {
        failure(
                tenantId,
                missionId,
                stageId,
                stepId,
                stepKind,
                attempt,
                false,
                cause
        );
    }

    public void cancelled(
            String tenantId,
            String missionId,
            String stageId,
            String stepId,
            String stepKind
    ) {
        LOGGER.atInfo()
                .addKeyValue(MissionTraceTags.TENANT_ID, safe(tenantId))
                .addKeyValue(MissionTraceTags.MISSION_ID, safe(missionId))
                .addKeyValue(MissionTraceTags.STAGE_ID, safe(stageId))
                .addKeyValue(MissionTraceTags.STEP_ID, safe(stepId))
                .addKeyValue(MissionTraceTags.STEP_KIND, safe(stepKind))
                .log("Mission step cancelled");
    }

    private static void failure(
            String tenantId,
            String missionId,
            String stageId,
            String stepId,
            String stepKind,
            int attempt,
            boolean retryable,
            Throwable cause
    ) {
        var event = LOGGER.atError()
                .addKeyValue(MissionTraceTags.TENANT_ID, safe(tenantId))
                .addKeyValue(MissionTraceTags.MISSION_ID, safe(missionId))
                .addKeyValue(MissionTraceTags.STAGE_ID, safe(stageId))
                .addKeyValue(MissionTraceTags.STEP_ID, safe(stepId))
                .addKeyValue(MissionTraceTags.STEP_KIND, safe(stepKind))
                .addKeyValue(
                        MissionTraceTags.ATTEMPT,
                        Math.max(1, attempt)
                )
                .addKeyValue(MissionTraceTags.RETRYABLE, retryable);

        if (cause != null) {
            event.setCause(cause);
        }

        event.log("Mission step failed");
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