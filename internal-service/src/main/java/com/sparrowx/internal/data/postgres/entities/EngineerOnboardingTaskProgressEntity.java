package com.sparrowx.internal.data.postgres.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(
        name = "engineer_onboarding_task_progress",
        indexes = {
                @Index(
                        name = "idx_engineer_onboarding_task_progress_tenant_assignment",
                        columnList = "tenant_id,assignment_id"
                ),
                @Index(
                        name = "idx_engineer_onboarding_task_progress_tenant_task",
                        columnList = "tenant_id,onboarding_task_id"
                ),
                @Index(
                        name = "idx_engineer_onboarding_task_progress_unique_assignment_task",
                        columnList = "tenant_id,assignment_id,onboarding_task_id",
                        unique = true
                )
        }
)
public class EngineerOnboardingTaskProgressEntity {

    @Id
    @Column(name = "task_progress_id", nullable = false, updatable = false)
    private String taskProgressId;

    @Column(name = "tenant_id", nullable = false, updatable = false)
    private String tenantId;

    @Column(name = "assignment_id", nullable = false)
    private String assignmentId;

    @Column(name = "onboarding_task_id", nullable = false)
    private String onboardingTaskId;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "completion_note", nullable = false, columnDefinition = "text")
    private String completionNote;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    protected EngineerOnboardingTaskProgressEntity() {
    }

    public EngineerOnboardingTaskProgressEntity(
            String taskProgressId,
            String tenantId,
            String assignmentId,
            String onboardingTaskId,
            String status,
            String completionNote,
            Instant startedAt,
            Instant completedAt
    ) {
        this.taskProgressId = taskProgressId;
        this.tenantId = tenantId;
        this.assignmentId = assignmentId;
        this.onboardingTaskId = onboardingTaskId;
        this.status = status;
        this.completionNote = completionNote;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
    }

    public String getTaskProgressId() {
        return taskProgressId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getAssignmentId() {
        return assignmentId;
    }

    public String getOnboardingTaskId() {
        return onboardingTaskId;
    }

    public String getStatus() {
        return status;
    }

    public String getCompletionNote() {
        return completionNote;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }
}