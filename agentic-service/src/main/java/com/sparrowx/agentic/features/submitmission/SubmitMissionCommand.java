package com.sparrowx.agentic.features.submitmission;

import buildingblocks.core.commands.Command;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public record SubmitMissionCommand(
        String requestId,
        String tenantId,
        String userId,
        String username,
        String projectId,
        String teamId,
        String traceId,
        String callerService,
        String sessionId,
        String conversationId,
        String clientChannel,
        String query,
        List<InputArtifactInput> inputArtifacts,
        MissionConstraintsInput constraints,
        MissionBudgetInput budget,
        Map<String, String> metadata
) implements Command<SubmitMissionResult> {

    public SubmitMissionCommand {
        requestId = normalize(requestId);
        tenantId = normalize(tenantId);
        userId = normalize(userId);
        username = normalize(username);
        projectId = normalize(projectId);
        teamId = normalize(teamId);
        traceId = normalize(traceId);
        callerService = normalize(callerService);
        sessionId = normalize(sessionId);
        conversationId = normalize(conversationId);
        clientChannel = normalize(clientChannel);
        query = normalize(query);
        inputArtifacts = inputArtifacts == null ? List.of() : List.copyOf(inputArtifacts);
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    public record InputArtifactInput(
            String artifactId,
            String type,
            String objectUri,
            byte[] inlineBytes,
            String externalUri,
            String inlineText,
            String filename,
            String contentType,
            String sha256,
            Map<String, String> metadata
    ) {
        public InputArtifactInput {
            artifactId = normalize(artifactId);
            type = normalize(type);
            objectUri = normalize(objectUri);
            inlineBytes = inlineBytes == null
                    ? new byte[0]
                    : Arrays.copyOf(inlineBytes, inlineBytes.length);
            externalUri = normalize(externalUri);
            inlineText = inlineText == null ? "" : inlineText;
            filename = normalize(filename);
            contentType = normalize(contentType);
            sha256 = normalize(sha256);
            metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
        }

        @Override
        public byte[] inlineBytes() {
            return Arrays.copyOf(inlineBytes, inlineBytes.length);
        }
    }

    public record MissionConstraintsInput(
            String preferredPath,
            List<String> allowedTools,
            List<String> allowedSourceServices,
            List<String> requiredOutputSections,
            boolean requireCitations,
            boolean requireHumanReview,
            boolean allowExternalSources,
            long maxRuntimeSeconds,
            Map<String, String> policyHints
    ) {
        public MissionConstraintsInput {
            preferredPath = normalize(preferredPath);
            allowedTools = normalizeList(allowedTools);
            allowedSourceServices = normalizeList(allowedSourceServices);
            requiredOutputSections = normalizeList(requiredOutputSections);
            policyHints = policyHints == null ? Map.of() : Map.copyOf(policyHints);
        }
    }

    public record MissionBudgetInput(
            int maxLlmCalls,
            int maxToolCalls,
            int maxRetrievalQueries,
            int maxItemsToHydrate,
            long maxInputTokens,
            long maxOutputTokens,
            long maxCostMicros
    ) {
    }

    private static List<String> normalizeList(List<String> values) {
        if (values == null) {
            return List.of();
        }

        return values.stream()
                .map(SubmitMissionCommand::normalize)
                .filter(value -> !value.isEmpty())
                .toList();
    }
}