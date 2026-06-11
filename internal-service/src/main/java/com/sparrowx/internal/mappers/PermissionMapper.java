package com.sparrowx.internal.mappers;

import com.sparrowx.internal.features.permission.createpermission.CreatePermissionCommand;
import com.sparrowx.internal.features.permission.createpermission.CreatePermissionResult;
import com.sparrowx.internal.features.permission.getpermission.GetPermissionQuery;
import com.sparrowx.internal.features.permission.getpermission.GetPermissionResult;
import com.sparrowx.internal.grpc.CreatePermissionRequest;
import com.sparrowx.internal.grpc.CreatePermissionResponse;
import com.sparrowx.internal.grpc.GetPermissionRequest;
import com.sparrowx.internal.grpc.GetPermissionResponse;
import com.sparrowx.internal.grpc.Permission;

public final class PermissionMapper {

    private PermissionMapper() {
    }

    public static CreatePermissionCommand toCreatePermissionCommand(
            CreatePermissionRequest request
    ) {
        return new CreatePermissionCommand(
                InternalMapper.tenantId(request.getContext()),
                InternalMapper.actorId(request.getContext()),
                InternalMapper.requestId(request.getContext()),
                request.getName(),
                request.getDescription()
        );
    }

    public static CreatePermissionResponse toCreatePermissionResponse(
            CreatePermissionResult result
    ) {
        return CreatePermissionResponse.newBuilder()
                .setPermission(toProto(result.permission()))
                .build();
    }

    public static GetPermissionQuery toGetPermissionQuery(
            GetPermissionRequest request
    ) {
        return new GetPermissionQuery(
                InternalMapper.tenantId(request.getContext()),
                InternalMapper.actorId(request.getContext()),
                InternalMapper.requestId(request.getContext()),
                request.getPermissionId()
        );
    }

    public static GetPermissionResponse toGetPermissionResponse(
            GetPermissionResult result
    ) {
        return GetPermissionResponse.newBuilder()
                .setPermission(toProto(result.permission()))
                .build();
    }

    public static Permission toProto(
            com.sparrowx.internal.models.Permission permission
    ) {
        return Permission.newBuilder()
                .setPermissionId(InternalMapper.value(permission.permissionId()))
                .setTenantId(InternalMapper.value(permission.tenantId()))
                .setName(permission.name())
                .setDescription(permission.description())
                .setCreatedAt(InternalMapper.toTimestamp(permission.createdAt()))
                .setUpdatedAt(InternalMapper.toTimestamp(permission.updatedAt()))
                .build();
    }
}