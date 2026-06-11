-- ============================================================
-- SparrowX Internal Service
-- V001__create_internal_service_tables.sql
-- ============================================================

CREATE TABLE IF NOT EXISTS engineers (
                                         engineer_id VARCHAR(64) PRIMARY KEY,
    tenant_id VARCHAR(128) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(320) NOT NULL,
    role VARCHAR(64) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
    );

CREATE UNIQUE INDEX IF NOT EXISTS idx_engineers_tenant_email
    ON engineers (tenant_id, email);

CREATE INDEX IF NOT EXISTS idx_engineers_tenant_role
    ON engineers (tenant_id, role);


-- ============================================================
-- Teams
-- ============================================================

CREATE TABLE IF NOT EXISTS teams (
                                     team_id VARCHAR(64) PRIMARY KEY,
    tenant_id VARCHAR(128) NOT NULL,
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL,
    description TEXT NOT NULL DEFAULT '',
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
    );

CREATE UNIQUE INDEX IF NOT EXISTS idx_teams_tenant_slug
    ON teams (tenant_id, slug);

CREATE INDEX IF NOT EXISTS idx_teams_tenant_name
    ON teams (tenant_id, name);


-- ============================================================
-- Modules
-- ============================================================

CREATE TABLE IF NOT EXISTS modules (
                                       module_id VARCHAR(64) PRIMARY KEY,
    tenant_id VARCHAR(128) NOT NULL,
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL,
    description TEXT NOT NULL DEFAULT '',
    owning_team_id VARCHAR(64) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_modules_owning_team
    FOREIGN KEY (owning_team_id)
    REFERENCES teams (team_id)
    );

CREATE UNIQUE INDEX IF NOT EXISTS idx_modules_tenant_slug
    ON modules (tenant_id, slug);

CREATE INDEX IF NOT EXISTS idx_modules_tenant_owning_team
    ON modules (tenant_id, owning_team_id);


-- ============================================================
-- Repositories
-- ============================================================

CREATE TABLE IF NOT EXISTS repositories (
                                            repository_id VARCHAR(64) PRIMARY KEY,
    tenant_id VARCHAR(128) NOT NULL,
    name VARCHAR(255) NOT NULL,
    url TEXT NOT NULL,
    module_id VARCHAR(64) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_repositories_module
    FOREIGN KEY (module_id)
    REFERENCES modules (module_id)
    );

CREATE INDEX IF NOT EXISTS idx_repositories_tenant_name
    ON repositories (tenant_id, name);

CREATE INDEX IF NOT EXISTS idx_repositories_tenant_module
    ON repositories (tenant_id, module_id);

CREATE UNIQUE INDEX IF NOT EXISTS idx_repositories_tenant_url
    ON repositories (tenant_id, url);


-- ============================================================
-- Internal Documents
-- ============================================================

CREATE TABLE IF NOT EXISTS internal_documents (
                                                  document_id VARCHAR(64) PRIMARY KEY,
    tenant_id VARCHAR(128) NOT NULL,
    title VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL,
    summary TEXT NOT NULL DEFAULT '',
    module_id VARCHAR(64) NOT NULL,
    repository_id VARCHAR(64) NOT NULL,
    external_ref TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_internal_documents_module
    FOREIGN KEY (module_id)
    REFERENCES modules (module_id),

    CONSTRAINT fk_internal_documents_repository
    FOREIGN KEY (repository_id)
    REFERENCES repositories (repository_id)
    );

CREATE UNIQUE INDEX IF NOT EXISTS idx_internal_documents_tenant_slug
    ON internal_documents (tenant_id, slug);

CREATE INDEX IF NOT EXISTS idx_internal_documents_tenant_module
    ON internal_documents (tenant_id, module_id);

CREATE INDEX IF NOT EXISTS idx_internal_documents_tenant_repository
    ON internal_documents (tenant_id, repository_id);

CREATE INDEX IF NOT EXISTS idx_internal_documents_tenant_external_ref
    ON internal_documents (tenant_id, external_ref);


-- ============================================================
-- Runbooks
-- ============================================================

CREATE TABLE IF NOT EXISTS runbooks (
                                        runbook_id VARCHAR(64) PRIMARY KEY,
    tenant_id VARCHAR(128) NOT NULL,
    title VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL,
    summary TEXT NOT NULL DEFAULT '',
    module_id VARCHAR(64) NOT NULL,
    document_id VARCHAR(64) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_runbooks_module
    FOREIGN KEY (module_id)
    REFERENCES modules (module_id),

    CONSTRAINT fk_runbooks_document
    FOREIGN KEY (document_id)
    REFERENCES internal_documents (document_id)
    );

CREATE UNIQUE INDEX IF NOT EXISTS idx_runbooks_tenant_slug
    ON runbooks (tenant_id, slug);

CREATE INDEX IF NOT EXISTS idx_runbooks_tenant_module
    ON runbooks (tenant_id, module_id);

CREATE INDEX IF NOT EXISTS idx_runbooks_tenant_document
    ON runbooks (tenant_id, document_id);


-- ============================================================
-- Onboarding Paths
-- ============================================================

CREATE TABLE IF NOT EXISTS onboarding_paths (
                                                onboarding_path_id VARCHAR(64) PRIMARY KEY,
    tenant_id VARCHAR(128) NOT NULL,
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL,
    description TEXT NOT NULL DEFAULT '',
    target_module_id VARCHAR(64) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_onboarding_paths_target_module
    FOREIGN KEY (target_module_id)
    REFERENCES modules (module_id)
    );

CREATE UNIQUE INDEX IF NOT EXISTS idx_onboarding_paths_tenant_slug
    ON onboarding_paths (tenant_id, slug);

CREATE INDEX IF NOT EXISTS idx_onboarding_paths_tenant_target_module
    ON onboarding_paths (tenant_id, target_module_id);


-- ============================================================
-- Onboarding Tasks
-- ============================================================

CREATE TABLE IF NOT EXISTS onboarding_tasks (
                                                onboarding_task_id VARCHAR(64) PRIMARY KEY,
    tenant_id VARCHAR(128) NOT NULL,
    onboarding_path_id VARCHAR(64) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL DEFAULT '',
    document_id VARCHAR(64) NOT NULL,
    runbook_id VARCHAR(64) NOT NULL,
    sort_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_onboarding_tasks_path
    FOREIGN KEY (onboarding_path_id)
    REFERENCES onboarding_paths (onboarding_path_id),

    CONSTRAINT fk_onboarding_tasks_document
    FOREIGN KEY (document_id)
    REFERENCES internal_documents (document_id),

    CONSTRAINT fk_onboarding_tasks_runbook
    FOREIGN KEY (runbook_id)
    REFERENCES runbooks (runbook_id),

    CONSTRAINT chk_onboarding_tasks_sort_order_non_negative
    CHECK (sort_order >= 0)
    );

CREATE INDEX IF NOT EXISTS idx_onboarding_tasks_tenant_path_sort
    ON onboarding_tasks (tenant_id, onboarding_path_id, sort_order);

CREATE INDEX IF NOT EXISTS idx_onboarding_tasks_tenant_document
    ON onboarding_tasks (tenant_id, document_id);

CREATE INDEX IF NOT EXISTS idx_onboarding_tasks_tenant_runbook
    ON onboarding_tasks (tenant_id, runbook_id);


-- ============================================================
-- Engineer Onboarding Assignments
-- ============================================================

CREATE TABLE IF NOT EXISTS engineer_onboarding_assignments (
                                                               assignment_id VARCHAR(64) PRIMARY KEY,
    tenant_id VARCHAR(128) NOT NULL,
    engineer_id VARCHAR(64) NOT NULL,
    onboarding_path_id VARCHAR(64) NOT NULL,
    status VARCHAR(64) NOT NULL,
    assigned_at TIMESTAMPTZ NOT NULL,
    started_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,

    CONSTRAINT fk_engineer_onboarding_assignments_engineer
    FOREIGN KEY (engineer_id)
    REFERENCES engineers (engineer_id),

    CONSTRAINT fk_engineer_onboarding_assignments_path
    FOREIGN KEY (onboarding_path_id)
    REFERENCES onboarding_paths (onboarding_path_id),

    CONSTRAINT chk_engineer_onboarding_assignments_status
    CHECK (
              status IN (
              'ASSIGNED',
              'IN_PROGRESS',
              'COMPLETED',
              'CANCELLED'
                        )
    )
    );

CREATE INDEX IF NOT EXISTS idx_engineer_onboarding_assignments_tenant_engineer
    ON engineer_onboarding_assignments (tenant_id, engineer_id);

CREATE INDEX IF NOT EXISTS idx_engineer_onboarding_assignments_tenant_path
    ON engineer_onboarding_assignments (tenant_id, onboarding_path_id);

CREATE UNIQUE INDEX IF NOT EXISTS idx_engineer_onboarding_assignments_unique_engineer_path
    ON engineer_onboarding_assignments (tenant_id, engineer_id, onboarding_path_id);


-- ============================================================
-- Engineer Onboarding Task Progress
-- ============================================================

CREATE TABLE IF NOT EXISTS engineer_onboarding_task_progress (
                                                                 task_progress_id VARCHAR(64) PRIMARY KEY,
    tenant_id VARCHAR(128) NOT NULL,
    assignment_id VARCHAR(64) NOT NULL,
    onboarding_task_id VARCHAR(64) NOT NULL,
    status VARCHAR(64) NOT NULL,
    completion_note TEXT NOT NULL DEFAULT '',
    started_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,

    CONSTRAINT fk_engineer_onboarding_task_progress_assignment
    FOREIGN KEY (assignment_id)
    REFERENCES engineer_onboarding_assignments (assignment_id),

    CONSTRAINT fk_engineer_onboarding_task_progress_task
    FOREIGN KEY (onboarding_task_id)
    REFERENCES onboarding_tasks (onboarding_task_id),

    CONSTRAINT chk_engineer_onboarding_task_progress_status
    CHECK (
              status IN (
              'NOT_STARTED',
              'IN_PROGRESS',
              'COMPLETED',
              'SKIPPED'
                        )
    )
    );

CREATE INDEX IF NOT EXISTS idx_engineer_onboarding_task_progress_tenant_assignment
    ON engineer_onboarding_task_progress (tenant_id, assignment_id);

CREATE INDEX IF NOT EXISTS idx_engineer_onboarding_task_progress_tenant_task
    ON engineer_onboarding_task_progress (tenant_id, onboarding_task_id);

CREATE UNIQUE INDEX IF NOT EXISTS idx_engineer_onboarding_task_progress_unique_assignment_task
    ON engineer_onboarding_task_progress (tenant_id, assignment_id, onboarding_task_id);


-- ============================================================
-- Permissions
-- ============================================================

CREATE TABLE IF NOT EXISTS permissions (
                                           permission_id VARCHAR(64) PRIMARY KEY,
    tenant_id VARCHAR(128) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT NOT NULL DEFAULT '',
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
    );

CREATE UNIQUE INDEX IF NOT EXISTS idx_permissions_tenant_name
    ON permissions (tenant_id, name);