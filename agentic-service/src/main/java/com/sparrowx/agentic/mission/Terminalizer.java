package com.sparrowx.agentic.mission;

import com.sparrowx.agentic.exceptions.MissionNotFoundException;
import com.sparrowx.agentic.mission.model.Mission;
import com.sparrowx.agentic.mission.model.MissionFailure;
import com.sparrowx.agentic.mission.model.MissionProgressEvent;
import com.sparrowx.agentic.mission.model.MissionResult;
import com.sparrowx.agentic.mission.model.MissionStatus;
import com.sparrowx.agentic.mission.store.MissionStore;
import com.sparrowx.agentic.runtime.checkpoint.CheckpointRef;
import com.sparrowx.agentic.runtime.checkpoint.CheckpointSerializer;
import com.sparrowx.agentic.runtime.checkpoint.CheckpointSnapshot;
import com.sparrowx.agentic.runtime.checkpoint.CheckpointStore;
import com.sparrowx.agentic.runtime.model.StepStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
public class Terminalizer {

    private static final int RESULT_SCHEMA_VERSION = 1;

    private static final Set<MissionStatus> COMPLETION_SOURCES = Set.of(
            MissionStatus.RUNNING,
            MissionStatus.COMPLETED
    );

    private static final Set<MissionStatus> FAILURE_SOURCES = Set.of(
            MissionStatus.SUBMITTED,
            MissionStatus.RUNNING,
            MissionStatus.WAITING_APPROVAL,
            MissionStatus.FAILED_RETRYABLE,
            MissionStatus.FAILED_TERMINAL
    );

    private static final Set<MissionStatus> CANCELLATION_SOURCES = Set.of(
            MissionStatus.SUBMITTED,
            MissionStatus.RUNNING,
            MissionStatus.WAITING_APPROVAL,
            MissionStatus.FAILED_RETRYABLE,
            MissionStatus.CANCELLED
    );

    private final MissionStore missionStore;
    private final MissionLifecycleService lifecycleService;
    private final CheckpointSerializer checkpointSerializer;
    private final CheckpointStore checkpointStore;
    private final MissionEventPublisher eventPublisher;

    public Terminalizer(
            MissionStore missionStore,
            MissionLifecycleService lifecycleService,
            CheckpointSerializer checkpointSerializer,
            CheckpointStore checkpointStore,
            MissionEventPublisher eventPublisher
    ) {
        this.missionStore = Objects.requireNonNull(
                missionStore,
                "missionStore"
        );
        this.lifecycleService = Objects.requireNonNull(
                lifecycleService,
                "lifecycleService"
        );
        this.checkpointSerializer = Objects.requireNonNull(
                checkpointSerializer,
                "checkpointSerializer"
        );
        this.checkpointStore = Objects.requireNonNull(
                checkpointStore,
                "checkpointStore"
        );
        this.eventPublisher = Objects.requireNonNull(
                eventPublisher,
                "eventPublisher"
        );
    }

    public Mission complete(
            String tenantId,
            String missionId,
            MissionResult result
    ) {
        Objects.requireNonNull(result, "result");

        Mission mission = findMission(tenantId, missionId);
        requireSource(mission, COMPLETION_SOURCES, MissionStatus.COMPLETED);

        if (!missionId.equals(result.missionId())) {
            throw new IllegalArgumentException(
                    "Mission result belongs to a different mission"
            );
        }

        Instant terminalAt = terminalTime(mission);

        CheckpointSnapshot snapshot = checkpointSerializer.serialize(
                "result_" + missionId,
                tenantId,
                missionId,
                CheckpointRef.CheckpointType.MISSION_RESULT,
                RESULT_SCHEMA_VERSION,
                terminalAt,
                Map.of("kind", "mission-result"),
                result
        );

        checkpointStore.save(snapshot);

        Mission completed = lifecycleService.transition(
                mission,
                MissionStatus.COMPLETED,
                null,
                null,
                terminalAt
        );

        publishTerminalEvent(
                completed,
                StepStatus.SUCCEEDED,
                "Mission completed",
                Map.of("terminal", "true")
        );

        return completed;
    }

    public Mission failTerminal(
            String tenantId,
            String missionId,
            MissionFailure failure
    ) {
        Objects.requireNonNull(failure, "failure");

        if (failure.retryable()) {
            throw new IllegalArgumentException(
                    "Terminal failure must not be marked retryable"
            );
        }

        Mission mission = findMission(tenantId, missionId);
        requireSource(
                mission,
                FAILURE_SOURCES,
                MissionStatus.FAILED_TERMINAL
        );

        Mission failed = lifecycleService.transition(
                mission,
                MissionStatus.FAILED_TERMINAL,
                failure,
                null,
                terminalTime(mission)
        );

        publishTerminalEvent(
                failed,
                StepStatus.FAILED_TERMINAL,
                failure.message(),
                Map.of(
                        "terminal", "true",
                        "errorCode", failure.code()
                )
        );

        return failed;
    }

    public Mission cancel(
            String tenantId,
            String missionId,
            String reason
    ) {
        Mission mission = findMission(tenantId, missionId);
        requireSource(
                mission,
                CANCELLATION_SOURCES,
                MissionStatus.CANCELLED
        );

        String cancellationReason = reason == null ? "" : reason;

        Mission cancelled = lifecycleService.transition(
                mission,
                MissionStatus.CANCELLED,
                null,
                null,
                terminalTime(mission)
        );

        publishTerminalEvent(
                cancelled,
                StepStatus.CANCELLED,
                cancellationReason.isBlank()
                        ? "Mission cancelled"
                        : cancellationReason,
                Map.of(
                        "terminal", "true",
                        "cancellationReason", cancellationReason
                )
        );

        return cancelled;
    }

    private Mission findMission(String tenantId, String missionId) {
        return missionStore.findById(tenantId, missionId)
                .orElseThrow(() -> new MissionNotFoundException(
                        tenantId,
                        missionId
                ));
    }

    private void publishTerminalEvent(
            Mission mission,
            StepStatus stepStatus,
            String message,
            Map<String, String> metadata
    ) {
        MissionProgressEvent event = new MissionProgressEvent(
                mission.missionId(),
                mission.status(),
                "terminal",
                "Finalization",
                "terminalize",
                "Terminalize mission",
                stepStatus,
                null,
                message,
                100.0,
                terminalResumeToken(mission),
                Objects.requireNonNull(
                        mission.completedAt(),
                        "Terminal mission requires completedAt"
                ),
                metadata
        );

        eventPublisher.publish(mission.tenantId(), event);
    }

    private static Instant terminalTime(Mission mission) {
        return mission.completedAt() == null
                ? Instant.now().truncatedTo(ChronoUnit.MICROS)
                : mission.completedAt();
    }

    private static String terminalResumeToken(Mission mission) {
        return "terminal:"
                + mission.missionId()
                + ":"
                + mission.status().name().toLowerCase();
    }

    private static void requireSource(
            Mission mission,
            Set<MissionStatus> allowedSources,
            MissionStatus target
    ) {
        if (!allowedSources.contains(mission.status())) {
            throw new IllegalStateException(
                    "Cannot terminalize mission from "
                            + mission.status()
                            + " to "
                            + target
            );
        }
    }
}