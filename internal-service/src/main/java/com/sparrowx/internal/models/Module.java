package com.sparrowx.internal.models;

import com.sparrowx.internal.valueobjects.CreatedAt;
import com.sparrowx.internal.valueobjects.ModuleId;
import com.sparrowx.internal.valueobjects.TeamId;
import com.sparrowx.internal.valueobjects.TenantId;
import com.sparrowx.internal.valueobjects.UpdatedAt;

public record Module(
        ModuleId moduleId,
        TenantId tenantId,
        String name,
        String slug,
        String description,
        TeamId owningTeamId,
        CreatedAt createdAt,
        UpdatedAt updatedAt
) {
    public Module {
        if (moduleId == null) {
            throw new IllegalArgumentException("moduleId is required");
        }

        if (tenantId == null) {
            throw new IllegalArgumentException("tenantId is required");
        }

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name is required");
        }

        if (slug == null || slug.isBlank()) {
            throw new IllegalArgumentException("slug is required");
        }

        if (description == null) {
            description = "";
        }

        if (owningTeamId == null) {
            throw new IllegalArgumentException("owningTeamId is required");
        }

        if (createdAt == null) {
            throw new IllegalArgumentException("createdAt is required");
        }

        if (updatedAt == null) {
            throw new IllegalArgumentException("updatedAt is required");
        }

        name = name.trim();
        slug = slug.trim();
        description = description.trim();
    }

    public static Module create(
            ModuleId moduleId,
            TenantId tenantId,
            String name,
            String slug,
            String description,
            TeamId owningTeamId,
            CreatedAt createdAt,
            UpdatedAt updatedAt
    ) {
        return new Module(
                moduleId,
                tenantId,
                name,
                slug,
                description,
                owningTeamId,
                createdAt,
                updatedAt
        );
    }
}