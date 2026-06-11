package com.sparrowx.internal.data.postgres.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(
        name = "permissions",
        indexes = {
                @Index(
                        name = "idx_permissions_tenant_name",
                        columnList = "tenant_id,name",
                        unique = true
                )
        }
)
public class PermissionEntity {

    @Id
    @Column(name = "permission_id", nullable = false, updatable = false)
    private String permissionId;

    @Column(name = "tenant_id", nullable = false, updatable = false)
    private String tenantId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", nullable = false, columnDefinition = "text")
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected PermissionEntity() {
    }

    public PermissionEntity(
            String permissionId,
            String tenantId,
            String name,
            String description,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.permissionId = permissionId;
        this.tenantId = tenantId;
        this.name = name;
        this.description = description;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getPermissionId() {
        return permissionId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}