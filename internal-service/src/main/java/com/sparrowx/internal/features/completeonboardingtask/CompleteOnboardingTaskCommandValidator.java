package com.sparrowx.internal.features.completeonboardingtask;

import com.sparrowx.internal.exceptions.InternalValidationException;
import org.springframework.stereotype.Component;

@Component
public class CompleteOnboardingTaskCommandValidator {

    public void validate(CompleteOnboardingTaskCommand command) {
        if (command == null) {
            throw new InternalValidationException("CompleteOnboardingTaskCommand is required");
        }

        if (command.tenantId() == null || command.tenantId().isBlank()) {
            throw new InternalValidationException("tenantId is required");
        }

        if (command.actorId() == null || command.actorId().isBlank()) {
            throw new InternalValidationException("actorId is required");
        }

        if (command.requestId() == null || command.requestId().isBlank()) {
            throw new InternalValidationException("requestId is required");
        }

        if (command.assignmentId() == null || command.assignmentId().isBlank()) {
            throw new InternalValidationException("assignmentId is required");
        }

        if (command.onboardingTaskId() == null || command.onboardingTaskId().isBlank()) {
            throw new InternalValidationException("onboardingTaskId is required");
        }
    }
}