package com.sparrowx.internal.features.repository.createrepository;

import buildingblocks.core.commands.Command;

public record CreateRepositoryCommand(
        String tenantId,
        String actorId,
        String requestId,
        String name,
        String url,
        String moduleId
) implements Command<CreateRepositoryResult> {
}