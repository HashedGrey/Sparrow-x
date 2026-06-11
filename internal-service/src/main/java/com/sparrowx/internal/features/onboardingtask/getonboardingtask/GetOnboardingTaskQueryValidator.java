package com.sparrowx.internal.features.onboardingtask.getonboardingtask;

import com.sparrowx.internal.exceptions.InternalValidationException;
import org.springframework.stereotype.Component;

@Component
public class GetOnboardingTaskQueryValidator {

    public void validate(GetOnboardingTaskQuery query) {
        if (query == null) {
            throw new InternalValidationException("GetOnboardingTaskQuery is required");
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

        if (query.onboardingTaskId() == null || query.onboardingTaskId().isBlank()) {
            throw new InternalValidationException("onboardingTaskId is required");
        }
    }
}