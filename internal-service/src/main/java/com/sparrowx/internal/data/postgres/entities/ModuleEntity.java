package com.sparrowx.internal.data.postgres.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(
        name = "modules",
        indexes = {
                @Index(
                        name = "idx_modules_tenant_slug",
                        columnList = "tenant_id,slug",
                        unique = true
                ),
                @Index(
                        name = "idx_modules_tenant_owning_team",
                        columnList = "tenant_id,owning_team_id"
                )
        }
)
public class ModuleEntity {

    @Id
    @Column(name = "module_id", nullable = false, updatable = false)
    private String moduleId;

    @Column(name = "tenant_id", nullable = false, updatable = false)
    private String tenantId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "slug", nullable = false)
    private String slug;

    @Column(name = "description", nullable = false, columnDefinition = "text")
    private String description;

    @Column(name = "owning_team_id", nullable = false)
    private String owningTeamId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected ModuleEntity() {
    }

    public ModuleEntity(
            String moduleId,
            String tenantId,
            String name,
            String slug,
            String description,
            String owningTeamId,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.moduleId = moduleId;
        this.tenantId = tenantId;
        this.name = name;
        this.slug = slug;
        this.description = description;
        this.owningTeamId = owningTeamId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getModuleId() {
        return moduleId;
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

    public String getOwningTeamId() {
        return owningTeamId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}