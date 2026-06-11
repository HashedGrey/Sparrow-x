package com.sparrowx.internal.features.permission.getpermission;

import com.sparrowx.internal.exceptions.InternalValidationException;
import org.springframework.stereotype.Component;

@Component
public class GetPermissionQueryValidator {

    public void validate(GetPermissionQuery query) {
        if (query == null) {
            throw new InternalValidationException("GetPermissionQuery is required");
        }

        if (query.tenantId() == null || query.tenantId().isBlank()) {
            throw new InternalValidationException("tenantId is required");
        }

        if (query.actorId() == null || query.actorId().isBlank()) {
            throw new InternalValidationException("actorId is required");
        }

        if (query.requestId() == null || query.requestId().isBlank()) {
            throw new InternalValidationException("requestId is required");
        }

        if (query.permissionId() == null || query.permissionId().isBlank()) {
            throw new InternalValidationException("permissionId is required");
        }
    }
}