package com.sparrowx.agentic.features.rejectmissiongate;

import com.sparrowx.agentic.exceptions.MissionValidationException;
import org.springframework.stereotype.Component;

@Component
public final class RejectMissionGateCommandValidator {

    private static final int MAX_ID_LENGTH = 256;
    private static final int MAX_REASON_LENGTH = 4_000;

    public void validate(RejectMissionGateCommand command) {
        if (command == null) {
            throw invalid(
                    "Reject mission gate command is required"
            );
        }

        MissionContext context = command.context();
        if (context == null) {
            throw invalid("Mission context is required");
        }

        requireId(context.requestId(), "Request ID");
        requireId(context.tenantId(), "Tenant ID");
        requireId(context.userId(), "Reviewer user ID");
        requireId(command.missionId(), "Mission ID");
        requireId(command.gateId(), "Gate ID");
        requireRequiredText(
                command.reason(),
                "Rejection reason"
        );
    }

    private static void requireId(String value, String name) {
        if (value == null || value.isBlank()) {
            throw invalid(name + " is required");
        }

        if (value.length() > MAX_ID_LENGTH) {
            throw invalid(
                    name + " must not exceed 256 characters"
            );
        }
    }

    private static void requireRequiredText(
            String value,
            String name
    ) {
        if (value == null || value.isBlank()) {
            throw invalid(name + " is required");
        }

        if (value.length() > MAX_REASON_LENGTH) {
            throw invalid(
                    name + " must not exceed 4000 characters"
            );
        }
    }

    private static MissionValidationException invalid(String message) {
        return new MissionValidationException(message);
    }
}