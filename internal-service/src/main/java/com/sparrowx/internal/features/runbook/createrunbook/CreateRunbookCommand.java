package com.sparrowx.internal.features.runbook.createrunbook;

import buildingblocks.core.commands.Command;

public record CreateRunbookCommand(
        String tenantId,
        String actorId,
        String requestId,
        String title,
        String summary,
        String moduleId,
        String documentId
) implements Command<CreateRunbookResult> {
}