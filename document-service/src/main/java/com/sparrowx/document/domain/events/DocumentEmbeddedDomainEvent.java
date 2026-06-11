package com.sparrowx.document.domain.events;

import buildingblocks.core.events.DomainEvent;
import lombok.Getter;

@Getter
public class DocumentEmbeddedDomainEvent extends DomainEvent {

    private final String tenantId;
    private final String projectId;
    private final String teamId;
    private final String documentId;
    private final String ingestionJobId;
    private final int chunksEmbedded;
    private final String embeddingModel;

    public DocumentEmbeddedDomainEvent(
            String tenantId,
            String projectId,
            String teamId,
            String documentId,
            String ingestionJobId,
            int chunksEmbedded,
            String embeddingModel
    ) {
        super(documentId);
        this.tenantId = tenantId;
        this.projectId = projectId;
        this.teamId = teamId;
        this.documentId = documentId;
        this.ingestionJobId = ingestionJobId;
        this.chunksEmbedded = chunksEmbedded;
        this.embeddingModel = embeddingModel;
    }

}