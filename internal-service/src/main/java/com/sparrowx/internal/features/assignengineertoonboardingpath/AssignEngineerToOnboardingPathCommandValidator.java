package com.sparrowx.internal.features.assignengineertoonboardingpath;

import com.sparrowx.internal.exceptions.InternalValidationException;
import org.springframework.stereotype.Component;

@Component
public class AssignEngineerToOnboardingPathCommandValidator {

    public void validate(AssignEngineerToOnboardingPathCommand command) {
        if (command == null) {
            throw new InternalValidationException(
                    "AssignEngineerToOnboardingPathCommand is required"
            );
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

        if (command.engineerId() == null || command.engineerId().isBlank()) {
            throw new InternalValidationException("engineerId is required");
        }

        if (command.onboardingPathId() == null || command.onboardingPathId().isBlank()) {
            throw new InternalValidationException("onboardingPathId is required");
        }
    }
}