package com.sparrowx.internal.features.searchinternalentities;

import com.sparrowx.internal.exceptions.InternalValidationException;
import org.springframework.stereotype.Component;

@Component
public class SearchInternalEntitiesQueryValidator {

    public void validate(SearchInternalEntitiesQuery query) {
        if (query == null) {
            throw new InternalValidationException("SearchInternalEntitiesQuery is required");
        }

        if (isBlank(query.tenantId())) {
            throw new InternalValidationException("tenantId is required");
        }

        if (isBlank(query.actorId())) {
            throw new InternalValidationException("actorId is required");
        }

        if (isBlank(query.requestId())) {
            throw new InternalValidationException("requestId is required");
        }

        if (isBlank(query.query()) && isBlank(query.rootEntityId())) {
            throw new InternalValidationException(
                    "Either query or rootEntityId is required for internal entity search"
            );
        }

        if (query.limit() < 0) {
            throw new InternalValidationException("limit cannot be negative");
        }

        if (query.depth() < 0) {
            throw new InternalValidationException("depth cannot be negative");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}