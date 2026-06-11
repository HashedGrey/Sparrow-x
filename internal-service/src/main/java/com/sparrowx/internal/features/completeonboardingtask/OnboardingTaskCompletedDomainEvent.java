package com.sparrowx.internal.features.completeonboardingtask;

import buildingblocks.core.events.DomainEvent;

public class OnboardingTaskCompletedDomainEvent extends DomainEvent {

    private final String tenantId;
    private final String taskProgressId;
    private final String assignmentId;
    private final String onboardingTaskId;
    private final String completionNote;
    private final String actorId;
    private final String requestId;

    public OnboardingTaskCompletedDomainEvent(
            String tenantId,
            String taskProgressId,
            String assignmentId,
            String onboardingTaskId,
            String completionNote,
            String actorId,
            String requestId
    ) {
        super(taskProgressId);
        this.tenantId = tenantId;
        this.taskProgressId = taskProgressId;
        this.assignmentId = assignmentId;
        this.onboardingTaskId = onboardingTaskId;
        this.completionNote = completionNote;
        this.actorId = actorId;
        this.requestId = requestId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getTaskProgressId() {
        return taskProgressId;
    }

    public String getAssignmentId() {
        return assignmentId;
    }

    public String getOnboardingTaskId() {
        return onboardingTaskId;
    }

    public String getCompletionNote() {
        return completionNote;
    }

    public String getActorId() {
        return actorId;
    }

    public String getRequestId() {
        return requestId;
    }
}