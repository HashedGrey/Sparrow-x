-- Add tenant ownership to ingestion jobs
ALTER TABLE ingestion_jobs
    ADD COLUMN tenant_id VARCHAR(255);

-- Backfill tenant_id from the owning document
UPDATE ingestion_jobs ij
SET tenant_id = d.tenant_id
FROM documents d
WHERE ij.document_id = d.document_id;

-- Enforce tenant_id after backfill
ALTER TABLE ingestion_jobs
    ALTER COLUMN tenant_id SET NOT NULL;

-- Fast tenant-scoped lookup by ingestion job id
CREATE INDEX idx_ingestion_jobs_tenant_job
    ON ingestion_jobs (tenant_id, ingestion_job_id);

-- Optional but useful for tenant-level job queries
CREATE INDEX idx_ingestion_jobs_tenant_status
    ON ingestion_jobs (tenant_id, status);