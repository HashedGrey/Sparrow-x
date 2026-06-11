package com.sparrowx.internal.features.runbook.createrunbook;

import com.sparrowx.internal.exceptions.InternalValidationException;
import org.springframework.stereotype.Component;

@Component
public class CreateRunbookCommandValidator {

    public void validate(CreateRunbookCommand command) {
        if (command == null) {
            throw new InternalValidationException("CreateRunbookCommand is required");
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

        if (command.title() == null || command.title().isBlank()) {
            throw new InternalValidationException("title is required");
        }

        if (command.moduleId() == null || command.moduleId().isBlank()) {
            throw new InternalValidationException("moduleId is required");
        }

        if (command.documentId() == null || command.documentId().isBlank()) {
            throw new InternalValidationException("documentId is required");
        }
    }
}