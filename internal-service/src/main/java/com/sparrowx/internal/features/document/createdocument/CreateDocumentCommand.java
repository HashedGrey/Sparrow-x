package com.sparrowx.internal.features.document.createdocument;

import buildingblocks.core.commands.Command;

public record CreateDocumentCommand(
        String tenantId,
        String actorId,
        String requestId,
        String title,
        String summary,
        String moduleId,
        String repositoryId,
        String externalRef
) implements Command<CreateDocumentResult> {
}