package com.sparrowx.internal.features.completeonboardingtask;

import buildingblocks.core.commands.Command;

public record CompleteOnboardingTaskCommand(
        String tenantId,
        String actorId,
        String requestId,
        String assignmentId,
        String onboardingTaskId,
        String completionNote
) implements Command<CompleteOnboardingTaskResult> {
}