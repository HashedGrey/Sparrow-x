package com.sparrowx.internal.data.postgres.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(
        name = "engineer_onboarding_assignments",
        indexes = {
                @Index(
                        name = "idx_engineer_onboarding_assignments_tenant_engineer",
                        columnList = "tenant_id,engineer_id"
                ),
                @Index(
                        name = "idx_engineer_onboarding_assignments_tenant_path",
                        columnList = "tenant_id,onboarding_path_id"
                ),
                @Index(
                        name = "idx_engineer_onboarding_assignments_unique_engineer_path",
                        columnList = "tenant_id,engineer_id,onboarding_path_id",
                        unique = true
                )
        }
)
public class EngineerOnboardingAssignmentEntity {

    @Id
    @Column(name = "assignment_id", nullable = false, updatable = false)
    private String assignmentId;

    @Column(name = "tenant_id", nullable = false, updatable = false)
    private String tenantId;

    @Column(name = "engineer_id", nullable = false)
    private String engineerId;

    @Column(name = "onboarding_path_id", nullable = false)
    private String onboardingPathId;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "assigned_at", nullable = false, updatable = false)
    private Instant assignedAt;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    protected EngineerOnboardingAssignmentEntity() {
    }

    public EngineerOnboardingAssignmentEntity(
            String assignmentId,
            String tenantId,
            String engineerId,
            String onboardingPathId,
            String status,
            Instant assignedAt,
            Instant startedAt,
            Instant completedAt
    ) {
        this.assignmentId = assignmentId;
        this.tenantId = tenantId;
        this.engineerId = engineerId;
        this.onboardingPathId = onboardingPathId;
        this.status = status;
        this.assignedAt = assignedAt;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
    }

    public String getAssignmentId() {
        return assignmentId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getEngineerId() {
        return engineerId;
    }

    public String getOnboardingPathId() {
        return onboardingPathId;
    }

    public String getStatus() {
        return status;
    }

    public Instant getAssignedAt() {
        return assignedAt;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }
}