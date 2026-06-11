package com.sparrowx.internal.features.module.createmodule;

import buildingblocks.core.commands.Command;

public record CreateModuleCommand(
        String tenantId,
        String actorId,
        String requestId,
        String name,
        String description,
        String owningTeamId
) implements Command<CreateModuleResult> {
}