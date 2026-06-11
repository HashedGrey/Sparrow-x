package com.sparrowx.internal.features.assignengineertoonboardingpath;

import buildingblocks.core.events.DomainEvent;

public class EngineerAssignedToOnboardingPathDomainEvent extends DomainEvent {

    private final String tenantId;
    private final String assignmentId;
    private final String engineerId;
    private final String onboardingPathId;
    private final String actorId;
    private final String requestId;

    public EngineerAssignedToOnboardingPathDomainEvent(
            String tenantId,
            String assignmentId,
            String engineerId,
            String onboardingPathId,
            String actorId,
            String requestId
    ) {
        super(assignmentId);
        this.tenantId = tenantId;
        this.assignmentId = assignmentId;
        this.engineerId = engineerId;
        this.onboardingPathId = onboardingPathId;
        this.actorId = actorId;
        this.requestId = requestId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getAssignmentId() {
        return assignmentId;
    }

    public String getEngineerId() {
        return engineerId;
    }

    public String getOnboardingPathId() {
        return onboardingPathId;
    }

    public String getActorId() {
        return actorId;
    }

    public String getRequestId() {
        return requestId;
    }
}