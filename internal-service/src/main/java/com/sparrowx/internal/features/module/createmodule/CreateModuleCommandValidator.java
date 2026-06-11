package com.sparrowx.internal.features.module.createmodule;

import com.sparrowx.internal.exceptions.InternalValidationException;
import org.springframework.stereotype.Component;

@Component
public class CreateModuleCommandValidator {

    public void validate(CreateModuleCommand command) {
        if (command == null) {
            throw new InternalValidationException("CreateModuleCommand is required");
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

        if (command.owningTeamId() == null || command.owningTeamId().isBlank()) {
            throw new InternalValidationException("owningTeamId is required");
        }
    }
}