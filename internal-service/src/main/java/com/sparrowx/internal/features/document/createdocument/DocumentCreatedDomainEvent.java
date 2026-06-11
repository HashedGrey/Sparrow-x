package com.sparrowx.internal.features.document.createdocument;

import buildingblocks.core.events.DomainEvent;

public class DocumentCreatedDomainEvent extends DomainEvent {

    private final String tenantId;
    private final String documentId;
    private final String title;
    private final String slug;
    private final String moduleId;
    private final String repositoryId;
    private final String externalRef;
    private final String actorId;
    private final String requestId;

    public DocumentCreatedDomainEvent(
            String tenantId,
            String documentId,
            String title,
            String slug,
            String moduleId,
            String repositoryId,
            String externalRef,
            String actorId,
            String requestId
    ) {
        super(documentId);
        this.tenantId = tenantId;
        this.documentId = documentId;
        this.title = title;
        this.slug = slug;
        this.moduleId = moduleId;
        this.repositoryId = repositoryId;
        this.externalRef = externalRef;
        this.actorId = actorId;
        this.requestId = requestId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getDocumentId() {
        return documentId;
    }

    public String getTitle() {
        return title;
    }

    public String getSlug() {
        return slug;
    }

    public String getModuleId() {
        return moduleId;
    }

    public String getRepositoryId() {
        return repositoryId;
    }

    public String getExternalRef() {
        return externalRef;
    }

    public String getActorId() {
        return actorId;
    }

    public String getRequestId() {
        return requestId;
    }
}