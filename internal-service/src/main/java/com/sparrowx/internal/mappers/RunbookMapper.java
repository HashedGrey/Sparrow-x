package com.sparrowx.internal.mappers;

import com.sparrowx.internal.features.runbook.createrunbook.CreateRunbookCommand;
import com.sparrowx.internal.features.runbook.createrunbook.CreateRunbookResult;
import com.sparrowx.internal.features.runbook.getrunbook.GetRunbookQuery;
import com.sparrowx.internal.features.runbook.getrunbook.GetRunbookResult;
import com.sparrowx.internal.grpc.CreateRunbookRequest;
import com.sparrowx.internal.grpc.CreateRunbookResponse;
import com.sparrowx.internal.grpc.GetRunbookRequest;
import com.sparrowx.internal.grpc.GetRunbookResponse;
import com.sparrowx.internal.grpc.Runbook;

public final class RunbookMapper {

    private RunbookMapper() {
    }

    public static CreateRunbookCommand toCreateRunbookCommand(
            CreateRunbookRequest request
    ) {
        return new CreateRunbookCommand(
                InternalMapper.tenantId(request.getContext()),
                InternalMapper.actorId(request.getContext()),
                InternalMapper.requestId(request.getContext()),
                request.getTitle(),
                request.getSummary(),
                request.getModuleId(),
                request.getDocumentId()
        );
    }

    public static CreateRunbookResponse toCreateRunbookResponse(
            CreateRunbookResult result
    ) {
        return CreateRunbookResponse.newBuilder()
                .setRunbook(toProto(result.runbook()))
                .build();
    }

    public static GetRunbookQuery toGetRunbookQuery(
            GetRunbookRequest request
    ) {
        return new GetRunbookQuery(
                InternalMapper.tenantId(request.getContext()),
                InternalMapper.actorId(request.getContext()),
                InternalMapper.requestId(request.getContext()),
                request.getRunbookId()
        );
    }

    public static GetRunbookResponse toGetRunbookResponse(
            GetRunbookResult result
    ) {
        return GetRunbookResponse.newBuilder()
                .setRunbook(toProto(result.runbook()))
                .build();
    }

    public static Runbook toProto(
            com.sparrowx.internal.models.Runbook runbook
    ) {
        return Runbook.newBuilder()
                .setRunbookId(InternalMapper.value(runbook.runbookId()))
                .setTenantId(InternalMapper.value(runbook.tenantId()))
                .setTitle(runbook.title())
                .setSlug(runbook.slug())
                .setSummary(runbook.summary())
                .setModuleId(InternalMapper.value(runbook.moduleId()))
                .setDocumentId(InternalMapper.value(runbook.documentId()))
                .setCreatedAt(InternalMapper.toTimestamp(runbook.createdAt()))
                .setUpdatedAt(InternalMapper.toTimestamp(runbook.updatedAt()))
                .build();
    }
}