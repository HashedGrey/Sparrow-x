package com.sparrowx.internal.features.repository.createrepository;

import com.sparrowx.internal.exceptions.InternalValidationException;
import org.springframework.stereotype.Component;

@Component
public class CreateRepositoryCommandValidator {

    public void validate(CreateRepositoryCommand command) {
        if (command == null) {
            throw new InternalValidationException("CreateRepositoryCommand is required");
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

        if (command.url() == null || command.url().isBlank()) {
            throw new InternalValidationException("url is required");
        }

        if (command.moduleId() == null || command.moduleId().isBlank()) {
            throw new InternalValidationException("moduleId is required");
        }
    }
}