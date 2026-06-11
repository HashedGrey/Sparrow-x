package com.sparrowx.internal.models;

import com.sparrowx.internal.valueobjects.CreatedAt;
import com.sparrowx.internal.valueobjects.TeamId;
import com.sparrowx.internal.valueobjects.TenantId;
import com.sparrowx.internal.valueobjects.UpdatedAt;

public record Team(
        TeamId teamId,
        TenantId tenantId,
        String name,
        String slug,
        String description,
        CreatedAt createdAt,
        UpdatedAt updatedAt
) {
    public Team {
        if (teamId == null) {
            throw new IllegalArgumentException("teamId is required");
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

    public static Team create(
            TeamId teamId,
            TenantId tenantId,
            String name,
            String slug,
            String description,
            CreatedAt createdAt,
            UpdatedAt updatedAt
    ) {
        return new Team(
                teamId,
                tenantId,
                name,
                slug,
                description,
                createdAt,
                updatedAt
        );
    }
}