package com.sparrowx.internal.features.permission.createpermission;

import buildingblocks.core.commands.Command;

public record CreatePermissionCommand(
        String tenantId,
        String actorId,
        String requestId,
        String name,
        String description
) implements Command<CreatePermissionResult> {
}