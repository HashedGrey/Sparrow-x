package com.sparrowx.internal.models;

import com.sparrowx.internal.valueobjects.CreatedAt;
import com.sparrowx.internal.valueobjects.InternalDocumentId;
import com.sparrowx.internal.valueobjects.ModuleId;
import com.sparrowx.internal.valueobjects.RunbookId;
import com.sparrowx.internal.valueobjects.TenantId;
import com.sparrowx.internal.valueobjects.UpdatedAt;

public record Runbook(
        RunbookId runbookId,
        TenantId tenantId,
        String title,
        String slug,
        String summary,
        ModuleId moduleId,
        InternalDocumentId documentId,
        CreatedAt createdAt,
        UpdatedAt updatedAt
) {
    public Runbook {
        if (runbookId == null) {
            throw new IllegalArgumentException("runbookId is required");
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

        if (documentId == null) {
            throw new IllegalArgumentException("documentId is required");
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
    }

    public static Runbook create(
            RunbookId runbookId,
            TenantId tenantId,
            String title,
            String slug,
            String summary,
            ModuleId moduleId,
            InternalDocumentId documentId,
            CreatedAt createdAt,
            UpdatedAt updatedAt
    ) {
        return new Runbook(
                runbookId,
                tenantId,
                title,
                slug,
                summary,
                moduleId,
                documentId,
                createdAt,
                updatedAt
        );
    }
}