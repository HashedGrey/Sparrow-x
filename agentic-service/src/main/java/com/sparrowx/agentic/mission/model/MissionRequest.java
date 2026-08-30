package com.sparrowx.agentic.mission.model;

import com.sparrowx.agentic.mission.artifact.InputArtifact;

import java.util.List;

/**
 * Normalized user mission input.
 *
 * The request id in MissionContext is the submission idempotency identity.
 * Request fingerprinting is performed by MissionSubmissionService.
 */
public record MissionRequest(
        MissionContext context,
        String query,
        List<InputArtifact> inputArtifacts,
        MissionConstraints constraints,
        MissionBudget budget
) {

    public MissionRequest {
        query = normalize(query);

        inputArtifacts = inputArtifacts == null
                ? List.of()
                : List.copyOf(inputArtifacts);
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}