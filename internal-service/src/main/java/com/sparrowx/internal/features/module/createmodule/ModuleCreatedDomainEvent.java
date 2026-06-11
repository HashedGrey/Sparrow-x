package com.sparrowx.internal.features.module.createmodule;

import buildingblocks.core.events.DomainEvent;

public class ModuleCreatedDomainEvent extends DomainEvent {

    private final String tenantId;
    private final String moduleId;
    private final String name;
    private final String slug;
    private final String owningTeamId;
    private final String actorId;
    private final String requestId;

    public ModuleCreatedDomainEvent(
            String tenantId,
            String moduleId,
            String name,
            String slug,
            String owningTeamId,
            String actorId,
            String requestId
    ) {
        super(moduleId);
        this.tenantId = tenantId;
        this.moduleId = moduleId;
        this.name = name;
        this.slug = slug;
        this.owningTeamId = owningTeamId;
        this.actorId = actorId;
        this.requestId = requestId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getModuleId() {
        return moduleId;
    }

    public String getName() {
        return name;
    }

    public String getSlug() {
        return slug;
    }

    public String getOwningTeamId() {
        return owningTeamId;
    }

    public String getActorId() {
        return actorId;
    }

    public String getRequestId() {
        return requestId;
    }
}