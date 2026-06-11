package com.sparrowx.internal.mappers;

import com.sparrowx.internal.features.onboardingpath.createonboardingpath.CreateOnboardingPathCommand;
import com.sparrowx.internal.features.onboardingpath.createonboardingpath.CreateOnboardingPathResult;
import com.sparrowx.internal.features.onboardingpath.getonboardingpath.GetOnboardingPathQuery;
import com.sparrowx.internal.features.onboardingpath.getonboardingpath.GetOnboardingPathResult;
import com.sparrowx.internal.grpc.CreateOnboardingPathRequest;
import com.sparrowx.internal.grpc.CreateOnboardingPathResponse;
import com.sparrowx.internal.grpc.GetOnboardingPathRequest;
import com.sparrowx.internal.grpc.GetOnboardingPathResponse;
import com.sparrowx.internal.grpc.OnboardingPath;
import com.sparrowx.internal.grpc.OnboardingTask;
import com.sparrowx.internal.features.onboardingtask.createonboardingtask.CreateOnboardingTaskCommand;
import com.sparrowx.internal.features.onboardingtask.createonboardingtask.CreateOnboardingTaskResult;
import com.sparrowx.internal.features.onboardingtask.getonboardingtask.GetOnboardingTaskQuery;
import com.sparrowx.internal.features.onboardingtask.getonboardingtask.GetOnboardingTaskResult;
import com.sparrowx.internal.grpc.CreateOnboardingTaskRequest;
import com.sparrowx.internal.grpc.CreateOnboardingTaskResponse;
import com.sparrowx.internal.grpc.GetOnboardingTaskRequest;
import com.sparrowx.internal.grpc.GetOnboardingTaskResponse;
import com.sparrowx.internal.features.assignengineertoonboardingpath.AssignEngineerToOnboardingPathCommand;
import com.sparrowx.internal.features.assignengineertoonboardingpath.AssignEngineerToOnboardingPathResult;
import com.sparrowx.internal.features.completeonboardingtask.CompleteOnboardingTaskCommand;
import com.sparrowx.internal.features.completeonboardingtask.CompleteOnboardingTaskResult;
import com.sparrowx.internal.features.getengineeronboardingprogress.GetEngineerOnboardingProgressQuery;
import com.sparrowx.internal.features.getengineeronboardingprogress.GetEngineerOnboardingProgressResult;
import com.sparrowx.internal.grpc.AssignEngineerToOnboardingPathRequest;
import com.sparrowx.internal.grpc.AssignEngineerToOnboardingPathResponse;
import com.sparrowx.internal.grpc.CompleteOnboardingTaskRequest;
import com.sparrowx.internal.grpc.CompleteOnboardingTaskResponse;
import com.sparrowx.internal.grpc.EngineerOnboardingAssignment;
import com.sparrowx.internal.grpc.EngineerOnboardingTaskProgress;
import com.sparrowx.internal.grpc.GetEngineerOnboardingProgressRequest;
import com.sparrowx.internal.grpc.GetEngineerOnboardingProgressResponse;
public final class OnboardingMapper {

    private OnboardingMapper() {
    }

    public static CreateOnboardingPathCommand toCreateOnboardingPathCommand(
            CreateOnboardingPathRequest request
    ) {
        return new CreateOnboardingPathCommand(
                InternalMapper.tenantId(request.getContext()),
                InternalMapper.actorId(request.getContext()),
                InternalMapper.requestId(request.getContext()),
                request.getName(),
                request.getDescription(),
                request.getTargetModuleId()
        );
    }

    public static CreateOnboardingPathResponse toCreateOnboardingPathResponse(
            CreateOnboardingPathResult result
    ) {
        return CreateOnboardingPathResponse.newBuilder()
                .setOnboardingPath(toProto(result.onboardingPath()))
                .build();
    }

    public static GetOnboardingPathQuery toGetOnboardingPathQuery(
            GetOnboardingPathRequest request
    ) {
        return new GetOnboardingPathQuery(
                InternalMapper.tenantId(request.getContext()),
                InternalMapper.actorId(request.getContext()),
                InternalMapper.requestId(request.getContext()),
                request.getOnboardingPathId()
        );
    }

    public static GetOnboardingPathResponse toGetOnboardingPathResponse(
            GetOnboardingPathResult result
    ) {
        var builder = GetOnboardingPathResponse.newBuilder()
                .setOnboardingPath(toProto(result.onboardingPath()));

        result.tasks().forEach(task ->
                builder.addTasks(toProto(task))
        );

        return builder.build();
    }

    public static OnboardingPath toProto(
            com.sparrowx.internal.models.OnboardingPath path
    ) {
        return OnboardingPath.newBuilder()
                .setOnboardingPathId(InternalMapper.value(path.onboardingPathId()))
                .setTenantId(InternalMapper.value(path.tenantId()))
                .setName(path.name())
                .setSlug(path.slug())
                .setDescription(path.description())
                .setTargetModuleId(InternalMapper.value(path.targetModuleId()))
                .setCreatedAt(InternalMapper.toTimestamp(path.createdAt()))
                .setUpdatedAt(InternalMapper.toTimestamp(path.updatedAt()))
                .build();
    }

    public static OnboardingTask toProto(
            com.sparrowx.internal.models.OnboardingTask task
    ) {
        return OnboardingTask.newBuilder()
                .setOnboardingTaskId(InternalMapper.value(task.onboardingTaskId()))
                .setTenantId(InternalMapper.value(task.tenantId()))
                .setOnboardingPathId(InternalMapper.value(task.onboardingPathId()))
                .setTitle(task.title())
                .setDescription(task.description())
                .setDocumentId(InternalMapper.value(task.documentId()))
                .setRunbookId(InternalMapper.value(task.runbookId()))
                .setSortOrder(task.sortOrder())
                .setCreatedAt(InternalMapper.toTimestamp(task.createdAt()))
                .setUpdatedAt(InternalMapper.toTimestamp(task.updatedAt()))
                .build();
    }

    public static CreateOnboardingTaskCommand toCreateOnboardingTaskCommand(
            CreateOnboardingTaskRequest request
    ) {
        return new CreateOnboardingTaskCommand(
                InternalMapper.tenantId(request.getContext()),
                InternalMapper.actorId(request.getContext()),
                InternalMapper.requestId(request.getContext()),
                request.getOnboardingPathId(),
                request.getTitle(),
                request.getDescription(),
                request.getDocumentId(),
                request.getRunbookId(),
                request.getSortOrder()
        );
    }

    public static CreateOnboardingTaskResponse toCreateOnboardingTaskResponse(
            CreateOnboardingTaskResult result
    ) {
        return CreateOnboardingTaskResponse.newBuilder()
                .setOnboardingTask(toProto(result.onboardingTask()))
                .build();
    }

    public static GetOnboardingTaskQuery toGetOnboardingTaskQuery(
            GetOnboardingTaskRequest request
    ) {
        return new GetOnboardingTaskQuery(
                InternalMapper.tenantId(request.getContext()),
                InternalMapper.actorId(request.getContext()),
                InternalMapper.requestId(request.getContext()),
                request.getOnboardingTaskId()
        );
    }

    public static GetOnboardingTaskResponse toGetOnboardingTaskResponse(
            GetOnboardingTaskResult result
    ) {
        return GetOnboardingTaskResponse.newBuilder()
                .setOnboardingTask(toProto(result.onboardingTask()))
                .build();
    }

    public static AssignEngineerToOnboardingPathCommand toAssignEngineerToOnboardingPathCommand(
            AssignEngineerToOnboardingPathRequest request
    ) {
        return new AssignEngineerToOnboardingPathCommand(
                InternalMapper.tenantId(request.getContext()),
                InternalMapper.actorId(request.getContext()),
                InternalMapper.requestId(request.getContext()),
                request.getEngineerId(),
                request.getOnboardingPathId()
        );
    }

    public static AssignEngineerToOnboardingPathResponse toAssignEngineerToOnboardingPathResponse(
            AssignEngineerToOnboardingPathResult result
    ) {
        return AssignEngineerToOnboardingPathResponse.newBuilder()
                .setAssignment(toProto(result.assignment()))
                .build();
    }

    public static CompleteOnboardingTaskCommand toCompleteOnboardingTaskCommand(
            CompleteOnboardingTaskRequest request
    ) {
        return new CompleteOnboardingTaskCommand(
                InternalMapper.tenantId(request.getContext()),
                InternalMapper.actorId(request.getContext()),
                InternalMapper.requestId(request.getContext()),
                request.getAssignmentId(),
                request.getOnboardingTaskId(),
                request.getCompletionNote()
        );
    }

    public static CompleteOnboardingTaskResponse toCompleteOnboardingTaskResponse(
            CompleteOnboardingTaskResult result
    ) {
        return CompleteOnboardingTaskResponse.newBuilder()
                .setTaskProgress(toProto(result.taskProgress()))
                .build();
    }

    public static GetEngineerOnboardingProgressQuery toGetEngineerOnboardingProgressQuery(
            GetEngineerOnboardingProgressRequest request
    ) {
        return new GetEngineerOnboardingProgressQuery(
                InternalMapper.tenantId(request.getContext()),
                InternalMapper.actorId(request.getContext()),
                InternalMapper.requestId(request.getContext()),
                request.getAssignmentId()
        );
    }

    public static GetEngineerOnboardingProgressResponse toGetEngineerOnboardingProgressResponse(
            GetEngineerOnboardingProgressResult result
    ) {
        return GetEngineerOnboardingProgressResponse.newBuilder()
                .setProgress(OnboardingProgressMapper.toProto(result.progress()))
                .build();
    }

    public static EngineerOnboardingAssignment toProto(
            com.sparrowx.internal.models.EngineerOnboardingAssignment assignment
    ) {
        var builder = EngineerOnboardingAssignment.newBuilder()
                .setAssignmentId(InternalMapper.value(assignment.assignmentId()))
                .setTenantId(InternalMapper.value(assignment.tenantId()))
                .setEngineerId(InternalMapper.value(assignment.engineerId()))
                .setOnboardingPathId(InternalMapper.value(assignment.onboardingPathId()))
                .setStatus(OnboardingProgressMapper.toProtoAssignmentStatus(assignment.status()));

        var assignedAt = InternalMapper.toTimestamp(assignment.assignedAt());
        if (assignedAt != null) {
            builder.setAssignedAt(assignedAt);
        }

        var startedAt = InternalMapper.toTimestamp(assignment.startedAt());
        if (startedAt != null) {
            builder.setStartedAt(startedAt);
        }

        var completedAt = InternalMapper.toTimestamp(assignment.completedAt());
        if (completedAt != null) {
            builder.setCompletedAt(completedAt);
        }

        return builder.build();
    }

    public static EngineerOnboardingTaskProgress toProto(
            com.sparrowx.internal.models.EngineerOnboardingTaskProgress progress
    ) {
        var builder = EngineerOnboardingTaskProgress.newBuilder()
                .setTaskProgressId(InternalMapper.value(progress.taskProgressId()))
                .setTenantId(InternalMapper.value(progress.tenantId()))
                .setAssignmentId(InternalMapper.value(progress.assignmentId()))
                .setOnboardingTaskId(InternalMapper.value(progress.onboardingTaskId()))
                .setStatus(OnboardingProgressMapper.toProtoTaskStatus(progress.status()))
                .setCompletionNote(progress.completionNote());

        var startedAt = InternalMapper.toTimestamp(progress.startedAt());
        if (startedAt != null) {
            builder.setStartedAt(startedAt);
        }

        var completedAt = InternalMapper.toTimestamp(progress.completedAt());
        if (completedAt != null) {
            builder.setCompletedAt(completedAt);
        }

        return builder.build();
    }
}