package com.sparrowx.internal.models;

import com.sparrowx.internal.valueobjects.CreatedAt;
import com.sparrowx.internal.valueobjects.InternalDocumentId;
import com.sparrowx.internal.valueobjects.ModuleId;
import com.sparrowx.internal.valueobjects.RepositoryId;
import com.sparrowx.internal.valueobjects.TenantId;
import com.sparrowx.internal.valueobjects.UpdatedAt;

public record InternalDocument(
        InternalDocumentId documentId,
        TenantId tenantId,
        String title,
        String slug,
        String summary,
        ModuleId moduleId,
        RepositoryId repositoryId,
        String externalRef,
        CreatedAt createdAt,
        UpdatedAt updatedAt
) {
    public InternalDocument {
        if (documentId == null) {
            throw new IllegalArgumentException("documentId is required");
        }

        if (tenantId == null) {
            throw new IllegalArgumentException("tenantId is required");
        }

        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("title is required");
        }

        if (slug == null || slug.isBlank()) {
            throw new IllegalArgumentException("slug is required");
        }

        if (summary == null) {
            summary = "";
        }

        if (moduleId == null) {
            throw new IllegalArgumentException("moduleId is required");
        }

        if (repositoryId == null) {
            throw new IllegalArgumentException("repositoryId is required");
        }

        if (externalRef == null) {
            externalRef = "";
        }

        if (createdAt == null) {
            throw new IllegalArgumentException("createdAt is required");
        }

        if (updatedAt == null) {
            throw new IllegalArgumentException("updatedAt is required");
        }

        title = title.trim();
        slug = slug.trim();
        summary = summary.trim();
        externalRef = externalRef.trim();
    }

    public static InternalDocument create(
            InternalDocumentId documentId,
            TenantId tenantId,
            String title,
            String slug,
            String summary,
            ModuleId moduleId,
            RepositoryId repositoryId,
            String externalRef,
            CreatedAt createdAt,
            UpdatedAt updatedAt
    ) {
        return new InternalDocument(
                documentId,
                tenantId,
                title,
                slug,
                summary,
                moduleId,
                repositoryId,
                externalRef,
                createdAt,
                updatedAt
        );
    }
}