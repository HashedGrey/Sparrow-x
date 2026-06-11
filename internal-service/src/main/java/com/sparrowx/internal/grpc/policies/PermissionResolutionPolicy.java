package com.sparrowx.internal.grpc.policies;

import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class PermissionResolutionPolicy {

    public boolean hasPermission(
            String actorId,
            String permissionName,
            Set<String> grantedPermissions
    ) {
        if (actorId == null || actorId.isBlank()) {
            return false;
        }

        if (permissionName == null || permissionName.isBlank()) {
            return false;
        }

        if (grantedPermissions == null || grantedPermissions.isEmpty()) {
            return false;
        }

        return grantedPermissions.contains(normalize(permissionName));
    }

    public boolean hasAnyPermission(
            String actorId,
            Set<String> requiredPermissions,
            Set<String> grantedPermissions
    ) {
        if (requiredPermissions == null || requiredPermissions.isEmpty()) {
            return true;
        }

        return requiredPermissions.stream()
                .anyMatch(permission ->
                        hasPermission(actorId, permission, grantedPermissions)
                );
    }

    public boolean hasAllPermissions(
            String actorId,
            Set<String> requiredPermissions,
            Set<String> grantedPermissions
    ) {
        if (requiredPermissions == null || requiredPermissions.isEmpty()) {
            return true;
        }

        return requiredPermissions.stream()
                .allMatch(permission ->
                        hasPermission(actorId, permission, grantedPermissions)
                );
    }

    private String normalize(String permissionName) {
        return permissionName.trim()
                .toUpperCase()
                .replaceAll("[^A-Z0-9:_-]+", "_")
                .replaceAll("(^_|_$)", "");
    }
}