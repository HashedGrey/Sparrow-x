package com.sparrowx.agentic.temporal.activity;

import com.sparrowx.agentic.actions.governance.CheckGroundingAction;
import com.sparrowx.agentic.agents.MissionAgent;
import com.sparrowx.agentic.components.PlanningComponent.Observation;
import com.sparrowx.agentic.governance.model.GovernanceDecision;
import com.sparrowx.agentic.mission.evidence.EvidenceRef;
import com.sparrowx.agentic.mission.model.MissionFailure;
import com.sparrowx.agentic.mission.model.MissionProgressEvent;
import com.sparrowx.agentic.mission.model.MissionStatus;
import com.sparrowx.agentic.planning.MissionIntent;
import com.sparrowx.agentic.planning.MissionPlan;
import com.sparrowx.agentic.planning.PlannedStep;
import com.sparrowx.agentic.planning.StepKind;
import com.sparrowx.agentic.runtime.checkpoint.CheckpointRef;
import com.sparrowx.agentic.runtime.gate.ApprovalService;
import com.sparrowx.agentic.steps.ResolveInternalContextStep;
import com.sparrowx.agentic.tools.document.DocumentEvidenceRequestBuilder.BuildSpec;
import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@ActivityInterface
public interface MissionActivities {

    @ActivityMethod(name = "ParseMissionIntent")
    MissionIntent parseIntent(IntentActivityRequest request);

    @ActivityMethod(name = "PlanOrReviewMission")
    MissionAgent.TurnDecision planOrReview(
            PlanActivityRequest request
    );

    @ActivityMethod(name = "ExecuteOneMissionHop")
    OneHopActivityResult executeOneHop(
            OneHopActivityRequest request
    );

    @ActivityMethod(name = "RequestMissionHumanApproval")
    void requestHumanApproval(GateActivityRequest request);

    @ActivityMethod(name = "RecordMissionHumanGateDecision")
    void recordHumanGateDecision(
            GateDecisionActivityRequest request
    );

    @ActivityMethod(name = "SynthesizeMissionAnswer")
    CheckpointRef synthesizeAnswer(
            SynthesisActivityRequest request
    );

    @ActivityMethod(name = "VerifyMissionGrounding")
    VerificationActivityResult verifyGrounding(
            VerificationActivityRequest request
    );

    @ActivityMethod(name = "PublishMissionEvent")
    MissionProgressEvent publishEvent(EventActivityRequest request);

    @ActivityMethod(name = "TerminalizeMission")
    TerminalizationResult terminalize(
            TerminalizationRequest request
    );

    record IntentActivityRequest(
            String effectId,
            String tenantId,
            String missionId,
            CheckpointRef missionInputRef,
            CheckpointRef preparedArtifactsRef
    ) {
        public IntentActivityRequest {
            effectId = requireText(effectId, "effectId");
            tenantId = requireText(tenantId, "tenantId");
            missionId = requireText(missionId, "missionId");
            requireScopedRef(
                    missionInputRef,
                    tenantId,
                    missionId,
                    CheckpointRef.CheckpointType.MISSION_INPUT,
                    "missionInputRef"
            );
            requireScopedRef(
                    preparedArtifactsRef,
                    tenantId,
                    missionId,
                    CheckpointRef.CheckpointType.PREPARED_ARTIFACTS,
                    "preparedArtifactsRef"
            );
        }
    }

    record PlanActivityRequest(
            String effectId,
            String tenantId,
            String missionId,
            CheckpointRef missionInputRef,
            MissionIntent intent,
            MissionPlan currentPlan,
            PlannedStep executedStep,
            Set<String> completedStepIds,
            List<CheckpointRef> observationRefs,
            Set<String> allowedTools,
            int remainingToolCalls,
            int remainingLlmCalls,
            boolean cancellationRequested
    ) {
        public PlanActivityRequest {
            effectId = requireText(effectId, "effectId");
            tenantId = requireText(tenantId, "tenantId");
            missionId = requireText(missionId, "missionId");
            requireScopedRef(
                    missionInputRef,
                    tenantId,
                    missionId,
                    CheckpointRef.CheckpointType.MISSION_INPUT,
                    "missionInputRef"
            );
            intent = Objects.requireNonNull(
                    intent,
                    "intent must not be null"
            );
            if (!missionId.equals(intent.missionId())) {
                throw new IllegalArgumentException(
                        "intent belongs to another mission"
                );
            }
            if (currentPlan != null
                    && !missionId.equals(currentPlan.missionId())) {
                throw new IllegalArgumentException(
                        "currentPlan belongs to another mission"
                );
            }
            completedStepIds = completedStepIds == null
                    ? Set.of()
                    : Set.copyOf(completedStepIds);
            observationRefs = copyObservationRefs(
                    observationRefs,
                    tenantId,
                    missionId
            );
            allowedTools = allowedTools == null
                    ? Set.of()
                    : Set.copyOf(allowedTools);
            if (remainingToolCalls < 0) {
                throw new IllegalArgumentException(
                        "remainingToolCalls must not be negative"
                );
            }
            if (remainingLlmCalls < 0) {
                throw new IllegalArgumentException(
                        "remainingLlmCalls must not be negative"
                );
            }
            if (executedStep != null
                    && (currentPlan == null
                    || observationRefs.isEmpty())) {
                throw new IllegalArgumentException(
                        "executedStep requires a plan and observation"
                );
            }
        }
    }

    record OneHopActivityRequest(
            String effectId,
            String tenantId,
            String missionId,
            CheckpointRef missionInputRef,
            String stepId,
            StepKind stepKind,
            BuildSpec documentSpec,
            ResolveInternalContextStep.Request internalRequest,
            Instant observedAt
    ) {
        public OneHopActivityRequest {
            effectId = requireText(effectId, "effectId");
            tenantId = requireText(tenantId, "tenantId");
            missionId = requireText(missionId, "missionId");
            stepId = requireText(stepId, "stepId");
            stepKind = Objects.requireNonNull(
                    stepKind,
                    "stepKind must not be null"
            );
            observedAt = Objects.requireNonNull(
                    observedAt,
                    "observedAt must not be null"
            );
            requireScopedRef(
                    missionInputRef,
                    tenantId,
                    missionId,
                    CheckpointRef.CheckpointType.MISSION_INPUT,
                    "missionInputRef"
            );

            if (stepKind == StepKind.BUILD_DOCUMENT_EVIDENCE) {
                Objects.requireNonNull(
                        documentSpec,
                        "documentSpec is required"
                );
                if (internalRequest != null) {
                    throw new IllegalArgumentException(
                            "internalRequest is not allowed "
                                    + "for document evidence"
                    );
                }
                requireEffectId(
                        effectId,
                        documentSpec.requestId()
                );
            } else if (stepKind.isInternalOperation()) {
                Objects.requireNonNull(
                        internalRequest,
                        "internalRequest is required"
                );
                if (documentSpec != null) {
                    throw new IllegalArgumentException(
                            "documentSpec is not allowed "
                                    + "for internal context"
                    );
                }
                requireInternalOperation(stepKind, internalRequest);
                requireEffectId(
                        effectId,
                        internalRequestId(internalRequest)
                );
            } else {
                throw new IllegalArgumentException(
                        "unsupported reactor hop: " + stepKind
                );
            }
        }
    }

    record OneHopActivityResult(
            CheckpointRef observationRef
    ) {
        public OneHopActivityResult {
            observationRef = Objects.requireNonNull(
                    observationRef,
                    "observationRef must not be null"
            );
            if (observationRef.checkpointType()
                    != CheckpointRef.CheckpointType.OBSERVATION) {
                throw new IllegalArgumentException(
                        "observationRef must reference an observation"
                );
            }
        }
    }

    record StoredObservation(
            Observation observation,
            List<EvidenceRef> evidenceRefs,
            List<String> warnings,
            Map<String, Object> attributes
    ) {
        public StoredObservation {
            observation = Objects.requireNonNull(
                    observation,
                    "observation must not be null"
            );
            evidenceRefs = evidenceRefs == null
                    ? List.of()
                    : List.copyOf(evidenceRefs);
            warnings = warnings == null
                    ? List.of()
                    : List.copyOf(warnings);
            attributes = attributes == null
                    ? Map.of()
                    : Map.copyOf(attributes);
        }
    }

    record GateActivityRequest(
            String effectId,
            ApprovalService.OpenRequest openRequest
    ) {
        public GateActivityRequest {
            effectId = requireText(effectId, "effectId");
            openRequest = Objects.requireNonNull(
                    openRequest,
                    "openRequest must not be null"
            );
            requireEffectId(effectId, openRequest.gateId());
        }
    }

    record GateDecisionActivityRequest(
            String effectId,
            GateDecisionKind kind,
            ApprovalService.DecisionRequest decisionRequest,
            ApprovalService.ExpiryRequest expiryRequest
    ) {
        public GateDecisionActivityRequest {
            effectId = requireText(effectId, "effectId");
            kind = Objects.requireNonNull(
                    kind,
                    "kind must not be null"
            );

            if (kind == GateDecisionKind.EXPIRE) {
                Objects.requireNonNull(
                        expiryRequest,
                        "expiryRequest is required for EXPIRE"
                );
                if (decisionRequest != null) {
                    throw new IllegalArgumentException(
                            "decisionRequest is not allowed for EXPIRE"
                    );
                }
            } else {
                Objects.requireNonNull(
                        decisionRequest,
                        "decisionRequest is required"
                );
                if (expiryRequest != null) {
                    throw new IllegalArgumentException(
                            "expiryRequest is allowed only for EXPIRE"
                    );
                }
            }
        }
    }

    record SynthesisActivityRequest(
            String effectId,
            String tenantId,
            String missionId,
            CheckpointRef missionInputRef,
            MissionIntent intent,
            MissionPlan finalPlan,
            List<CheckpointRef> observationRefs,
            List<GovernanceDecision> governanceDecisions,
            Instant createdAt
    ) {
        public SynthesisActivityRequest {
            effectId = requireText(effectId, "effectId");
            tenantId = requireText(tenantId, "tenantId");
            missionId = requireText(missionId, "missionId");
            requireScopedRef(
                    missionInputRef,
                    tenantId,
                    missionId,
                    CheckpointRef.CheckpointType.MISSION_INPUT,
                    "missionInputRef"
            );
            intent = Objects.requireNonNull(
                    intent,
                    "intent must not be null"
            );
            finalPlan = Objects.requireNonNull(
                    finalPlan,
                    "finalPlan must not be null"
            );
            if (!missionId.equals(intent.missionId())
                    || !missionId.equals(finalPlan.missionId())) {
                throw new IllegalArgumentException(
                        "synthesis state belongs to another mission"
                );
            }
            observationRefs = copyObservationRefs(
                    observationRefs,
                    tenantId,
                    missionId
            );
            governanceDecisions =
                    governanceDecisions == null
                            ? List.of()
                            : List.copyOf(governanceDecisions);
            createdAt = Objects.requireNonNull(
                    createdAt,
                    "createdAt must not be null"
            );
        }
    }

    record VerificationActivityRequest(
            String effectId,
            String tenantId,
            String missionId,
            CheckpointRef missionResultRef,
            CheckGroundingAction.CheckSpec spec,
            Instant createdAt
    ) {
        public VerificationActivityRequest {
            effectId = requireText(effectId, "effectId");
            tenantId = requireText(tenantId, "tenantId");
            missionId = requireText(missionId, "missionId");
            requireScopedRef(
                    missionResultRef,
                    tenantId,
                    missionId,
                    CheckpointRef.CheckpointType.MISSION_RESULT,
                    "missionResultRef"
            );
            spec = Objects.requireNonNull(
                    spec,
                    "spec must not be null"
            );
            createdAt = Objects.requireNonNull(
                    createdAt,
                    "createdAt must not be null"
            );
        }
    }

    record VerificationActivityResult(
            CheckpointRef verificationRef,
            GovernanceDecision decision
    ) {
        public VerificationActivityResult {
            verificationRef = Objects.requireNonNull(
                    verificationRef,
                    "verificationRef must not be null"
            );
            decision = Objects.requireNonNull(
                    decision,
                    "decision must not be null"
            );
        }
    }

    record EventActivityRequest(
            String effectId,
            String tenantId,
            MissionProgressEvent event
    ) {
        public EventActivityRequest {
            effectId = requireText(effectId, "effectId");
            tenantId = requireText(tenantId, "tenantId");
            event = Objects.requireNonNull(
                    event,
                    "event must not be null"
            );
            requireEffectId(effectId, event.resumeToken());
        }
    }

    record TerminalizationRequest(
            String effectId,
            String tenantId,
            String missionId,
            TerminalizationKind kind,
            CheckpointRef missionResultRef,
            MissionFailure failure,
            String cancellationReason
    ) {
        public TerminalizationRequest {
            effectId = requireText(effectId, "effectId");
            tenantId = requireText(tenantId, "tenantId");
            missionId = requireText(missionId, "missionId");
            kind = Objects.requireNonNull(
                    kind,
                    "kind must not be null"
            );
            cancellationReason = cancellationReason == null
                    ? ""
                    : cancellationReason;

            switch (kind) {
                case COMPLETE -> {
                    requireScopedRef(
                            missionResultRef,
                            tenantId,
                            missionId,
                            CheckpointRef.CheckpointType.MISSION_RESULT,
                            "missionResultRef"
                    );
                    if (failure != null
                            || !cancellationReason.isBlank()) {
                        throw new IllegalArgumentException(
                                "completion accepts only a result reference"
                        );
                    }
                }
                case FAIL_TERMINAL -> {
                    Objects.requireNonNull(
                            failure,
                            "failure is required"
                    );
                    if (failure.retryable()) {
                        throw new IllegalArgumentException(
                                "terminal failure must not be retryable"
                        );
                    }
                    if (missionResultRef != null
                            || !cancellationReason.isBlank()) {
                        throw new IllegalArgumentException(
                                "failure accepts only a MissionFailure"
                        );
                    }
                }
                case CANCEL -> {
                    if (missionResultRef != null || failure != null) {
                        throw new IllegalArgumentException(
                                "cancellation accepts only a reason"
                        );
                    }
                }
            }
        }
    }

    record TerminalizationResult(
            MissionStatus status,
            Instant completedAt
    ) {
        public TerminalizationResult {
            status = Objects.requireNonNull(
                    status,
                    "status must not be null"
            );
            completedAt = Objects.requireNonNull(
                    completedAt,
                    "completedAt must not be null"
            );
        }
    }

    enum TerminalizationKind {
        COMPLETE,
        FAIL_TERMINAL,
        CANCEL
    }

    enum GateDecisionKind {
        APPROVE,
        REJECT,
        EXPIRE
    }

    private static List<CheckpointRef> copyObservationRefs(
            List<CheckpointRef> references,
            String tenantId,
            String missionId
    ) {
        List<CheckpointRef> copied = references == null
                ? List.of()
                : List.copyOf(references);

        for (CheckpointRef reference : copied) {
            requireScopedRef(
                    reference,
                    tenantId,
                    missionId,
                    CheckpointRef.CheckpointType.OBSERVATION,
                    "observationRef"
            );
        }

        return copied;
    }

    private static void requireScopedRef(
            CheckpointRef reference,
            String tenantId,
            String missionId,
            CheckpointRef.CheckpointType type,
            String field
    ) {
        Objects.requireNonNull(
                reference,
                field + " must not be null"
        );

        if (!tenantId.equals(reference.tenantId())
                || !missionId.equals(reference.missionId())
                || type != reference.checkpointType()) {
            throw new IllegalArgumentException(
                    field + " has the wrong mission scope or type"
            );
        }
    }

    private static void requireEffectId(
            String effectId,
            String downstreamId
    ) {
        if (!effectId.equals(downstreamId)) {
            throw new IllegalArgumentException(
                    "stable effect id mismatch"
            );
        }
    }

    private static void requireInternalOperation(
            StepKind stepKind,
            ResolveInternalContextStep.Request request
    ) {
        ResolveInternalContextStep.Operation expected = switch (stepKind) {
            case SEARCH_INTERNAL_ENTITIES ->
                    ResolveInternalContextStep.Operation.SEARCH_ENTITIES;
            case READ_INTERNAL_COMPANY_GRAPH ->
                    ResolveInternalContextStep.Operation.READ_COMPANY_GRAPH;
            case READ_LEARNING_GRAPH ->
                    ResolveInternalContextStep.Operation.READ_LEARNING_GRAPH;
            default -> throw new IllegalArgumentException(
                    "unsupported internal step: " + stepKind
            );
        };

        if (request.operation() != expected) {
            throw new IllegalArgumentException(
                    "internal operation does not match stepKind"
            );
        }
    }

    private static String internalRequestId(
            ResolveInternalContextStep.Request request
    ) {
        return request.operation()
                == ResolveInternalContextStep.Operation.SEARCH_ENTITIES
                ? request.searchSpec().requestId()
                : request.graphSpec().requestId();
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    field + " must not be blank"
            );
        }
        return value;
    }
}