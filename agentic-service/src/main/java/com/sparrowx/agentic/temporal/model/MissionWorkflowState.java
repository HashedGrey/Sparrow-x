package com.sparrowx.agentic.temporal.model;

import com.sparrowx.agentic.mission.model.MissionStatus;
import com.sparrowx.agentic.runtime.checkpoint.CheckpointRef;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Minimal durable recovery/wait state. It deliberately contains no Embabel
 * goals, actions, plan, observations or blackboard values.
 */
public record MissionWorkflowState(
        String missionId,
        String tenantId,
        MissionStatus status,
        PendingGate pendingGate,
        Set<String> approvedGateIds,
        Map<String, String> processedUpdateFingerprints,
        String cancellationReason,
        CheckpointRef resultRef,
        Instant startedAt,
        Instant completedAt,
        Instant cancelledAt,
        Instant approvedAt,
        Instant rejectedAt
) {

    public MissionWorkflowState {
        missionId = requireText(missionId, "missionId");
        tenantId = requireText(tenantId, "tenantId");
        status = Objects.requireNonNull(status, "status must not be null");
        approvedGateIds = approvedGateIds == null
                ? Set.of()
                : Set.copyOf(approvedGateIds);
        processedUpdateFingerprints = processedUpdateFingerprints == null
                ? Map.of()
                : Map.copyOf(processedUpdateFingerprints);
        cancellationReason = cancellationReason == null
                ? ""
                : cancellationReason;
        startedAt = Objects.requireNonNull(
                startedAt,
                "startedAt must not be null"
        );
    }

    public static MissionWorkflowState initial(
            MissionWorkflowInput input,
            Instant startedAt
    ) {
        return new MissionWorkflowState(
                input.missionId(),
                input.tenantId(),
                MissionStatus.SUBMITTED,
                null,
                Set.of(),
                Map.of(),
                "",
                null,
                startedAt,
                null,
                null,
                null,
                null
        );
    }

    public MissionWorkflowState waitingFor(PendingGate gate) {
        return copy(
                MissionStatus.RUNNING,
                Objects.requireNonNull(gate, "gate must not be null"),
                approvedGateIds,
                processedUpdateFingerprints,
                "",
                resultRef,
                null,
                null
        );
    }

    public MissionWorkflowState running() {
        return copy(
                MissionStatus.RUNNING,
                null,
                approvedGateIds,
                processedUpdateFingerprints,
                "",
                resultRef,
                null,
                null
        );
    }

    public MissionWorkflowState approved(
            MissionWorkflowCommand command,
            Instant decidedAt
    ) {
        requirePendingGate(command);
        Objects.requireNonNull(decidedAt, "decidedAt must not be null");
        Set<String> approvals = new LinkedHashSet<>(approvedGateIds);
        approvals.add(command.gateId());
        MissionWorkflowState next = copy(
                MissionStatus.RUNNING,
                null,
                approvals,
                withProcessed(command),
                "",
                resultRef,
                null,
                null
        );
        return next.withDecisionTimes(decidedAt, rejectedAt);
    }

    public MissionWorkflowState rejected(
            MissionWorkflowCommand command,
            Instant decidedAt
    ) {
        requirePendingGate(command);
        MissionWorkflowState next = copy(
                MissionStatus.CANCELLED,
                null,
                approvedGateIds,
                withProcessed(command),
                "Gate rejected: " + command.reason(),
                null,
                decidedAt,
                decidedAt
        );
        return next.withDecisionTimes(approvedAt, decidedAt);
    }

    public MissionWorkflowState cancelled(
            MissionWorkflowCommand command,
            Instant decidedAt
    ) {
        requireCommand(command);
        return copy(
                MissionStatus.CANCELLED,
                null,
                approvedGateIds,
                withProcessed(command),
                command.reason(),
                null,
                decidedAt,
                decidedAt
        );
    }

    public MissionWorkflowState timedOut(
            String reason,
            Instant decidedAt
    ) {
        return copy(
                MissionStatus.CANCELLED,
                null,
                approvedGateIds,
                processedUpdateFingerprints,
                requireText(reason, "reason"),
                null,
                decidedAt,
                decidedAt
        );
    }

    public MissionWorkflowState completed(
            CheckpointRef reference,
            Instant completionTime
    ) {
        return copy(
                MissionStatus.COMPLETED,
                null,
                approvedGateIds,
                processedUpdateFingerprints,
                "",
                Objects.requireNonNull(
                        reference,
                        "reference must not be null"
                ),
                Objects.requireNonNull(
                        completionTime,
                        "completionTime must not be null"
                ),
                null
        );
    }

    public boolean terminal() {
        return status == MissionStatus.COMPLETED
                || status == MissionStatus.CANCELLED
                || status == MissionStatus.FAILED_TERMINAL;
    }

    /** Compatibility accessor for handlers written against the old state. */
    public MissionStatus currentStatus() {
        return status;
    }

    public boolean alreadyProcessed(MissionWorkflowCommand command) {
        requireCommand(command);
        String fingerprint = processedUpdateFingerprints.get(
                command.updateId()
        );
        if (fingerprint == null) {
            return false;
        }
        if (!fingerprint.equals(command.fingerprint())) {
            throw new IllegalStateException(
                    "WORKFLOW_UPDATE_ID_CONFLICT: " + command.updateId()
            );
        }
        return true;
    }

    public void requirePendingGate(MissionWorkflowCommand command) {
        requireCommand(command);
        if (pendingGate == null
                || !pendingGate.gateId().equals(command.gateId())) {
            throw new IllegalStateException(
                    "command does not target the pending gate"
            );
        }
    }

    public void requireCommand(MissionWorkflowCommand command) {
        Objects.requireNonNull(command, "command must not be null");
        if (!missionId.equals(command.missionId())
                || !tenantId.equals(command.tenantId())) {
            throw new IllegalArgumentException(
                    "command belongs to another mission"
            );
        }
    }

    private Map<String, String> withProcessed(
            MissionWorkflowCommand command
    ) {
        Map<String, String> values = new LinkedHashMap<>(
                processedUpdateFingerprints
        );
        String previous = values.put(
                command.updateId(),
                command.fingerprint()
        );
        if (previous != null
                && !previous.equals(command.fingerprint())) {
            throw new IllegalStateException(
                    "WORKFLOW_UPDATE_ID_CONFLICT: " + command.updateId()
            );
        }
        return Map.copyOf(values);
    }

    private MissionWorkflowState copy(
            MissionStatus nextStatus,
            PendingGate nextGate,
            Set<String> approvals,
            Map<String, String> updates,
            String cancelReason,
            CheckpointRef nextResult,
            Instant nextCompletedAt,
            Instant nextCancelledAt
    ) {
        return new MissionWorkflowState(
                missionId,
                tenantId,
                nextStatus,
                nextGate,
                approvals,
                updates,
                cancelReason,
                nextResult,
                startedAt,
                nextCompletedAt,
                nextCancelledAt,
                approvedAt,
                rejectedAt
        );
    }

    private MissionWorkflowState withDecisionTimes(
            Instant nextApprovedAt,
            Instant nextRejectedAt
    ) {
        return new MissionWorkflowState(
                missionId,
                tenantId,
                status,
                pendingGate,
                approvedGateIds,
                processedUpdateFingerprints,
                cancellationReason,
                resultRef,
                startedAt,
                completedAt,
                cancelledAt,
                nextApprovedAt,
                nextRejectedAt
        );
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    field + " must not be blank"
            );
        }
        return value.trim();
    }

    public record PendingGate(
            String gateId,
            String title,
            String reason,
            Set<String> requiredReviewerRoles,
            Instant createdAt,
            Instant expiresAt
    ) {
        public PendingGate {
            gateId = requireText(gateId, "gateId");
            title = requireText(title, "title");
            reason = requireText(reason, "reason");
            requiredReviewerRoles = requiredReviewerRoles == null
                    ? Set.of()
                    : Set.copyOf(requiredReviewerRoles);
            createdAt = Objects.requireNonNull(
                    createdAt,
                    "createdAt must not be null"
            );
            expiresAt = Objects.requireNonNull(
                    expiresAt,
                    "expiresAt must not be null"
            );
            if (!expiresAt.isAfter(createdAt)) {
                throw new IllegalArgumentException(
                        "expiresAt must be after createdAt"
                );
            }
        }
    }
}
