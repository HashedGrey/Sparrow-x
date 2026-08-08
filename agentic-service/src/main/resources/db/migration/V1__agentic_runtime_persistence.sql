CREATE TABLE IF NOT EXISTS agentic_missions (
                                                mission_id text PRIMARY KEY,
                                                request_id text NOT NULL,
                                                tenant_id text NOT NULL,
                                                user_id text NOT NULL,
                                                username text,
                                                project_id text,
                                                team_id text,
                                                trace_id text,
                                                caller_service text,
                                                session_id text,
                                                conversation_id text,
                                                client_channel text,
                                                query text NOT NULL,
                                                status text NOT NULL,
                                                selected_path text NOT NULL,
                                                submitted_at timestamptz NOT NULL,
                                                terminal_at timestamptz,
                                                metadata jsonb NOT NULL DEFAULT '{}'::jsonb,
                                                updated_at timestamptz NOT NULL DEFAULT now()
    );

CREATE UNIQUE INDEX IF NOT EXISTS idx_agentic_missions_tenant_request
    ON agentic_missions (tenant_id, request_id);

CREATE INDEX IF NOT EXISTS idx_agentic_missions_tenant_status
    ON agentic_missions (tenant_id, status);

CREATE INDEX IF NOT EXISTS idx_agentic_missions_tenant_user
    ON agentic_missions (tenant_id, user_id);


CREATE TABLE IF NOT EXISTS agentic_runtime_events (
                                                      id bigserial PRIMARY KEY,
                                                      tenant_id text NOT NULL,
                                                      mission_id text NOT NULL,
                                                      mission_status text NOT NULL,
                                                      stage_id text,
                                                      stage_name text,
                                                      step_id text,
                                                      step_name text,
                                                      step_status text,
                                                      component_id text,
                                                      component_kind text,
                                                      component_name text,
                                                      component_input_summary jsonb NOT NULL DEFAULT '{}'::jsonb,
                                                      component_output_summary jsonb NOT NULL DEFAULT '{}'::jsonb,
                                                      component_started_at timestamptz,
                                                      component_completed_at timestamptz,
                                                      message text,
                                                      progress_percent double precision NOT NULL DEFAULT 0,
                                                      resume_token text NOT NULL,
                                                      emitted_at timestamptz NOT NULL,
                                                      metadata jsonb NOT NULL DEFAULT '{}'::jsonb
);

CREATE INDEX IF NOT EXISTS idx_agentic_runtime_events_stream
    ON agentic_runtime_events (tenant_id, mission_id, emitted_at);

CREATE UNIQUE INDEX IF NOT EXISTS idx_agentic_runtime_events_resume
    ON agentic_runtime_events (tenant_id, mission_id, resume_token);


CREATE TABLE IF NOT EXISTS agentic_checkpoints (
                                                   checkpoint_id text PRIMARY KEY,
                                                   tenant_id text NOT NULL,
                                                   mission_id text NOT NULL,
                                                   step_id text,
                                                   checkpoint_type text NOT NULL,
                                                   schema_version text NOT NULL,
                                                   payload text NOT NULL,
                                                   created_at timestamptz NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_agentic_checkpoints_tenant_mission
    ON agentic_checkpoints (tenant_id, mission_id, created_at);

CREATE INDEX IF NOT EXISTS idx_agentic_checkpoints_tenant_step
    ON agentic_checkpoints (tenant_id, step_id, created_at);

CREATE INDEX IF NOT EXISTS idx_agentic_checkpoints_tenant_type
    ON agentic_checkpoints (tenant_id, mission_id, checkpoint_type, created_at);


CREATE TABLE IF NOT EXISTS agentic_step_executions (
                                                       step_id text PRIMARY KEY,
                                                       tenant_id text NOT NULL,
                                                       mission_id text NOT NULL,
                                                       kind text NOT NULL,
                                                       status text NOT NULL,
                                                       attempt integer NOT NULL,
                                                       max_attempts integer NOT NULL,
                                                       input_checkpoint_ref text,
                                                       output_checkpoint_ref text,
                                                       failure_code text,
                                                       failure_message text,
                                                       metadata jsonb NOT NULL DEFAULT '{}'::jsonb,
                                                       created_at timestamptz NOT NULL,
                                                       updated_at timestamptz NOT NULL,
                                                       leased_at timestamptz,
                                                       started_at timestamptz,
                                                       completed_at timestamptz
);

CREATE INDEX IF NOT EXISTS idx_agentic_steps_tenant_mission
    ON agentic_step_executions (tenant_id, mission_id);

CREATE INDEX IF NOT EXISTS idx_agentic_steps_tenant_status
    ON agentic_step_executions (tenant_id, status);

CREATE INDEX IF NOT EXISTS idx_agentic_steps_runnable
    ON agentic_step_executions (tenant_id, status, created_at);

CREATE INDEX IF NOT EXISTS idx_agentic_steps_mission_status
    ON agentic_step_executions (tenant_id, mission_id, status);


CREATE TABLE IF NOT EXISTS agentic_mission_executions (
                                                          execution_id text PRIMARY KEY,
                                                          tenant_id text NOT NULL,
                                                          mission_id text NOT NULL,
                                                          status text NOT NULL,
                                                          current_step_id text,
                                                          version bigint NOT NULL,
                                                          created_at timestamptz NOT NULL,
                                                          updated_at timestamptz NOT NULL,
                                                          started_at timestamptz,
                                                          completed_at timestamptz
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_agentic_execution_tenant_mission
    ON agentic_mission_executions (tenant_id, mission_id);

CREATE INDEX IF NOT EXISTS idx_agentic_execution_status
    ON agentic_mission_executions (status);

CREATE INDEX IF NOT EXISTS idx_agentic_execution_recoverable
    ON agentic_mission_executions (status, updated_at);


CREATE TABLE IF NOT EXISTS agentic_worker_leases (
                                                     lease_id text PRIMARY KEY,
                                                     tenant_id text NOT NULL,
                                                     mission_id text NOT NULL,
                                                     step_id text NOT NULL,
                                                     worker_id text NOT NULL,
                                                     leased_at timestamptz NOT NULL,
                                                     heartbeat_at timestamptz NOT NULL,
                                                     expires_at timestamptz NOT NULL,
                                                     released boolean NOT NULL DEFAULT false
);

CREATE INDEX IF NOT EXISTS idx_agentic_leases_tenant_step_active
    ON agentic_worker_leases (tenant_id, step_id, released, expires_at);

CREATE INDEX IF NOT EXISTS idx_agentic_leases_expired
    ON agentic_worker_leases (released, expires_at);

CREATE INDEX IF NOT EXISTS idx_agentic_leases_worker
    ON agentic_worker_leases (worker_id, released);


CREATE TABLE IF NOT EXISTS agentic_human_gates (
                                                   gate_id text PRIMARY KEY,
                                                   tenant_id text NOT NULL,
                                                   mission_id text NOT NULL,
                                                   title text NOT NULL,
                                                   reason text NOT NULL,
                                                   required_reviewer_roles jsonb NOT NULL DEFAULT '[]'::jsonb,
                                                   review_payload jsonb NOT NULL DEFAULT '{}'::jsonb,
                                                   status text NOT NULL,
                                                   decided_by_user_id text,
                                                   decision_note text,
                                                   created_at timestamptz NOT NULL,
                                                   expires_at timestamptz,
                                                   decided_at timestamptz
);

CREATE INDEX IF NOT EXISTS idx_agentic_human_gates_tenant_mission
    ON agentic_human_gates (tenant_id, mission_id, created_at);

CREATE INDEX IF NOT EXISTS idx_agentic_human_gates_open
    ON agentic_human_gates (tenant_id, mission_id, status, created_at);

CREATE INDEX IF NOT EXISTS idx_agentic_human_gates_expires
    ON agentic_human_gates (status, expires_at);