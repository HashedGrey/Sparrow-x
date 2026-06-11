package com.sparrowx.internal.mappers;

import com.sparrowx.internal.features.module.createmodule.CreateModuleCommand;
import com.sparrowx.internal.features.module.createmodule.CreateModuleResult;
import com.sparrowx.internal.features.module.getmodule.GetModuleQuery;
import com.sparrowx.internal.features.module.getmodule.GetModuleResult;
import com.sparrowx.internal.grpc.CreateModuleRequest;
import com.sparrowx.internal.grpc.CreateModuleResponse;
import com.sparrowx.internal.grpc.GetModuleRequest;
import com.sparrowx.internal.grpc.GetModuleResponse;
import com.sparrowx.internal.grpc.Module;

public final class ModuleMapper {

    private ModuleMapper() {
    }

    public static CreateModuleCommand toCreateModuleCommand(
            CreateModuleRequest request
    ) {
        return new CreateModuleCommand(
                InternalMapper.tenantId(request.getContext()),
                InternalMapper.actorId(request.getContext()),
                InternalMapper.requestId(request.getContext()),
                request.getName(),
                request.getDescription(),
                request.getOwningTeamId()
        );
    }

    public static CreateModuleResponse toCreateModuleResponse(
            CreateModuleResult result
    ) {
        return CreateModuleResponse.newBuilder()
                .setModule(toProto(result.module()))
                .build();
    }

    public static GetModuleQuery toGetModuleQuery(
            GetModuleRequest request
    ) {
        return new GetModuleQuery(
                InternalMapper.tenantId(request.getContext()),
                InternalMapper.actorId(request.getContext()),
                InternalMapper.requestId(request.getContext()),
                request.getModuleId()
        );
    }

    public static GetModuleResponse toGetModuleResponse(
            GetModuleResult result
    ) {
        return GetModuleResponse.newBuilder()
                .setModule(toProto(result.module()))
                .build();
    }

    public static Module toProto(
            com.sparrowx.internal.models.Module module
    ) {
        return Module.newBuilder()
                .setModuleId(InternalMapper.value(module.moduleId()))
                .setTenantId(InternalMapper.value(module.tenantId()))
                .setName(module.name())
                .setSlug(module.slug())
                .setDescription(module.description())
                .setOwningTeamId(InternalMapper.value(module.owningTeamId()))
                .setCreatedAt(InternalMapper.toTimestamp(module.createdAt()))
                .setUpdatedAt(InternalMapper.toTimestamp(module.updatedAt()))
                .build();
    }
}