-- ============================================================
-- Document Service - Initial PostgreSQL schema
-- ============================================================

-- =========================
-- documents
-- =========================

CREATE TABLE documents (
                           document_id         VARCHAR(64) PRIMARY KEY,
                           tenant_id           VARCHAR(255) NOT NULL,
                           project_id          VARCHAR(255),
                           team_id             VARCHAR(255),

                           title               VARCHAR(255),
                           file_name           VARCHAR(255) NOT NULL,
                           mime_type           VARCHAR(255) NOT NULL,
                           size_bytes          BIGINT NOT NULL,

                           object_key          VARCHAR(255),
                           content_hash        VARCHAR(255),

                           status              VARCHAR(64) NOT NULL,

                           created_at          TIMESTAMPTZ NOT NULL,
                           updated_at          TIMESTAMPTZ NOT NULL,
                           created_by_user_id  VARCHAR(255) NOT NULL
);

CREATE INDEX idx_documents_tenant_id
    ON documents (tenant_id);

CREATE INDEX idx_documents_tenant_project
    ON documents (tenant_id, project_id);

CREATE INDEX idx_documents_tenant_team
    ON documents (tenant_id, team_id);

CREATE INDEX idx_documents_status
    ON documents (status);

CREATE INDEX idx_documents_created_at
    ON documents (created_at);


-- =========================
-- ingestion_jobs
-- =========================

CREATE TABLE ingestion_jobs (
                                ingestion_job_id    VARCHAR(64) PRIMARY KEY,
                                document_id         VARCHAR(64) NOT NULL,
                                tenant_id           VARCHAR(255) NOT NULL,

                                status              VARCHAR(64) NOT NULL,
                                failure_reason      TEXT,

                                chunks_created      INTEGER NOT NULL DEFAULT 0,
                                chunks_indexed      INTEGER NOT NULL DEFAULT 0,

                                created_at          TIMESTAMPTZ NOT NULL,
                                completed_at        TIMESTAMPTZ,

                                CONSTRAINT fk_ingestion_jobs_document
                                    FOREIGN KEY (document_id)
                                        REFERENCES documents (document_id)
                                        ON DELETE CASCADE
);

CREATE INDEX idx_ingestion_jobs_document_id
    ON ingestion_jobs (document_id);

CREATE INDEX idx_ingestion_jobs_status
    ON ingestion_jobs (status);

CREATE INDEX idx_ingestion_jobs_created_at
    ON ingestion_jobs (created_at);


-- =========================
-- document_chunks
-- =========================

CREATE TABLE document_chunks (
                                 id                  BIGSERIAL PRIMARY KEY,

                                 chunk_id            VARCHAR(64) NOT NULL UNIQUE,
                                 document_id         VARCHAR(64) NOT NULL,
                                 tenant_id           VARCHAR(64) NOT NULL,
                                 project_id          VARCHAR(64),
                                 team_id             VARCHAR(64),

                                 chunk_index         INTEGER NOT NULL,

                                 chunk_text          TEXT NOT NULL,

                                 page_start          INTEGER NOT NULL DEFAULT 0,
                                 page_end            INTEGER NOT NULL DEFAULT 0,

                                 content_hash        VARCHAR(128) NOT NULL,
                                 token_count         INTEGER,

                                 embedding_id        VARCHAR(128),
                                 search_document_id  VARCHAR(128),

                                 created_at          TIMESTAMPTZ NOT NULL,

                                 CONSTRAINT fk_document_chunks_document
                                     FOREIGN KEY (document_id)
                                         REFERENCES documents (document_id)
                                         ON DELETE CASCADE,

                                 CONSTRAINT uq_document_chunks_document_index
                                     UNIQUE (document_id, chunk_index)
);

CREATE INDEX idx_document_chunks_document_id
    ON document_chunks (document_id);

CREATE INDEX idx_document_chunks_tenant_document
    ON document_chunks (tenant_id, document_id);

CREATE INDEX idx_document_chunks_tenant_project
    ON document_chunks (tenant_id, project_id);

CREATE INDEX idx_document_chunks_tenant_team
    ON document_chunks (tenant_id, team_id);

CREATE INDEX idx_document_chunks_embedding_id
    ON document_chunks (embedding_id);

CREATE INDEX idx_document_chunks_search_document_id
    ON document_chunks (search_document_id);

CREATE INDEX idx_document_chunks_content_hash
    ON document_chunks (content_hash);