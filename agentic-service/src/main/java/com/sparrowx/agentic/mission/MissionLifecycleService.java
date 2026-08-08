package com.sparrowx.agentic.mission;

import com.sparrowx.agentic.mission.model.HumanGateState;
import com.sparrowx.agentic.mission.model.Mission;
import com.sparrowx.agentic.mission.model.MissionFailure;
import com.sparrowx.agentic.mission.model.MissionStatus;
import com.sparrowx.agentic.mission.store.MissionStore;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
public class MissionLifecycleService {

    private static final Map<MissionStatus, Set<MissionStatus>>
            ALLOWED_TRANSITIONS = Map.of(
            MissionStatus.UNSPECIFIED,
            Set.of(MissionStatus.SUBMITTED),

            MissionStatus.SUBMITTED,
            Set.of(
                    MissionStatus.RUNNING,
                    MissionStatus.FAILED_RETRYABLE,
                    MissionStatus.FAILED_TERMINAL,
                    MissionStatus.CANCELLED
            ),

            MissionStatus.RUNNING,
            Set.of(
                    MissionStatus.WAITING_APPROVAL,
                    MissionStatus.FAILED_RETRYABLE,
                    MissionStatus.FAILED_TERMINAL,
                    MissionStatus.COMPLETED,
                    MissionStatus.CANCELLED
            ),

            MissionStatus.WAITING_APPROVAL,
            Set.of(
                    MissionStatus.RUNNING,
                    MissionStatus.FAILED_TERMINAL,
                    MissionStatus.CANCELLED
            ),

            MissionStatus.FAILED_RETRYABLE,
            Set.of(
                    MissionStatus.RUNNING,
                    MissionStatus.FAILED_TERMINAL,
                    MissionStatus.CANCELLED
            ),

            MissionStatus.FAILED_TERMINAL,
            Set.of(),

            MissionStatus.COMPLETED,
            Set.of(),

            MissionStatus.CANCELLED,
            Set.of()
    );

    private final MissionStore missionStore;

    public MissionLifecycleService(MissionStore missionStore) {
        this.missionStore = Objects.requireNonNull(
                missionStore,
                "missionStore"
        );
    }

    public Mission transition(
            Mission mission,
            MissionStatus targetStatus,
            MissionFailure failure,
            HumanGateState waitState,
            Instant occurredAt
    ) {
        Objects.requireNonNull(mission, "mission");
        Objects.requireNonNull(targetStatus, "targetStatus");
        Objects.requireNonNull(occurredAt, "occurredAt");

        if (mission.status() == targetStatus) {
            return mission;
        }

        Set<MissionStatus> allowed = ALLOWED_TRANSITIONS.get(
                mission.status()
        );

        if (allowed == null || !allowed.contains(targetStatus)) {
            throw new IllegalStateException(
                    "Illegal mission transition: "
                            + mission.status()
                            + " -> "
                            + targetStatus
            );
        }

        MissionFailure nextFailure = resolveFailure(
                targetStatus,
                failure
        );

        HumanGateState nextWaitState = resolveWaitState(
                targetStatus,
                waitState
        );

        Instant startedAt = mission.startedAt();
        if (targetStatus == MissionStatus.RUNNING && startedAt == null) {
            startedAt = occurredAt;
        }

        Instant completedAt = targetStatus.isTerminal()
                ? occurredAt
                : null;

        Mission updated = new Mission(
                mission.missionId(),
                mission.context(),
                mission.requestFingerprint(),
                mission.query(),
                mission.preparedArtifacts(),
                mission.constraints(),
                mission.budget(),
                mission.selectedPath(),
                targetStatus,
                mission.versionSnapshot(),
                nextFailure,
                nextWaitState,
                mission.submittedAt(),
                startedAt,
                occurredAt,
                completedAt
        );

        return missionStore.save(updated);
    }

    private static MissionFailure resolveFailure(
            MissionStatus targetStatus,
            MissionFailure failure
    ) {
        boolean failed = targetStatus == MissionStatus.FAILED_RETRYABLE
                || targetStatus == MissionStatus.FAILED_TERMINAL;

        if (failed) {
            return Objects.requireNonNull(
                    failure,
                    "A failed mission requires failure details"
            );
        }

        return null;
    }

    private static HumanGateState resolveWaitState(
            MissionStatus targetStatus,
            HumanGateState waitState
    ) {
        if (targetStatus == MissionStatus.WAITING_APPROVAL) {
            return Objects.requireNonNull(
                    waitState,
                    "WAITING_APPROVAL requires a wait state"
            );
        }

        return null;
    }
}