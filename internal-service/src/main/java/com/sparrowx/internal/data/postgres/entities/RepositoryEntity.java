package com.sparrowx.internal.data.postgres.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(
        name = "repositories",
        indexes = {
                @Index(
                        name = "idx_repositories_tenant_name",
                        columnList = "tenant_id,name"
                ),
                @Index(
                        name = "idx_repositories_tenant_module",
                        columnList = "tenant_id,module_id"
                ),
                @Index(
                        name = "idx_repositories_tenant_url",
                        columnList = "tenant_id,url",
                        unique = true
                )
        }
)
public class RepositoryEntity {

    @Id
    @Column(name = "repository_id", nullable = false, updatable = false)
    private String repositoryId;

    @Column(name = "tenant_id", nullable = false, updatable = false)
    private String tenantId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "url", nullable = false)
    private String url;

    @Column(name = "module_id", nullable = false)
    private String moduleId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected RepositoryEntity() {
    }

    public RepositoryEntity(
            String repositoryId,
            String tenantId,
            String name,
            String url,
            String moduleId,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.repositoryId = repositoryId;
        this.tenantId = tenantId;
        this.name = name;
        this.url = url;
        this.moduleId = moduleId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getRepositoryId() {
        return repositoryId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public String getModuleId() {
        return moduleId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}