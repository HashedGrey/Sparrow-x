package com.sparrowx.internal.features.team.createteam;

import buildingblocks.core.commands.Command;

public record CreateTeamCommand(
        String tenantId,
        String actorId,
        String requestId,
        String name,
        String description
) implements Command<CreateTeamResult> {
}