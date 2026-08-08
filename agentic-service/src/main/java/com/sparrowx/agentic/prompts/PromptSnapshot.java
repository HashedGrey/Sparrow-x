package com.sparrowx.agentic.prompts;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

public record PromptSnapshot(
        String missionId,
        String operationId,
        String promptName,
        String promptVersion,
        String systemPrompt,
        String userPrompt,
        String schemaName,
        String schemaVersion,
        String modelRouteKey,
        String contentHash,
        Instant createdAt,
        Map<String, String> metadata) {

    public PromptSnapshot {
        missionId = requireText(missionId, "missionId");
        operationId = requireText(operationId, "operationId");
        promptName = requireText(promptName, "promptName");
        promptVersion = requireText(
                promptVersion,
                "promptVersion");
        systemPrompt = systemPrompt == null
                ? ""
                : systemPrompt;
        userPrompt = requireText(userPrompt, "userPrompt");
        schemaName = requireText(schemaName, "schemaName");
        schemaVersion = requireText(
                schemaVersion,
                "schemaVersion");
        modelRouteKey = requireText(
                modelRouteKey,
                "modelRouteKey");
        contentHash = requireText(
                contentHash,
                "contentHash");
        createdAt = Objects.requireNonNull(
                createdAt,
                "createdAt must not be null");
        metadata = metadata == null
                ? Map.of()
                : Map.copyOf(metadata);
    }

    private static String requireText(
            String value,
            String field) {

        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    field + " must not be blank");
        }

        return value;
    }
}