package com.sparrowx.internal.data.postgres.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(
        name = "runbooks",
        indexes = {
                @Index(
                        name = "idx_runbooks_tenant_slug",
                        columnList = "tenant_id,slug",
                        unique = true
                ),
                @Index(
                        name = "idx_runbooks_tenant_module",
                        columnList = "tenant_id,module_id"
                ),
                @Index(
                        name = "idx_runbooks_tenant_document",
                        columnList = "tenant_id,document_id"
                )
        }
)
public class RunbookEntity {

    @Id
    @Column(name = "runbook_id", nullable = false, updatable = false)
    private String runbookId;

    @Column(name = "tenant_id", nullable = false, updatable = false)
    private String tenantId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "slug", nullable = false)
    private String slug;

    @Column(name = "summary", nullable = false, columnDefinition = "text")
    private String summary;

    @Column(name = "module_id", nullable = false)
    private String moduleId;

    @Column(name = "document_id", nullable = false)
    private String documentId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected RunbookEntity() {
    }

    public RunbookEntity(
            String runbookId,
            String tenantId,
            String title,
            String slug,
            String summary,
            String moduleId,
            String documentId,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.runbookId = runbookId;
        this.tenantId = tenantId;
        this.title = title;
        this.slug = slug;
        this.summary = summary;
        this.moduleId = moduleId;
        this.documentId = documentId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getRunbookId() {
        return runbookId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getTitle() {
        return title;
    }

    public String getSlug() {
        return slug;
    }

    public String getSummary() {
        return summary;
    }

    public String getModuleId() {
        return moduleId;
    }

    public String getDocumentId() {
        return documentId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}