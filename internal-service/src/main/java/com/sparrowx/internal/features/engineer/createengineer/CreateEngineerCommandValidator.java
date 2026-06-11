package com.sparrowx.internal.features.engineer.createengineer;

import com.sparrowx.internal.exceptions.InternalValidationException;
import com.sparrowx.internal.valueobjects.EmailAddress;
import com.sparrowx.internal.valueobjects.EngineerRole;
import org.springframework.stereotype.Component;

@Component
public class CreateEngineerCommandValidator {

    public void validate(CreateEngineerCommand command) {
        if (command == null) {
            throw new InternalValidationException("CreateEngineerCommand is required");
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

        if (command.fullName() == null || command.fullName().isBlank()) {
            throw new InternalValidationException("fullName is required");
        }

        if (command.email() == null || command.email().isBlank()) {
            throw new InternalValidationException("email is required");
        }

        EmailAddress.of(command.email());
        EngineerRole.from(command.role());
    }
}