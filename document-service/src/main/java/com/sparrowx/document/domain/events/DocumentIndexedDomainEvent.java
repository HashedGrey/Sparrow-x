package com.sparrowx.document.domain.events;

import buildingblocks.core.events.DomainEvent;
import lombok.Getter;

@Getter
public class DocumentIndexedDomainEvent extends DomainEvent {

    private final String tenantId;
    private final String projectId;
    private final String teamId;
    private final String documentId;
    private final String ingestionJobId;
    private final int chunksIndexed;
    private final boolean keywordIndexed;
    private final boolean vectorIndexed;

    public DocumentIndexedDomainEvent(
            String tenantId,
            String projectId,
            String teamId,
            String documentId,
            String ingestionJobId,
            int chunksIndexed,
            boolean keywordIndexed,
            boolean vectorIndexed
    ) {
        super(documentId);
        this.tenantId = tenantId;
        this.projectId = projectId;
        this.teamId = teamId;
        this.documentId = documentId;
        this.ingestionJobId = ingestionJobId;
        this.chunksIndexed = chunksIndexed;
        this.keywordIndexed = keywordIndexed;
        this.vectorIndexed = vectorIndexed;
    }

}