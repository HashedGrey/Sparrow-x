package com.sparrowx.document.domain.events;

import buildingblocks.core.events.DomainEvent;
import lombok.Getter;

@Getter
public class DocumentUploadedDomainEvent extends DomainEvent {

    private final String tenantId;
    private final String projectId;
    private final String teamId;
    private final String documentId;
    private final String ingestionJobId;
    private final String fileName;
    private final String mimeType;
    private final long sizeBytes;
    private final String contentHash;
    private final String objectKey;
    private final String uploadedByUserId;

    public DocumentUploadedDomainEvent(
            String tenantId,
            String projectId,
            String teamId,
            String documentId,
            String ingestionJobId,
            String fileName,
            String mimeType,
            long sizeBytes,
            String contentHash,
            String objectKey,
            String uploadedByUserId
    ) {
        super(documentId);
        this.tenantId = tenantId;
        this.projectId = projectId;
        this.teamId = teamId;
        this.documentId = documentId;
        this.ingestionJobId = ingestionJobId;
        this.fileName = fileName;
        this.mimeType = mimeType;
        this.sizeBytes = sizeBytes;
        this.contentHash = contentHash;
        this.objectKey = objectKey;
        this.uploadedByUserId = uploadedByUserId;
    }

}