package com.sparrowx.internal.features.onboardingpath.getonboardingpath;

import com.sparrowx.internal.exceptions.InternalValidationException;
import org.springframework.stereotype.Component;

@Component
public class GetOnboardingPathQueryValidator {

    public void validate(GetOnboardingPathQuery query) {
        if (query == null) {
            throw new InternalValidationException("GetOnboardingPathQuery is required");
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

        if (query.onboardingPathId() == null || query.onboardingPathId().isBlank()) {
            throw new InternalValidationException("onboardingPathId is required");
        }
    }
}