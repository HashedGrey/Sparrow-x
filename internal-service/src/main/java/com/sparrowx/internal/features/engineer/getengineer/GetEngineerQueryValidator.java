package com.sparrowx.internal.features.engineer.getengineer;

import com.sparrowx.internal.exceptions.InternalValidationException;
import org.springframework.stereotype.Component;

@Component
public class GetEngineerQueryValidator {

    public void validate(GetEngineerQuery query) {
        if (query == null) {
            throw new InternalValidationException("GetEngineerQuery is required");
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

        if (query.engineerId() == null || query.engineerId().isBlank()) {
            throw new InternalValidationException("engineerId is required");
        }
    }
}