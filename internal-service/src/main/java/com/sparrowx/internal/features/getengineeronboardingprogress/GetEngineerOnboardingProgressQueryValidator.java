package com.sparrowx.internal.features.getengineeronboardingprogress;

import com.sparrowx.internal.exceptions.InternalValidationException;
import org.springframework.stereotype.Component;

@Component
public class GetEngineerOnboardingProgressQueryValidator {

    public void validate(GetEngineerOnboardingProgressQuery query) {
        if (query == null) {
            throw new InternalValidationException(
                    "GetEngineerOnboardingProgressQuery is required"
            );
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

        if (query.assignmentId() == null || query.assignmentId().isBlank()) {
            throw new InternalValidationException("assignmentId is required");
        }
    }
}