package com.sparrowx.agentic.features.submitmission;

import com.sparrowx.agentic.exceptions.MissionValidationException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public final class SubmitMissionCommandValidator {

    private static final int MAX_ID_LENGTH = 256;
    private static final int MAX_QUERY_LENGTH = 32_000;
    private static final int MAX_TEXT_LENGTH = 4_000;
    private static final int MAX_ARTIFACTS = 64;
    private static final int MAX_METADATA_ENTRIES = 128;
    private static final int MAX_METADATA_KEY_LENGTH = 128;
    private static final int MAX_METADATA_VALUE_LENGTH = 2_048;

    public void validate(SubmitMissionCommand command) {
        if (command == null) {
            throw invalid("Submit mission command is required.");
        }

        requireId(command.requestId(), "request ID");
        requireId(command.tenantId(), "tenant ID");
        requireId(command.userId(), "user ID");
        requireText(command.query(), "query", MAX_QUERY_LENGTH);

        optionalId(command.username(), "username");
        optionalId(command.projectId(), "project ID");
        optionalId(command.teamId(), "team ID");
        optionalId(command.traceId(), "trace ID");
        optionalId(command.callerService(), "caller service");
        optionalId(command.sessionId(), "session ID");
        optionalId(command.conversationId(), "conversation ID");
        optionalId(command.clientChannel(), "client channel");

        validateMetadata(command.metadata(), "mission metadata");

        if (command.inputArtifacts().size() > MAX_ARTIFACTS) {
            throw invalid("Input artifacts must not exceed " + MAX_ARTIFACTS + ".");
        }

        for (int index = 0; index < command.inputArtifacts().size(); index++) {
            validateArtifact(command.inputArtifacts().get(index), index);
        }

        validateConstraints(command.constraints());
        validateBudget(command.budget());
    }

    private static void validateArtifact(
            SubmitMissionCommand.InputArtifactInput artifact,
            int index
    ) {
        if (artifact == null) {
            throw invalid("Input artifact at index " + index + " is required.");
        }

        requireId(artifact.artifactId(), "artifact ID");
        requireText(artifact.type(), "artifact type", MAX_ID_LENGTH);
        optionalText(artifact.objectUri(), "object URI", MAX_TEXT_LENGTH);
        optionalText(artifact.externalUri(), "external URI", MAX_TEXT_LENGTH);
        optionalText(artifact.inlineText(), "inline text", MAX_QUERY_LENGTH);
        optionalText(artifact.filename(), "filename", MAX_ID_LENGTH);
        optionalText(artifact.contentType(), "content type", MAX_ID_LENGTH);
        optionalText(artifact.sha256(), "SHA-256", MAX_ID_LENGTH);
        validateMetadata(artifact.metadata(), "artifact metadata");

        int populatedContentFields = 0;
        populatedContentFields += artifact.objectUri().isBlank() ? 0 : 1;
        populatedContentFields += artifact.inlineBytes().length == 0 ? 0 : 1;
        populatedContentFields += artifact.externalUri().isBlank() ? 0 : 1;
        populatedContentFields += artifact.inlineText().isBlank() ? 0 : 1;

        if (populatedContentFields != 1) {
            throw invalid(
                    "Artifact " + artifact.artifactId()
                            + " must provide exactly one content source."
            );
        }
    }

    private static void validateConstraints(
            SubmitMissionCommand.MissionConstraintsInput constraints
    ) {
        if (constraints == null) {
            throw invalid("Mission constraints are required.");
        }

        optionalText(
                constraints.preferredPath(),
                "preferred path",
                MAX_ID_LENGTH
        );
        validateStringList(
                constraints.allowedTools(),
                "allowed tool",
                MAX_ID_LENGTH
        );
        validateStringList(
                constraints.allowedSourceServices(),
                "allowed source service",
                MAX_ID_LENGTH
        );
        validateStringList(
                constraints.requiredOutputSections(),
                "required output section",
                MAX_ID_LENGTH
        );
        validateMetadata(constraints.policyHints(), "policy hints");

        if (constraints.maxRuntimeSeconds() < 0) {
            throw invalid("Maximum runtime seconds must not be negative.");
        }
    }

    private static void validateBudget(
            SubmitMissionCommand.MissionBudgetInput budget
    ) {
        if (budget == null) {
            throw invalid("Mission budget is required.");
        }

        requireNonNegative(budget.maxLlmCalls(), "Maximum LLM calls");
        requireNonNegative(budget.maxToolCalls(), "Maximum tool calls");
        requireNonNegative(
                budget.maxRetrievalQueries(),
                "Maximum retrieval queries"
        );
        requireNonNegative(
                budget.maxItemsToHydrate(),
                "Maximum items to hydrate"
        );
        requireNonNegative(budget.maxInputTokens(), "Maximum input tokens");
        requireNonNegative(budget.maxOutputTokens(), "Maximum output tokens");
        requireNonNegative(budget.maxCostMicros(), "Maximum cost micros");
    }

    private static void requireId(String value, String field) {
        requireText(value, field, MAX_ID_LENGTH);
    }

    private static void optionalId(String value, String field) {
        optionalText(value, field, MAX_ID_LENGTH);
    }

    private static void requireText(
            String value,
            String field,
            int maximumLength
    ) {
        if (value == null || value.isBlank()) {
            throw invalid(field + " is required.");
        }

        if (value.length() > maximumLength) {
            throw invalid(
                    field + " must not exceed " + maximumLength + " characters."
            );
        }
    }

    private static void optionalText(
            String value,
            String field,
            int maximumLength
    ) {
        if (value != null && value.length() > maximumLength) {
            throw invalid(
                    field + " must not exceed " + maximumLength + " characters."
            );
        }
    }

    private static void validateStringList(
            List<String> values,
            String field,
            int maximumLength
    ) {
        if (values == null) {
            throw invalid(field + " collection is required.");
        }

        for (String value : values) {
            requireText(value, field, maximumLength);
        }
    }

    private static void validateMetadata(
            Map<String, String> metadata,
            String field
    ) {
        if (metadata == null) {
            throw invalid(field + " is required.");
        }

        if (metadata.size() > MAX_METADATA_ENTRIES) {
            throw invalid(
                    field + " must not exceed "
                            + MAX_METADATA_ENTRIES + " entries."
            );
        }

        metadata.forEach((key, value) -> {
            requireText(key, field + " key", MAX_METADATA_KEY_LENGTH);
            optionalText(
                    value,
                    field + " value",
                    MAX_METADATA_VALUE_LENGTH
            );
        });
    }

    private static void requireNonNegative(long value, String field) {
        if (value < 0) {
            throw invalid(field + " must not be negative.");
        }
    }

    private static MissionValidationException invalid(String message) {
        return new MissionValidationException(message);
    }
}