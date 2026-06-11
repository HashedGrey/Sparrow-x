package com.sparrowx.internal.features.onboardingtask.createonboardingtask;

import buildingblocks.core.events.DomainEvent;

public class OnboardingTaskCreatedDomainEvent extends DomainEvent {

    private final String tenantId;
    private final String onboardingTaskId;
    private final String onboardingPathId;
    private final String title;
    private final String documentId;
    private final String runbookId;
    private final int sortOrder;
    private final String actorId;
    private final String requestId;

    public OnboardingTaskCreatedDomainEvent(
            String tenantId,
            String onboardingTaskId,
            String onboardingPathId,
            String title,
            String documentId,
            String runbookId,
            int sortOrder,
            String actorId,
            String requestId
    ) {
        super(onboardingTaskId);
        this.tenantId = tenantId;
        this.onboardingTaskId = onboardingTaskId;
        this.onboardingPathId = onboardingPathId;
        this.title = title;
        this.documentId = documentId;
        this.runbookId = runbookId;
        this.sortOrder = sortOrder;
        this.actorId = actorId;
        this.requestId = requestId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getOnboardingTaskId() {
        return onboardingTaskId;
    }

    public String getOnboardingPathId() {
        return onboardingPathId;
    }

    public String getTitle() {
        return title;
    }

    public String getDocumentId() {
        return documentId;
    }

    public String getRunbookId() {
        return runbookId;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public String getActorId() {
        return actorId;
    }

    public String getRequestId() {
        return requestId;
    }
}