package com.sparrowx.agentic.observability;

/**
 * Centralized log, metric and trace attribute names.
 */
public final class MissionTraceTags {

    public static final String REQUEST_ID = "request_id";
    public static final String TENANT_ID = "tenant_id";
    public static final String USER_ID = "user_id";
    public static final String ACTOR_ID = "actor_id";
    public static final String PROJECT_ID = "project_id";
    public static final String TEAM_ID = "team_id";
    public static final String TRACE_ID = "trace_id";

    public static final String MISSION_ID = "mission_id";
    public static final String MISSION_STATUS = "mission_status";
    public static final String MISSION_PATH = "mission_path";

    public static final String WORKFLOW_ID = "workflow_id";
    public static final String RUN_ID = "run_id";

    public static final String STAGE_ID = "stage_id";
    public static final String STEP_ID = "step_id";
    public static final String STEP_KIND = "step_kind";
    public static final String STEP_STATUS = "step_status";

    public static final String COMPONENT_ID = "component_id";
    public static final String COMPONENT_KIND = "component_kind";

    public static final String TOOL_NAME = "tool_name";
    public static final String TOOL_OPERATION = "tool_operation";
    public static final String TOOL_CALL_ID = "tool_call_id";

    public static final String GATE_ID = "gate_id";
    public static final String CHECKPOINT_ID = "checkpoint_id";

    public static final String ATTEMPT = "attempt";
    public static final String OUTCOME = "outcome";
    public static final String RETRYABLE = "retryable";
    public static final String ERROR_CODE = "error_code";
    public static final String ELAPSED_MILLIS = "elapsed_ms";
    public static final String REASON_PRESENT = "reason_present";

    private MissionTraceTags() {
    }
}