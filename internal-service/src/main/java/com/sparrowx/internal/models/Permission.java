package com.sparrowx.internal.models;

import com.sparrowx.internal.valueobjects.CreatedAt;
import com.sparrowx.internal.valueobjects.PermissionId;
import com.sparrowx.internal.valueobjects.TenantId;
import com.sparrowx.internal.valueobjects.UpdatedAt;

public record Permission(
        PermissionId permissionId,
        TenantId tenantId,
        String name,
        String description,
        CreatedAt createdAt,
        UpdatedAt updatedAt
) {
    public Permission {
        if (permissionId == null) {
            throw new IllegalArgumentException("permissionId is required");
        }

        if (tenantId == null) {
            throw new IllegalArgumentException("tenantId is required");
        }

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name is required");
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
        description = description.trim();
    }

    public static Permission create(
            PermissionId permissionId,
            TenantId tenantId,
            String name,
            String description,
            CreatedAt createdAt,
            UpdatedAt updatedAt
    ) {
        return new Permission(
                permissionId,
                tenantId,
                name,
                description,
                createdAt,
                updatedAt
        );
    }
}