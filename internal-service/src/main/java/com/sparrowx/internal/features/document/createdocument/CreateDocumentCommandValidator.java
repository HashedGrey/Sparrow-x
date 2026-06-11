package com.sparrowx.internal.features.document.createdocument;

import com.sparrowx.internal.exceptions.InternalValidationException;
import org.springframework.stereotype.Component;

@Component
public class CreateDocumentCommandValidator {

    public void validate(CreateDocumentCommand command) {
        if (command == null) {
            throw new InternalValidationException("CreateDocumentCommand is required");
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

        if (command.repositoryId() == null || command.repositoryId().isBlank()) {
            throw new InternalValidationException("repositoryId is required");
        }

        if (command.externalRef() == null || command.externalRef().isBlank()) {
            throw new InternalValidationException("externalRef is required");
        }
    }
}