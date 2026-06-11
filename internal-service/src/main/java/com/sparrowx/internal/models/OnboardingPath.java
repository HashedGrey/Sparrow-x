package com.sparrowx.internal.models;

import com.sparrowx.internal.valueobjects.CreatedAt;
import com.sparrowx.internal.valueobjects.ModuleId;
import com.sparrowx.internal.valueobjects.OnboardingPathId;
import com.sparrowx.internal.valueobjects.TenantId;
import com.sparrowx.internal.valueobjects.UpdatedAt;

public record OnboardingPath(
        OnboardingPathId onboardingPathId,
        TenantId tenantId,
        String name,
        String slug,
        String description,
        ModuleId targetModuleId,
        CreatedAt createdAt,
        UpdatedAt updatedAt
) {
    public OnboardingPath {
        if (onboardingPathId == null) {
            throw new IllegalArgumentException("onboardingPathId is required");
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

        if (targetModuleId == null) {
            throw new IllegalArgumentException("targetModuleId is required");
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

    public static OnboardingPath create(
            OnboardingPathId onboardingPathId,
            TenantId tenantId,
            String name,
            String slug,
            String description,
            ModuleId targetModuleId,
            CreatedAt createdAt,
            UpdatedAt updatedAt
    ) {
        return new OnboardingPath(
                onboardingPathId,
                tenantId,
                name,
                slug,
                description,
                targetModuleId,
                createdAt,
                updatedAt
        );
    }
}