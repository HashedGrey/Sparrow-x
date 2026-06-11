package com.sparrowx.internal.features.onboardingpath.createonboardingpath;

import buildingblocks.core.commands.Command;

public record CreateOnboardingPathCommand(
        String tenantId,
        String actorId,
        String requestId,
        String name,
        String description,
        String targetModuleId
) implements Command<CreateOnboardingPathResult> {
}