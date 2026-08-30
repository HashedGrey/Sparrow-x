package com.sparrowx.agentic.temporal.activity;

import com.sparrowx.agentic.runtime.checkpoint.CheckpointRef;
import com.sparrowx.agentic.runtime.gate.ApprovalService;
import com.sparrowx.agentic.temporal.model.MissionWorkflowCommand;
import com.sparrowx.agentic.temporal.model.MissionWorkflowInput;
import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

import java.time.Instant;
import java.util.Objects;
import java.util.Set;

/**
 * Recovery and enterprise-wait boundaries only.
 * No Embabel intent, plan, action, goal or blackboard type crosses this API.
 */
@ActivityInterface
public interface MissionActivities {

    @ActivityMethod(name = "RunEmbabelMission")
    RunMissionResult runMission(RunMissionRequest request);

    @ActivityMethod(name = "OpenMissionPreflightGate")
    void openGate(OpenGateRequest request);

    @ActivityMethod(name = "RecordMissionGateDecision")
    void recordGateDecision(GateDecisionRequest request);

    @ActivityMethod(name = "CancelMission")
    Instant cancelMission(CancelMissionRequest request);

    record RunMissionRequest(
            MissionWorkflowInput workflowInput,
            Set<String> approvedGateIds,
            Instant startedAt
    ) {
        public RunMissionRequest {
            workflowInput = Objects.requireNonNull(
                    workflowInput,
                    "workflowInput must not be null"
            );
            approvedGateIds = approvedGateIds == null
                    ? Set.of()
                    : Set.copyOf(approvedGateIds);
            startedAt = Objects.requireNonNull(
                    startedAt,
                    "startedAt must not be null"
            );
        }
    }

    record RunMissionResult(
            CheckpointRef resultRef,
            Instant completedAt
    ) {
        public RunMissionResult {
            resultRef = Objects.requireNonNull(
                    resultRef,
                    "resultRef must not be null"
            );
            completedAt = Objects.requireNonNull(
                    completedAt,
                    "completedAt must not be null"
            );
        }
    }

    record OpenGateRequest(
            String effectId,
            ApprovalService.OpenRequest openRequest
    ) {
        public OpenGateRequest {
            effectId = requireText(effectId, "effectId");
            openRequest = Objects.requireNonNull(
                    openRequest,
                    "openRequest must not be null"
            );
        }
    }

    record GateDecisionRequest(
            String effectId,
            MissionWorkflowCommand command,
            Instant decidedAt
    ) {
        public GateDecisionRequest {
            effectId = requireText(effectId, "effectId");
            command = Objects.requireNonNull(
                    command,
                    "command must not be null"
            );
            decidedAt = Objects.requireNonNull(
                    decidedAt,
                    "decidedAt must not be null"
            );
        }
    }

    record CancelMissionRequest(
            String tenantId,
            String missionId,
            String reason
    ) {
        public CancelMissionRequest {
            tenantId = requireText(tenantId, "tenantId");
            missionId = requireText(missionId, "missionId");
            reason = requireText(reason, "reason");
        }
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
