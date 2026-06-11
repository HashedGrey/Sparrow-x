package com.sparrowx.internal.features.getinternalgraphcontext;

import com.sparrowx.internal.exceptions.InternalValidationException;
import org.springframework.stereotype.Component;

@Component
public class GetInternalGraphContextQueryValidator {

    private static final int DEFAULT_MAX_DEPTH = 5;
    private static final int DEFAULT_MAX_LIMIT = 250;

    public void validate(GetInternalGraphContextQuery query) {
        if (query == null) {
            throw new InternalValidationException("GetInternalGraphContextQuery is required");
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

        if (query.graphType() == null || query.graphType().isBlank()) {
            throw new InternalValidationException("graphType is required");
        }

        if (query.rootEntityId() == null || query.rootEntityId().isBlank()) {
            throw new InternalValidationException("rootEntityId is required");
        }

        if (query.rootNodeType() == null || query.rootNodeType().isBlank()) {
            throw new InternalValidationException("rootNodeType is required");
        }

        if (query.depth() < 0) {
            throw new InternalValidationException("depth must not be negative");
        }

        if (query.depth() > DEFAULT_MAX_DEPTH) {
            throw new InternalValidationException("depth must not exceed " + DEFAULT_MAX_DEPTH);
        }

        if (query.limit() < 0) {
            throw new InternalValidationException("limit must not be negative");
        }

        if (query.limit() > DEFAULT_MAX_LIMIT) {
            throw new InternalValidationException("limit must not exceed " + DEFAULT_MAX_LIMIT);
        }
    }
}