package com.sparrowx.internal.data.postgres.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(
        name = "onboarding_paths",
        indexes = {
                @Index(
                        name = "idx_onboarding_paths_tenant_slug",
                        columnList = "tenant_id,slug",
                        unique = true
                ),
                @Index(
                        name = "idx_onboarding_paths_tenant_target_module",
                        columnList = "tenant_id,target_module_id"
                )
        }
)
public class OnboardingPathEntity {

    @Id
    @Column(name = "onboarding_path_id", nullable = false, updatable = false)
    private String onboardingPathId;

    @Column(name = "tenant_id", nullable = false, updatable = false)
    private String tenantId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "slug", nullable = false)
    private String slug;

    @Column(name = "description", nullable = false, columnDefinition = "text")
    private String description;

    @Column(name = "target_module_id", nullable = false)
    private String targetModuleId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected OnboardingPathEntity() {
    }

    public OnboardingPathEntity(
            String onboardingPathId,
            String tenantId,
            String name,
            String slug,
            String description,
            String targetModuleId,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.onboardingPathId = onboardingPathId;
        this.tenantId = tenantId;
        this.name = name;
        this.slug = slug;
        this.description = description;
        this.targetModuleId = targetModuleId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getOnboardingPathId() {
        return onboardingPathId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getName() {
        return name;
    }

    public String getSlug() {
        return slug;
    }

    public String getDescription() {
        return description;
    }

    public String getTargetModuleId() {
        return targetModuleId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}