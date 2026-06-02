package com.sparrowx.document.data.postgres.entities;

import com.sparrowx.document.domain.valueobjects.IngestionStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Setter
@Getter
@Entity
@Table(name = "ingestion_jobs")
public class IngestionJobEntity {

    @Id
    @Column(name = "ingestion_job_id", nullable = false, updatable = false)
    private String ingestionJobId;

    @Column(name = "document_id", nullable = false)
    private String documentId;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private IngestionStatus status;

    @Column(name = "failure_reason", length = 4000)
    private String failureReason;

    @Column(name = "chunks_created", nullable = false)
    private int chunksCreated;

    @Column(name = "chunks_indexed", nullable = false)
    private int chunksIndexed;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "completed_at")
    private Instant completedAt;

}