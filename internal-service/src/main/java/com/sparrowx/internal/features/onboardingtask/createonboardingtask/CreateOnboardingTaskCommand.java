package com.sparrowx.internal.features.onboardingtask.createonboardingtask;

import buildingblocks.core.commands.Command;

public record CreateOnboardingTaskCommand(
        String tenantId,
        String actorId,
        String requestId,
        String onboardingPathId,
        String title,
        String description,
        String documentId,
        String runbookId,
        int sortOrder
) implements Command<CreateOnboardingTaskResult> {
}