package com.sparrowx.internal.data.postgres.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(
        name = "onboarding_tasks",
        indexes = {
                @Index(
                        name = "idx_onboarding_tasks_tenant_path_sort",
                        columnList = "tenant_id,onboarding_path_id,sort_order"
                ),
                @Index(
                        name = "idx_onboarding_tasks_tenant_document",
                        columnList = "tenant_id,document_id"
                ),
                @Index(
                        name = "idx_onboarding_tasks_tenant_runbook",
                        columnList = "tenant_id,runbook_id"
                )
        }
)
public class OnboardingTaskEntity {

    @Id
    @Column(name = "onboarding_task_id", nullable = false, updatable = false)
    private String onboardingTaskId;

    @Column(name = "tenant_id", nullable = false, updatable = false)
    private String tenantId;

    @Column(name = "onboarding_path_id", nullable = false)
    private String onboardingPathId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", nullable = false, columnDefinition = "text")
    private String description;

    @Column(name = "document_id", nullable = false)
    private String documentId;

    @Column(name = "runbook_id", nullable = false)
    private String runbookId;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected OnboardingTaskEntity() {
    }

    public OnboardingTaskEntity(
            String onboardingTaskId,
            String tenantId,
            String onboardingPathId,
            String title,
            String description,
            String documentId,
            String runbookId,
            int sortOrder,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.onboardingTaskId = onboardingTaskId;
        this.tenantId = tenantId;
        this.onboardingPathId = onboardingPathId;
        this.title = title;
        this.description = description;
        this.documentId = documentId;
        this.runbookId = runbookId;
        this.sortOrder = sortOrder;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getOnboardingTaskId() {
        return onboardingTaskId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getOnboardingPathId() {
        return onboardingPathId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getDocumentId() {
        return documentId;
    }

    public String getRunbookId() {
        return runbookId;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}