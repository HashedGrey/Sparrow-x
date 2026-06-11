package com.sparrowx.internal.models;

import com.sparrowx.internal.valueobjects.CreatedAt;
import com.sparrowx.internal.valueobjects.ModuleId;
import com.sparrowx.internal.valueobjects.RepositoryId;
import com.sparrowx.internal.valueobjects.TenantId;
import com.sparrowx.internal.valueobjects.UpdatedAt;

public record Repository(
        RepositoryId repositoryId,
        TenantId tenantId,
        String name,
        String url,
        ModuleId moduleId,
        CreatedAt createdAt,
        UpdatedAt updatedAt
) {
    public Repository {
        if (repositoryId == null) {
            throw new IllegalArgumentException("repositoryId is required");
        }

        if (tenantId == null) {
            throw new IllegalArgumentException("tenantId is required");
        }

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name is required");
        }

        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("url is required");
        }

        if (moduleId == null) {
            throw new IllegalArgumentException("moduleId is required");
        }

        if (createdAt == null) {
            throw new IllegalArgumentException("createdAt is required");
        }

        if (updatedAt == null) {
            throw new IllegalArgumentException("updatedAt is required");
        }

        name = name.trim();
        url = url.trim();
    }

    public static Repository create(
            RepositoryId repositoryId,
            TenantId tenantId,
            String name,
            String url,
            ModuleId moduleId,
            CreatedAt createdAt,
            UpdatedAt updatedAt
    ) {
        return new Repository(
                repositoryId,
                tenantId,
                name,
                url,
                moduleId,
                createdAt,
                updatedAt
        );
    }
}