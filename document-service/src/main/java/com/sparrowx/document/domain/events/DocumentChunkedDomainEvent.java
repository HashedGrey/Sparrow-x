package com.sparrowx.document.domain.events;

import buildingblocks.core.events.DomainEvent;
import lombok.Getter;

@Getter
public class DocumentChunkedDomainEvent extends DomainEvent {

    private final String tenantId;
    private final String projectId;
    private final String teamId;
    private final String documentId;
    private final String ingestionJobId;
    private final int chunksCreated;

    public DocumentChunkedDomainEvent(
            String tenantId,
            String projectId,
            String teamId,
            String documentId,
            String ingestionJobId,
            int chunksCreated
    ) {
        super(documentId);
        this.tenantId = tenantId;
        this.projectId = projectId;
        this.teamId = teamId;
        this.documentId = documentId;
        this.ingestionJobId = ingestionJobId;
        this.chunksCreated = chunksCreated;
    }

}