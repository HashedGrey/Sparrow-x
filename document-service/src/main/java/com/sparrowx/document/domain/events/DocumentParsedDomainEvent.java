package com.sparrowx.document.domain.events;

import buildingblocks.core.events.DomainEvent;
import lombok.Getter;

@Getter
public class DocumentParsedDomainEvent extends DomainEvent {

    private final String tenantId;
    private final String projectId;
    private final String teamId;
    private final String documentId;
    private final String ingestionJobId;
    private final String fileName;
    private final String mimeType;
    private final int pageCount;
    private final int extractedCharacters;

    public DocumentParsedDomainEvent(
            String tenantId,
            String projectId,
            String teamId,
            String documentId,
            String ingestionJobId,
            String fileName,
            String mimeType,
            int pageCount,
            int extractedCharacters
    ) {
        super(documentId);
        this.tenantId = tenantId;
        this.projectId = projectId;
        this.teamId = teamId;
        this.documentId = documentId;
        this.ingestionJobId = ingestionJobId;
        this.fileName = fileName;
        this.mimeType = mimeType;
        this.pageCount = pageCount;
        this.extractedCharacters = extractedCharacters;
    }

}