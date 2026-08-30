package com.sparrowx.agentic.temporal.activity;

import com.sparrowx.agentic.agents.EmbabelMissionRunner;
import com.sparrowx.agentic.agents.MissionRunInput;
import com.sparrowx.agentic.exceptions.MissionNotFoundException;
import com.sparrowx.agentic.mission.MissionLifecycleService;
import com.sparrowx.agentic.mission.Terminalizer;
import com.sparrowx.agentic.mission.artifact.ArtifactPreparationResult;
import com.sparrowx.agentic.mission.model.Mission;
import com.sparrowx.agentic.mission.model.MissionRequest;
import com.sparrowx.agentic.mission.model.MissionResult;
import com.sparrowx.agentic.mission.model.MissionStatus;
import com.sparrowx.agentic.mission.store.MissionStore;
import com.sparrowx.agentic.runtime.checkpoint.CheckpointRef;
import com.sparrowx.agentic.runtime.checkpoint.CheckpointSerializer;
import com.sparrowx.agentic.runtime.checkpoint.CheckpointSnapshot;
import com.sparrowx.agentic.runtime.checkpoint.CheckpointStore;
import com.sparrowx.agentic.runtime.gate.ApprovalService;
import com.sparrowx.agentic.temporal.model.MissionWorkflowCommand;
import com.sparrowx.agentic.temporal.model.MissionWorkflowInput;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Objects;

/**
 * Hydrates immutable business input, invokes one complete Embabel process and
 * persists only the terminal result. Temporal retries this Activity on failure.
 */
@Component
public final class MissionActivitiesImpl implements MissionActivities {

    private final MissionStore missionStore;
    private final MissionLifecycleService lifecycleService;
    private final CheckpointStore checkpointStore;
    private final CheckpointSerializer checkpointSerializer;
    private final EmbabelMissionRunner missionRunner;
    private final ApprovalService approvalService;
    private final Terminalizer terminalizer;

    public MissionActivitiesImpl(
            MissionStore missionStore,
            MissionLifecycleService lifecycleService,
            CheckpointStore checkpointStore,
            CheckpointSerializer checkpointSerializer,
            EmbabelMissionRunner missionRunner,
            ApprovalService approvalService,
            Terminalizer terminalizer
    ) {
        this.missionStore = Objects.requireNonNull(missionStore, "missionStore must not be null");
        this.lifecycleService = Objects.requireNonNull(lifecycleService, "lifecycleService must not be null");
        this.checkpointStore = Objects.requireNonNull(checkpointStore, "checkpointStore must not be null");
        this.checkpointSerializer = Objects.requireNonNull(checkpointSerializer, "checkpointSerializer must not be null");
        this.missionRunner = Objects.requireNonNull(missionRunner, "missionRunner must not be null");
        this.approvalService = Objects.requireNonNull(approvalService, "approvalService must not be null");
        this.terminalizer = Objects.requireNonNull(terminalizer, "terminalizer must not be null");
    }

    @Override
    public RunMissionResult runMission(RunMissionRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        MissionWorkflowInput workflow = request.workflowInput();

        MissionRequest missionRequest = deserialize(
                workflow.missionInputRef(),
                MissionRequest.class
        );
        ArtifactPreparationResult artifacts = deserialize(
                workflow.preparedArtifactsRef(),
                ArtifactPreparationResult.class
        );

        if (!workflow.tenantId().equals(
                missionRequest.context().tenantId())) {
            throw new IllegalStateException(
                    "hydrated mission input belongs to another tenant"
            );
        }

        Mission existingMission = missionStore.findById(
                workflow.tenantId(),
                workflow.missionId()
        ).orElseThrow(() -> new MissionNotFoundException(
                workflow.tenantId(),
                workflow.missionId()
        ));

        if (existingMission.status() == MissionStatus.COMPLETED) {
            return resumeCompletedMission(workflow);
        }

        ensureRunning(workflow, request.startedAt());

        MissionResult result = missionRunner.run(new MissionRunInput(
                workflow.missionId(),
                missionRequest,
                artifacts,
                request.approvedGateIds(),
                request.startedAt()
        ));


        Mission terminalMission = terminalizer.complete(
                workflow.tenantId(),
                workflow.missionId(),
                result
        );

        CheckpointRef resultRef = checkpointStore.findLatest(
                        workflow.tenantId(),
                        workflow.missionId(),
                        CheckpointRef.CheckpointType.MISSION_RESULT
                )
                .map(CheckpointSnapshot::reference)
                .orElseThrow(() -> new IllegalStateException(
                        "terminal result checkpoint was not persisted"
                ));

        return new RunMissionResult(
                resultRef,
                Objects.requireNonNull(
                        terminalMission.completedAt(),
                        "terminal mission must have completedAt"
                )
        );
    }

    @Override
    public void openGate(OpenGateRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        approvalService.open(request.openRequest());
    }

    @Override
    public void recordGateDecision(GateDecisionRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        MissionWorkflowCommand command = request.command();
        ApprovalService.DecisionRequest decision =
                new ApprovalService.DecisionRequest(
                        command.tenantId(),
                        command.missionId(),
                        command.gateId(),
                        command.actorUserId(),
                        command.actorRoles(),
                        command.reason(),
                        request.decidedAt()
                );

        switch (command.type()) {
            case APPROVE -> approvalService.approve(decision);
            case REJECT -> approvalService.reject(decision);
            case CANCEL -> throw new IllegalArgumentException(
                    "cancel is not a gate decision"
            );
        }
    }

    @Override
    public Instant cancelMission(CancelMissionRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        Mission mission = terminalizer.cancel(
                request.tenantId(),
                request.missionId(),
                request.reason()
        );
        return Objects.requireNonNull(
                mission.completedAt(),
                "cancelled mission must have completedAt"
        );
    }

    private Mission ensureRunning(
            MissionWorkflowInput workflow,
            Instant startedAt
    ) {
        Mission mission = missionStore.findById(
                workflow.tenantId(),
                workflow.missionId()
        ).orElseThrow(() -> new MissionNotFoundException(workflow.tenantId(), workflow.missionId()
        ));

        if (mission.status() == MissionStatus.RUNNING) {
            return mission;
        }

        if (mission.status() != MissionStatus.SUBMITTED) {
            throw new IllegalStateException(
                    "Cannot start mission from "
                            + mission.status()
            );
        }

        return lifecycleService.transition(
                mission,
                MissionStatus.RUNNING,
                null,
                null,
                Objects.requireNonNull(
                        startedAt,
                        "startedAt must not be null"
                )
        );
    }

    private RunMissionResult resumeCompletedMission(
            MissionWorkflowInput workflow
    ) {
        CheckpointSnapshot snapshot = checkpointStore.findLatest(
                workflow.tenantId(),
                workflow.missionId(),
                CheckpointRef.CheckpointType.MISSION_RESULT
        ).orElseThrow(() -> new IllegalStateException(
                "completed mission has no result checkpoint"
        ));

        MissionResult result = checkpointSerializer.deserialize(snapshot, MissionResult.class);

        Mission terminalMission = terminalizer.complete(workflow.tenantId(), workflow.missionId(), result);

        CheckpointSnapshot persistedResult = checkpointStore.findLatest(workflow.tenantId(),
                workflow.missionId(),
                CheckpointRef.CheckpointType.MISSION_RESULT
        ).orElseThrow(() -> new IllegalStateException(
                "terminal result checkpoint was not persisted"
        ));

        return new RunMissionResult(persistedResult.reference(),
                Objects.requireNonNull(
                        terminalMission.completedAt(),
                        "completed mission must have completedAt"
                )
        );
    }

    private <T> T deserialize(
            CheckpointRef reference,
            Class<T> type
    ) {
        CheckpointSnapshot snapshot = checkpointStore.findById(
                reference.tenantId(),
                reference.missionId(),
                reference.checkpointId()
        ).orElseThrow(() -> new IllegalArgumentException(
                "checkpoint not found: " + reference.checkpointId()
        ));

        if (!reference.equals(snapshot.reference())) {
            throw new IllegalStateException(
                    "checkpoint reference mismatch: "
                            + reference.checkpointId()
            );
        }
        return checkpointSerializer.deserialize(snapshot, type);
    }


}
