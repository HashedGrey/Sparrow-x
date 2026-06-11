package com.sparrowx.internal.mappers;

import com.sparrowx.internal.features.engineer.createengineer.CreateEngineerCommand;
import com.sparrowx.internal.features.engineer.createengineer.CreateEngineerResult;
import com.sparrowx.internal.features.engineer.getengineer.GetEngineerQuery;
import com.sparrowx.internal.features.engineer.getengineer.GetEngineerResult;
import com.sparrowx.internal.grpc.CreateEngineerRequest;
import com.sparrowx.internal.grpc.CreateEngineerResponse;
import com.sparrowx.internal.grpc.Engineer;
import com.sparrowx.internal.grpc.EngineerRole;
import com.sparrowx.internal.grpc.GetEngineerRequest;
import com.sparrowx.internal.grpc.GetEngineerResponse;

public final class EngineerMapper {

    private EngineerMapper() {
    }

    public static CreateEngineerCommand toCreateEngineerCommand(
            CreateEngineerRequest request
    ) {
        return new CreateEngineerCommand(
                InternalMapper.tenantId(request.getContext()),
                InternalMapper.actorId(request.getContext()),
                InternalMapper.requestId(request.getContext()),
                request.getFullName(),
                request.getEmail(),
                request.getRole().name()
        );
    }

    public static CreateEngineerResponse toCreateEngineerResponse(
            CreateEngineerResult result
    ) {
        return CreateEngineerResponse.newBuilder()
                .setEngineer(toProto(result.engineer()))
                .build();
    }

    public static GetEngineerQuery toGetEngineerQuery(
            GetEngineerRequest request
    ) {
        return new GetEngineerQuery(
                InternalMapper.tenantId(request.getContext()),
                InternalMapper.actorId(request.getContext()),
                InternalMapper.requestId(request.getContext()),
                request.getEngineerId()
        );
    }

    public static GetEngineerResponse toGetEngineerResponse(
            GetEngineerResult result
    ) {
        return GetEngineerResponse.newBuilder()
                .setEngineer(toProto(result.engineer()))
                .build();
    }

    public static Engineer toProto(
            com.sparrowx.internal.models.Engineer engineer
    ) {
        return Engineer.newBuilder()
                .setEngineerId(InternalMapper.value(engineer.engineerId()))
                .setTenantId(InternalMapper.value(engineer.tenantId()))
                .setFullName(engineer.fullName())
                .setEmail(InternalMapper.value(engineer.email()))
                .setRole(toProtoRole(engineer.role()))
                .setCreatedAt(InternalMapper.toTimestamp(engineer.createdAt()))
                .setUpdatedAt(InternalMapper.toTimestamp(engineer.updatedAt()))
                .build();
    }

    private static EngineerRole toProtoRole(Object role) {
        if (role == null) {
            return EngineerRole.ENGINEER_ROLE_UNSPECIFIED;
        }

        return switch (role.toString()) {
            case "LEARNER", "ENGINEER_ROLE_LEARNER" ->
                    EngineerRole.ENGINEER_ROLE_LEARNER;
            case "ENGINEER", "ENGINEER_ROLE_ENGINEER" ->
                    EngineerRole.ENGINEER_ROLE_ENGINEER;
            case "AGENTIC_ENGINEER", "ENGINEER_ROLE_AGENTIC_ENGINEER" ->
                    EngineerRole.ENGINEER_ROLE_AGENTIC_ENGINEER;
            case "ADMIN", "ENGINEER_ROLE_ADMIN" ->
                    EngineerRole.ENGINEER_ROLE_ADMIN;
            default -> EngineerRole.ENGINEER_ROLE_UNSPECIFIED;
        };
    }
}