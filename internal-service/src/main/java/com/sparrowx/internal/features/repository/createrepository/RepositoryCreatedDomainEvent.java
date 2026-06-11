package com.sparrowx.internal.features.repository.createrepository;

import buildingblocks.core.events.DomainEvent;

public class RepositoryCreatedDomainEvent extends DomainEvent {

    private final String tenantId;
    private final String repositoryId;
    private final String name;
    private final String url;
    private final String moduleId;
    private final String actorId;
    private final String requestId;

    public RepositoryCreatedDomainEvent(
            String tenantId,
            String repositoryId,
            String name,
            String url,
            String moduleId,
            String actorId,
            String requestId
    ) {
        super(repositoryId);
        this.tenantId = tenantId;
        this.repositoryId = repositoryId;
        this.name = name;
        this.url = url;
        this.moduleId = moduleId;
        this.actorId = actorId;
        this.requestId = requestId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getRepositoryId() {
        return repositoryId;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public String getModuleId() {
        return moduleId;
    }

    public String getActorId() {
        return actorId;
    }

    public String getRequestId() {
        return requestId;
    }
}