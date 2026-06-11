package com.sparrowx.internal.features.repository.getrepository;

import com.sparrowx.internal.exceptions.InternalValidationException;
import org.springframework.stereotype.Component;

@Component
public class GetRepositoryQueryValidator {

    public void validate(GetRepositoryQuery query) {
        if (query == null) {
            throw new InternalValidationException("GetRepositoryQuery is required");
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

        if (query.repositoryId() == null || query.repositoryId().isBlank()) {
            throw new InternalValidationException("repositoryId is required");
        }
    }
}