package com.sparrowx.internal.features.onboardingtask.createonboardingtask;

import com.sparrowx.internal.exceptions.InternalValidationException;
import org.springframework.stereotype.Component;

@Component
public class CreateOnboardingTaskCommandValidator {

    public void validate(CreateOnboardingTaskCommand command) {
        if (command == null) {
            throw new InternalValidationException("CreateOnboardingTaskCommand is required");
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

        if (command.onboardingPathId() == null || command.onboardingPathId().isBlank()) {
            throw new InternalValidationException("onboardingPathId is required");
        }

        if (command.title() == null || command.title().isBlank()) {
            throw new InternalValidationException("title is required");
        }

        if (command.documentId() == null || command.documentId().isBlank()) {
            throw new InternalValidationException("documentId is required");
        }

        if (command.runbookId() == null || command.runbookId().isBlank()) {
            throw new InternalValidationException("runbookId is required");
        }

        if (command.sortOrder() < 0) {
            throw new InternalValidationException("sortOrder must not be negative");
        }
    }
}