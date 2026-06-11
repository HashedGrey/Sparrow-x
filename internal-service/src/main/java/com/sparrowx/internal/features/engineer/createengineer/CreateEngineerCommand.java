package com.sparrowx.internal.features.engineer.createengineer;

import buildingblocks.core.commands.Command;

public record CreateEngineerCommand(
        String tenantId,
        String actorId,
        String requestId,
        String fullName,
        String email,
        String role
) implements Command<CreateEngineerResult> {
}