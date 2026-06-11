package com.sparrowx.internal.features.runbook.getrunbook;

import com.sparrowx.internal.exceptions.InternalValidationException;
import org.springframework.stereotype.Component;

@Component
public class GetRunbookQueryValidator {

    public void validate(GetRunbookQuery query) {
        if (query == null) {
            throw new InternalValidationException("GetRunbookQuery is required");
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

        if (query.runbookId() == null || query.runbookId().isBlank()) {
            throw new InternalValidationException("runbookId is required");
        }
    }
}