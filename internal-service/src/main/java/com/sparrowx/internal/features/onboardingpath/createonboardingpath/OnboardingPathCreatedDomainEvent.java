package com.sparrowx.internal.features.onboardingpath.createonboardingpath;

import buildingblocks.core.events.DomainEvent;

public class OnboardingPathCreatedDomainEvent extends DomainEvent {

    private final String tenantId;
    private final String onboardingPathId;
    private final String name;
    private final String slug;
    private final String targetModuleId;
    private final String actorId;
    private final String requestId;

    public OnboardingPathCreatedDomainEvent(
            String tenantId,
            String onboardingPathId,
            String name,
            String slug,
            String targetModuleId,
            String actorId,
            String requestId
    ) {
        super(onboardingPathId);
        this.tenantId = tenantId;
        this.onboardingPathId = onboardingPathId;
        this.name = name;
        this.slug = slug;
        this.targetModuleId = targetModuleId;
        this.actorId = actorId;
        this.requestId = requestId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getOnboardingPathId() {
        return onboardingPathId;
    }

    public String getName() {
        return name;
    }

    public String getSlug() {
        return slug;
    }

    public String getTargetModuleId() {
        return targetModuleId;
    }

    public String getActorId() {
        return actorId;
    }

    public String getRequestId() {
        return requestId;
    }
}