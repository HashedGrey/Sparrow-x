package com.sparrowx.internal.mappers;

import com.sparrowx.internal.features.getengineeronboardingprogress.OnboardingProgressTaskView;
import com.sparrowx.internal.grpc.EngineerOnboardingProgress;
import com.sparrowx.internal.grpc.OnboardingAssignmentStatus;
import com.sparrowx.internal.grpc.OnboardingTaskProgressStatus;

public final class OnboardingProgressMapper {

    private OnboardingProgressMapper() {
    }

    public static EngineerOnboardingProgress toProto(
            com.sparrowx.internal.models.EngineerOnboardingProgress progress
    ) {
        var builder = EngineerOnboardingProgress.newBuilder()
                .setAssignmentId(InternalMapper.value(progress.assignmentId()))
                .setEngineerId(InternalMapper.value(progress.engineerId()))
                .setOnboardingPathId(InternalMapper.value(progress.onboardingPathId()))
                .setAssignmentStatus(toProtoAssignmentStatus(progress.assignmentStatus()))
                .setTotalTasks(progress.totalTasks())
                .setCompletedTasks(progress.completedTasks())
                .setCompletionPercentage(progress.completionPercentage());

        progress.tasks().forEach(task ->
                builder.addTasks(toProto(task))
        );

        return builder.build();
    }

    public static com.sparrowx.internal.grpc.OnboardingProgressTaskView toProto(
            OnboardingProgressTaskView task
    ) {
        var builder = com.sparrowx.internal.grpc.OnboardingProgressTaskView
                .newBuilder()
                .setOnboardingTaskId(InternalMapper.value(task.onboardingTaskId()))
                .setTitle(task.title())
                .setDescription(task.description())
                .setStatus(toProtoTaskStatus(task.status()))
                .setSortOrder(task.sortOrder());

        var completedAt = InternalMapper.toTimestamp(task.completedAt());
        if (completedAt != null) {
            builder.setCompletedAt(completedAt);
        }

        return builder.build();
    }

    public static OnboardingAssignmentStatus toProtoAssignmentStatus(
            Object status
    ) {
        if (status == null) {
            return OnboardingAssignmentStatus.ONBOARDING_ASSIGNMENT_STATUS_UNSPECIFIED;
        }

        return switch (status.toString()) {
            case "ASSIGNED", "ONBOARDING_ASSIGNMENT_STATUS_ASSIGNED" ->
                    OnboardingAssignmentStatus.ONBOARDING_ASSIGNMENT_STATUS_ASSIGNED;
            case "IN_PROGRESS", "ONBOARDING_ASSIGNMENT_STATUS_IN_PROGRESS" ->
                    OnboardingAssignmentStatus.ONBOARDING_ASSIGNMENT_STATUS_IN_PROGRESS;
            case "COMPLETED", "ONBOARDING_ASSIGNMENT_STATUS_COMPLETED" ->
                    OnboardingAssignmentStatus.ONBOARDING_ASSIGNMENT_STATUS_COMPLETED;
            case "CANCELLED", "ONBOARDING_ASSIGNMENT_STATUS_CANCELLED" ->
                    OnboardingAssignmentStatus.ONBOARDING_ASSIGNMENT_STATUS_CANCELLED;
            default -> OnboardingAssignmentStatus.ONBOARDING_ASSIGNMENT_STATUS_UNSPECIFIED;
        };
    }

    public static OnboardingTaskProgressStatus toProtoTaskStatus(
            Object status
    ) {
        if (status == null) {
            return OnboardingTaskProgressStatus.ONBOARDING_TASK_PROGRESS_STATUS_UNSPECIFIED;
        }

        return switch (status.toString()) {
            case "NOT_STARTED", "ONBOARDING_TASK_PROGRESS_STATUS_NOT_STARTED" ->
                    OnboardingTaskProgressStatus.ONBOARDING_TASK_PROGRESS_STATUS_NOT_STARTED;
            case "IN_PROGRESS", "ONBOARDING_TASK_PROGRESS_STATUS_IN_PROGRESS" ->
                    OnboardingTaskProgressStatus.ONBOARDING_TASK_PROGRESS_STATUS_IN_PROGRESS;
            case "COMPLETED", "ONBOARDING_TASK_PROGRESS_STATUS_COMPLETED" ->
                    OnboardingTaskProgressStatus.ONBOARDING_TASK_PROGRESS_STATUS_COMPLETED;
            case "SKIPPED", "ONBOARDING_TASK_PROGRESS_STATUS_SKIPPED" ->
                    OnboardingTaskProgressStatus.ONBOARDING_TASK_PROGRESS_STATUS_SKIPPED;
            default -> OnboardingTaskProgressStatus.ONBOARDING_TASK_PROGRESS_STATUS_UNSPECIFIED;
        };
    }
}