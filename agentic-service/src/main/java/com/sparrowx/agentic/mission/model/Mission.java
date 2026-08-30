package com.sparrowx.agentic.mission.model;

import com.sparrowx.agentic.mission.artifact.PreparedArtifact;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

/**
 * Business aggregate and public lifecycle projection, not Temporal history.
 */
public record Mission(
        String missionId,
        MissionContext context,
        String requestFingerprint,
        String query,
        List<PreparedArtifact> preparedArtifacts,
        MissionConstraints constraints,
        MissionBudget budget,
        MissionPath selectedPath,
        MissionStatus status,
        MissionVersionSnapshot versionSnapshot,
        MissionFailure failure,
        HumanGateState waitState,
        Instant submittedAt,
        Instant startedAt,
        Instant updatedAt,
        Instant completedAt
) {

    public Mission {
        missionId = nullToEmpty(missionId);
        context = Objects.requireNonNull(context, "context");
        requestFingerprint = nullToEmpty(requestFingerprint);
        query = nullToEmpty(query);

        preparedArtifacts = preparedArtifacts == null
                ? List.of()
                : List.copyOf(preparedArtifacts);

        constraints = Objects.requireNonNull(constraints, "constraints");
        budget = Objects.requireNonNull(budget, "budget");

        selectedPath = selectedPath == null
                ? MissionPath.UNSPECIFIED
                : selectedPath;

        status = status == null
                ? MissionStatus.UNSPECIFIED
                : status;

        versionSnapshot = Objects.requireNonNull(
                versionSnapshot,
                "versionSnapshot"
        );

        submittedAt = normalizeInstant(
                Objects.requireNonNull(submittedAt, "submittedAt")
        );

        startedAt = normalizeInstant(startedAt);
        updatedAt = normalizeInstant(
                Objects.requireNonNull(updatedAt, "updatedAt")
        );
        completedAt = normalizeInstant(completedAt);
    }

    private static Instant normalizeInstant(Instant value) {
        return value == null
                ? null
                : value.truncatedTo(ChronoUnit.MICROS);
    }

    public String tenantId() {
        return context.tenantId();
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}