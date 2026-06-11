package com.sparrowx.internal.features.document.getdocument;

import buildingblocks.core.queries.Query;

public record GetDocumentQuery(
        String tenantId,
        String actorId,
        String requestId,
        String documentId
) implements Query<GetDocumentResult> {
}