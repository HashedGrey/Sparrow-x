package com.sparrowx.internal.features.engineer.createengineer;

import buildingblocks.core.events.DomainEvent;

public class EngineerCreatedDomainEvent extends DomainEvent {

    private final String tenantId;
    private final String engineerId;
    private final String fullName;
    private final String email;
    private final String role;
    private final String actorId;
    private final String requestId;

    public EngineerCreatedDomainEvent(
            String tenantId,
            String engineerId,
            String fullName,
            String email,
            String role,
            String actorId,
            String requestId
    ) {
        super(engineerId);
        this.tenantId = tenantId;
        this.engineerId = engineerId;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.actorId = actorId;
        this.requestId = requestId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getEngineerId() {
        return engineerId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    public String getActorId() {
        return actorId;
    }

    public String getRequestId() {
        return requestId;
    }
}