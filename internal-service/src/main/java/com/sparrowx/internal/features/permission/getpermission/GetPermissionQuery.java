package com.sparrowx.internal.features.permission.getpermission;

import buildingblocks.core.queries.Query;

public record GetPermissionQuery(
        String tenantId,
        String actorId,
        String requestId,
        String permissionId
) implements Query<GetPermissionResult> {
}