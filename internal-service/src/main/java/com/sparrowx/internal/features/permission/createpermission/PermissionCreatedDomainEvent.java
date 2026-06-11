package com.sparrowx.internal.features.permission.createpermission;

import buildingblocks.core.events.DomainEvent;

public class PermissionCreatedDomainEvent extends DomainEvent {

    private final String tenantId;
    private final String permissionId;
    private final String name;
    private final String actorId;
    private final String requestId;

    public PermissionCreatedDomainEvent(
            String tenantId,
            String permissionId,
            String name,
            String actorId,
            String requestId
    ) {
        super(permissionId);
        this.tenantId = tenantId;
        this.permissionId = permissionId;
        this.name = name;
        this.actorId = actorId;
        this.requestId = requestId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getPermissionId() {
        return permissionId;
    }

    public String getName() {
        return name;
    }

    public String getActorId() {
        return actorId;
    }

    public String getRequestId() {
        return requestId;
    }
}