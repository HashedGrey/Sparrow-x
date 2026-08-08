package com.sparrowx.agentic.observability;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Logs downstream calls without recording request or response payloads.
 */
@Component
public final class ToolCallLogger {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(ToolCallLogger.class);

    public void started(
            String tenantId,
            String missionId,
            String toolName,
            String operation,
            String callId,
            int attempt
    ) {
        LOGGER.atInfo()
                .addKeyValue(MissionTraceTags.TENANT_ID, safe(tenantId))
                .addKeyValue(MissionTraceTags.MISSION_ID, safe(missionId))
                .addKeyValue(MissionTraceTags.TOOL_NAME, safe(toolName))
                .addKeyValue(
                        MissionTraceTags.TOOL_OPERATION,
                        safe(operation)
                )
                .addKeyValue(MissionTraceTags.TOOL_CALL_ID, safe(callId))
                .addKeyValue(
                        MissionTraceTags.ATTEMPT,
                        Math.max(1, attempt)
                )
                .log("Tool call started");
    }

    public void succeeded(
            String tenantId,
            String missionId,
            String toolName,
            String operation,
            String callId,
            Duration elapsed
    ) {
        LOGGER.atInfo()
                .addKeyValue(MissionTraceTags.TENANT_ID, safe(tenantId))
                .addKeyValue(MissionTraceTags.MISSION_ID, safe(missionId))
                .addKeyValue(MissionTraceTags.TOOL_NAME, safe(toolName))
                .addKeyValue(
                        MissionTraceTags.TOOL_OPERATION,
                        safe(operation)
                )
                .addKeyValue(MissionTraceTags.TOOL_CALL_ID, safe(callId))
                .addKeyValue(
                        MissionTraceTags.ELAPSED_MILLIS,
                        millis(elapsed)
                )
                .log("Tool call succeeded");
    }

    public void failed(
            String tenantId,
            String missionId,
            String toolName,
            String operation,
            String callId,
            Duration elapsed,
            boolean retryable,
            Throwable cause
    ) {
        var event = LOGGER.atError()
                .addKeyValue(MissionTraceTags.TENANT_ID, safe(tenantId))
                .addKeyValue(MissionTraceTags.MISSION_ID, safe(missionId))
                .addKeyValue(MissionTraceTags.TOOL_NAME, safe(toolName))
                .addKeyValue(
                        MissionTraceTags.TOOL_OPERATION,
                        safe(operation)
                )
                .addKeyValue(MissionTraceTags.TOOL_CALL_ID, safe(callId))
                .addKeyValue(
                        MissionTraceTags.ELAPSED_MILLIS,
                        millis(elapsed)
                )
                .addKeyValue(MissionTraceTags.RETRYABLE, retryable);

        if (cause != null) {
            event.setCause(cause);
        }

        event.log("Tool call failed");
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