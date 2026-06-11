package com.sparrowx.internal.data.postgres.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;

import java.time.Instant;

@Getter
@Entity
@Table(
        name = "internal_documents",
        indexes = {
                @Index(
                        name = "idx_internal_documents_tenant_slug",
                        columnList = "tenant_id,slug",
                        unique = true
                ),
                @Index(
                        name = "idx_internal_documents_tenant_module",
                        columnList = "tenant_id,module_id"
                ),
                @Index(
                        name = "idx_internal_documents_tenant_repository",
                        columnList = "tenant_id,repository_id"
                ),
                @Index(
                        name = "idx_internal_documents_tenant_external_ref",
                        columnList = "tenant_id,external_ref"
                )
        }
)
public class InternalDocumentEntity {

    @Id
    @Column(name = "document_id", nullable = false, updatable = false)
    private String documentId;

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

    @Column(name = "repository_id", nullable = false)
    private String repositoryId;

    @Column(name = "external_ref", nullable = false)
    private String externalRef;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected InternalDocumentEntity() {
    }

    public InternalDocumentEntity(
            String documentId,
            String tenantId,
            String title,
            String slug,
            String summary,
            String moduleId,
            String repositoryId,
            String externalRef,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.documentId = documentId;
        this.tenantId = tenantId;
        this.title = title;
        this.slug = slug;
        this.summary = summary;
        this.moduleId = moduleId;
        this.repositoryId = repositoryId;
        this.externalRef = externalRef;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

}