package com.sparrowx.agentic.agents;

import com.sparrowx.agentic.mission.artifact.ArtifactPreparationResult;
import com.sparrowx.agentic.mission.model.MissionRequest;

import java.time.Instant;
import java.util.Objects;
import java.util.Set;

/**
 * Complete business input supplied to one Embabel process.
 *
 * Temporal treats this payload as opaque Activity data. Embabel alone turns
 * it into goals, actions, plans and blackboard state.
 */
public record MissionRunInput(
        String missionId,
        MissionRequest request,
        ArtifactPreparationResult preparedArtifacts,
        Set<String> approvedGateIds,
        Instant startedAt
) {

    public MissionRunInput {
        missionId = requireText(missionId, "missionId");
        request = Objects.requireNonNull(
                request,
                "request must not be null"
        );
        preparedArtifacts = Objects.requireNonNull(
                preparedArtifacts,
                "preparedArtifacts must not be null"
        );
        approvedGateIds = approvedGateIds == null
                ? Set.of()
                : Set.copyOf(approvedGateIds);
        startedAt = Objects.requireNonNull(
                startedAt,
                "startedAt must not be null"
        );

        if (request.context().tenantId().isBlank()) {
            throw new IllegalArgumentException(
                    "request.context.tenantId must not be blank"
            );
        }
    }

    public String tenantId() {
        return request.context().tenantId();
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    field + " must not be blank"
            );
        }
        return value.trim();
    }
}
