package com.sparrowx.internal.models;

import com.sparrowx.internal.valueobjects.CreatedAt;
import com.sparrowx.internal.valueobjects.InternalDocumentId;
import com.sparrowx.internal.valueobjects.OnboardingPathId;
import com.sparrowx.internal.valueobjects.OnboardingTaskId;
import com.sparrowx.internal.valueobjects.RunbookId;
import com.sparrowx.internal.valueobjects.TenantId;
import com.sparrowx.internal.valueobjects.UpdatedAt;

public record OnboardingTask(
        OnboardingTaskId onboardingTaskId,
        TenantId tenantId,
        OnboardingPathId onboardingPathId,
        String title,
        String description,
        InternalDocumentId documentId,
        RunbookId runbookId,
        int sortOrder,
        CreatedAt createdAt,
        UpdatedAt updatedAt
) {
    public OnboardingTask {
        if (onboardingTaskId == null) {
            throw new IllegalArgumentException("onboardingTaskId is required");
        }

        if (tenantId == null) {
            throw new IllegalArgumentException("tenantId is required");
        }

        if (onboardingPathId == null) {
            throw new IllegalArgumentException("onboardingPathId is required");
        }

        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("title is required");
        }

        if (description == null) {
            description = "";
        }

        if (documentId == null) {
            throw new IllegalArgumentException("documentId is required");
        }

        if (runbookId == null) {
            throw new IllegalArgumentException("runbookId is required");
        }

        if (sortOrder < 0) {
            throw new IllegalArgumentException("sortOrder must not be negative");
        }

        if (createdAt == null) {
            throw new IllegalArgumentException("createdAt is required");
        }

        if (updatedAt == null) {
            throw new IllegalArgumentException("updatedAt is required");
        }

        title = title.trim();
        description = description.trim();
    }

    public static OnboardingTask create(
            OnboardingTaskId onboardingTaskId,
            TenantId tenantId,
            OnboardingPathId onboardingPathId,
            String title,
            String description,
            InternalDocumentId documentId,
            RunbookId runbookId,
            int sortOrder,
            CreatedAt createdAt,
            UpdatedAt updatedAt
    ) {
        return new OnboardingTask(
                onboardingTaskId,
                tenantId,
                onboardingPathId,
                title,
                description,
                documentId,
                runbookId,
                sortOrder,
                createdAt,
                updatedAt
        );
    }
}