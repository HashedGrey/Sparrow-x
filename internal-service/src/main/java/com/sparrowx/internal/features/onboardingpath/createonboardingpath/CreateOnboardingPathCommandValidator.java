package com.sparrowx.internal.features.onboardingpath.createonboardingpath;

import com.sparrowx.internal.exceptions.InternalValidationException;
import org.springframework.stereotype.Component;

@Component
public class CreateOnboardingPathCommandValidator {

    public void validate(CreateOnboardingPathCommand command) {
        if (command == null) {
            throw new InternalValidationException("CreateOnboardingPathCommand is required");
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

        if (command.name() == null || command.name().isBlank()) {
            throw new InternalValidationException("name is required");
        }

        if (command.targetModuleId() == null || command.targetModuleId().isBlank()) {
            throw new InternalValidationException("targetModuleId is required");
        }
    }
}