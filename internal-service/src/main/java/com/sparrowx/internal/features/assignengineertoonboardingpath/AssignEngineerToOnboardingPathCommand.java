package com.sparrowx.internal.features.assignengineertoonboardingpath;

import buildingblocks.core.commands.Command;

public record AssignEngineerToOnboardingPathCommand(
        String tenantId,
        String actorId,
        String requestId,
        String engineerId,
        String onboardingPathId
) implements Command<AssignEngineerToOnboardingPathResult> {
}