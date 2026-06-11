package com.sparrowx.internal.features.module.getmodule;

import com.sparrowx.internal.exceptions.InternalValidationException;
import org.springframework.stereotype.Component;

@Component
public class GetModuleQueryValidator {

    public void validate(GetModuleQuery query) {
        if (query == null) {
            throw new InternalValidationException("GetModuleQuery is required");
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

        if (query.moduleId() == null || query.moduleId().isBlank()) {
            throw new InternalValidationException("moduleId is required");
        }
    }
}