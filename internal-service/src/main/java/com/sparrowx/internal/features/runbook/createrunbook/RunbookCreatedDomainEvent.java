package com.sparrowx.internal.features.runbook.createrunbook;

import buildingblocks.core.events.DomainEvent;

public class RunbookCreatedDomainEvent extends DomainEvent {

    private final String tenantId;
    private final String runbookId;
    private final String title;
    private final String slug;
    private final String moduleId;
    private final String documentId;
    private final String actorId;
    private final String requestId;

    public RunbookCreatedDomainEvent(
            String tenantId,
            String runbookId,
            String title,
            String slug,
            String moduleId,
            String documentId,
            String actorId,
            String requestId
    ) {
        super(runbookId);
        this.tenantId = tenantId;
        this.runbookId = runbookId;
        this.title = title;
        this.slug = slug;
        this.moduleId = moduleId;
        this.documentId = documentId;
        this.actorId = actorId;
        this.requestId = requestId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getRunbookId() {
        return runbookId;
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

    public String getDocumentId() {
        return documentId;
    }

    public String getActorId() {
        return actorId;
    }

    public String getRequestId() {
        return requestId;
    }
}