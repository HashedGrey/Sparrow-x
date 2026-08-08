package com.sparrowx.agentic.temporal.activity;

import com.sparrowx.agentic.actions.document.BuildDocumentEvidenceAction;
import com.sparrowx.agentic.actions.governance.CheckGroundingAction;
import com.sparrowx.agentic.agents.MissionAgent;
import com.sparrowx.agentic.components.IntentComponent.IntentRequest;
import com.sparrowx.agentic.components.PlanningComponent.Observation;
import com.sparrowx.agentic.components.PlanningComponent.PlanningRequest;
import com.sparrowx.agentic.components.ReviewComponent.ReviewRequest;
import com.sparrowx.agentic.components.SynthesisComponent.SynthesisRequest;
import com.sparrowx.agentic.components.ToolSelectionComponent.SelectionRequest;
import com.sparrowx.agentic.mission.MissionEventPublisher;
import com.sparrowx.agentic.mission.Terminalizer;
import com.sparrowx.agentic.mission.artifact.ArtifactPreparationResult;
import com.sparrowx.agentic.mission.evidence.EvidenceRef;
import com.sparrowx.agentic.mission.evidence.EvidenceRegistry;
import com.sparrowx.agentic.mission.model.Mission;
import com.sparrowx.agentic.mission.model.MissionProgressEvent;
import com.sparrowx.agentic.mission.model.MissionRequest;
import com.sparrowx.agentic.mission.model.MissionResult;
import com.sparrowx.agentic.planning.MissionIntent;
import com.sparrowx.agentic.runtime.checkpoint.CheckpointRef;
import com.sparrowx.agentic.runtime.checkpoint.CheckpointSerializer;
import com.sparrowx.agentic.runtime.checkpoint.CheckpointSnapshot;
import com.sparrowx.agentic.runtime.checkpoint.CheckpointStore;
import com.sparrowx.agentic.runtime.gate.ApprovalService;
import com.sparrowx.agentic.steps.BuildDocumentEvidenceStep;
import com.sparrowx.agentic.steps.ParseMissionIntentStep;
import com.sparrowx.agentic.steps.PlanMissionStep;
import com.sparrowx.agentic.steps.RequestHumanApprovalStep;
import com.sparrowx.agentic.steps.ResolveInternalContextStep;
import com.sparrowx.agentic.steps.SynthesizeAnswerStep;
import com.sparrowx.agentic.steps.VerifyGroundingStep;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Component
public final class MissionActivitiesImpl
        implements MissionActivities {

    private static final int OBSERVATION_SCHEMA_VERSION = 1;
    private static final int RESULT_SCHEMA_VERSION = 1;
    private static final int VERIFICATION_SCHEMA_VERSION = 1;

    private final ParseMissionIntentStep parseMissionIntentStep;
    private final PlanMissionStep planMissionStep;
    private final BuildDocumentEvidenceStep documentEvidenceStep;
    private final ResolveInternalContextStep internalContextStep;
    private final RequestHumanApprovalStep humanApprovalStep;
    private final SynthesizeAnswerStep synthesizeAnswerStep;
    private final VerifyGroundingStep verifyGroundingStep;
    private final CheckpointSerializer checkpointSerializer;
    private final CheckpointStore checkpointStore;
    private final MissionEventPublisher eventPublisher;
    private final Terminalizer terminalizer;
    private final ApprovalService approvalService;

    public MissionActivitiesImpl(
            ParseMissionIntentStep parseMissionIntentStep,
            PlanMissionStep planMissionStep,
            BuildDocumentEvidenceStep documentEvidenceStep,
            ResolveInternalContextStep internalContextStep,
            RequestHumanApprovalStep humanApprovalStep,
            SynthesizeAnswerStep synthesizeAnswerStep,
            VerifyGroundingStep verifyGroundingStep,
            CheckpointSerializer checkpointSerializer,
            CheckpointStore checkpointStore,
            MissionEventPublisher eventPublisher,
            Terminalizer terminalizer,
            ApprovalService approvalService
    ) {
        this.parseMissionIntentStep = Objects.requireNonNull(
                parseMissionIntentStep,
                "parseMissionIntentStep must not be null"
        );
        this.planMissionStep = Objects.requireNonNull(
                planMissionStep,
                "planMissionStep must not be null"
        );
        this.documentEvidenceStep = Objects.requireNonNull(
                documentEvidenceStep,
                "documentEvidenceStep must not be null"
        );
        this.internalContextStep = Objects.requireNonNull(
                internalContextStep,
                "internalContextStep must not be null"
        );
        this.humanApprovalStep = Objects.requireNonNull(
                humanApprovalStep,
                "humanApprovalStep must not be null"
        );
        this.synthesizeAnswerStep = Objects.requireNonNull(
                synthesizeAnswerStep,
                "synthesizeAnswerStep must not be null"
        );
        this.verifyGroundingStep = Objects.requireNonNull(
                verifyGroundingStep,
                "verifyGroundingStep must not be null"
        );
        this.checkpointSerializer = Objects.requireNonNull(
                checkpointSerializer,
                "checkpointSerializer must not be null"
        );
        this.checkpointStore = Objects.requireNonNull(
                checkpointStore,
                "checkpointStore must not be null"
        );
        this.eventPublisher = Objects.requireNonNull(
                eventPublisher,
                "eventPublisher must not be null"
        );
        this.terminalizer = Objects.requireNonNull(
                terminalizer,
                "terminalizer must not be null"
        );
        this.approvalService = Objects.requireNonNull(
                approvalService,
                "approvalService must not be null"
        );
    }

    @Override
    public MissionIntent parseIntent(
            IntentActivityRequest request
    ) {
        Objects.requireNonNull(request, "request must not be null");

        MissionRequest missionRequest = loadMissionRequest(
                request.tenantId(),
                request.missionId(),
                request.missionInputRef()
        );

        ArtifactPreparationResult artifacts =
                checkpointSerializer.deserialize(
                        loadSnapshot(
                                request.tenantId(),
                                request.missionId(),
                                request.preparedArtifactsRef()
                        ),
                        ArtifactPreparationResult.class
                );

        IntentRequest intentRequest = new IntentRequest(
                request.missionId(),
                missionRequest.query(),
                artifacts.preparedArtifacts(),
                missionRequest.constraints().preferredPath(),
                Set.copyOf(
                        missionRequest.constraints().allowedTools()
                ),
                Set.copyOf(
                        missionRequest.constraints()
                                .allowedSourceServices()
                ),
                missionRequest.constraints().requiredOutputSections(),
                missionRequest.constraints().requireCitations(),
                missionRequest.constraints().requireHumanReview(),
                missionRequest.constraints().allowExternalSources(),
                Map.of()
        );

        return parseMissionIntentStep.execute(
                missionRequest.context(),
                intentRequest
        );
    }

    @Override
    public MissionAgent.TurnDecision planOrReview(
            PlanActivityRequest request
    ) {
        Objects.requireNonNull(request, "request must not be null");

        MissionRequest missionRequest = loadMissionRequest(
                request.tenantId(),
                request.missionId(),
                request.missionInputRef()
        );

        List<StoredObservation> stored =
                loadStoredObservations(
                        request.tenantId(),
                        request.missionId(),
                        request.observationRefs()
                );

        List<Observation> observations = stored.stream()
                .map(StoredObservation::observation)
                .toList();

        PlanningRequest planningRequest = new PlanningRequest(
                request.missionId(),
                request.intent(),
                request.currentPlan(),
                observations,
                request.completedStepIds(),
                request.allowedTools(),
                request.remainingToolCalls(),
                request.remainingLlmCalls(),
                Map.of()
        );

        ReviewRequest reviewRequest = null;
        if (request.executedStep() != null) {
            Observation latest =
                    observations.get(observations.size() - 1);

            reviewRequest = new ReviewRequest(
                    request.missionId(),
                    request.intent(),
                    request.currentPlan(),
                    request.executedStep(),
                    latest,
                    request.completedStepIds(),
                    request.remainingToolCalls(),
                    request.cancellationRequested(),
                    Map.of()
            );
        }

        SelectionRequest selectionRequest = new SelectionRequest(
                request.intent(),
                request.currentPlan(),
                request.completedStepIds(),
                request.allowedTools(),
                request.remainingToolCalls(),
                Map.of()
        );

        return planMissionStep.execute(
                missionRequest.context(),
                new MissionAgent.TurnInput(
                        planningRequest,
                        reviewRequest,
                        selectionRequest
                )
        );
    }

    @Override
    public OneHopActivityResult executeOneHop(
            OneHopActivityRequest request
    ) {
        Objects.requireNonNull(request, "request must not be null");

        MissionContext context = loadMissionRequest(
                request.tenantId(),
                request.missionId(),
                request.missionInputRef()
        ).context();

        List<EvidenceRef> evidenceRefs;
        List<String> warnings;
        String summary;
        Map<String, Object> attributes;

        if (request.stepKind()
                == com.sparrowx.agentic.planning.StepKind
                .BUILD_DOCUMENT_EVIDENCE) {

            BuildDocumentEvidenceAction.Result result =
                    documentEvidenceStep.execute(
                            context,
                            request.documentSpec()
                    );

            evidenceRefs = result.evidenceRefs();
            warnings = result.warnings();
            summary = "Built document evidence; coverage="
                    + result.coverageScore();
            attributes = Map.of(
                    "coverageScore", result.coverageScore(),
                    "usedChunkRetrieval",
                    result.usedChunkRetrieval(),
                    "usedClaimCache",
                    result.usedClaimCache()
            );
        } else {
            ResolveInternalContextStep.Result result =
                    internalContextStep.execute(
                            context,
                            request.internalRequest()
                    );

            evidenceRefs = result.evidenceRefs();
            warnings = result.warnings();
            summary = result.summary();
            attributes = result.attributes();
        }

        String checkpointId =
                "observation:" + request.effectId();

        Observation observation = new Observation(
                request.stepId(),
                request.stepKind(),
                summary,
                checkpointId,
                attributes
        );

        StoredObservation stored = new StoredObservation(
                observation,
                evidenceRefs,
                warnings,
                attributes
        );

        CheckpointRef reference = saveCheckpoint(
                checkpointId,
                request.tenantId(),
                request.missionId(),
                CheckpointRef.CheckpointType.OBSERVATION,
                OBSERVATION_SCHEMA_VERSION,
                request.observedAt(),
                Map.of(
                        "effectId", request.effectId(),
                        "stepId", request.stepId(),
                        "stepKind", request.stepKind().name()
                ),
                stored
        );

        return new OneHopActivityResult(reference);
    }

    @Override
    public void requestHumanApproval(
            GateActivityRequest request
    ) {
        Objects.requireNonNull(request, "request must not be null");
        humanApprovalStep.execute(request.openRequest());
    }

    @Override
    public void recordHumanGateDecision(
            GateDecisionActivityRequest request
    ) {
        Objects.requireNonNull(request, "request must not be null");

        switch (request.kind()) {
            case APPROVE ->
                    approvalService.approve(request.decisionRequest());
            case REJECT ->
                    approvalService.reject(request.decisionRequest());
            case EXPIRE ->
                    approvalService.expire(request.expiryRequest());
        }
    }

    @Override
    public CheckpointRef synthesizeAnswer(
            SynthesisActivityRequest request
    ) {
        Objects.requireNonNull(request, "request must not be null");

        MissionRequest missionRequest = loadMissionRequest(
                request.tenantId(),
                request.missionId(),
                request.missionInputRef()
        );

        List<StoredObservation> stored =
                loadStoredObservations(
                        request.tenantId(),
                        request.missionId(),
                        request.observationRefs()
                );

        List<Observation> observations = stored.stream()
                .map(StoredObservation::observation)
                .toList();

        EvidenceRegistry registry = new EvidenceRegistry();
        stored.stream()
                .flatMap(value -> value.evidenceRefs().stream())
                .forEach(registry::register);

        SynthesisRequest synthesisRequest =
                new SynthesisRequest(
                        request.missionId(),
                        true,
                        request.intent(),
                        request.finalPlan(),
                        observations,
                        registry.snapshot(),
                        request.governanceDecisions(),
                        request.intent().requiredOutputSections(),
                        Map.of()
                );

        MissionResult result = synthesizeAnswerStep.execute(
                missionRequest.context(),
                synthesisRequest,
                Map.of()
        );

        return saveCheckpoint(
                "result:" + request.effectId(),
                request.tenantId(),
                request.missionId(),
                CheckpointRef.CheckpointType.MISSION_RESULT,
                RESULT_SCHEMA_VERSION,
                request.createdAt(),
                Map.of(
                        "effectId", request.effectId(),
                        "kind", "mission-result"
                ),
                result
        );
    }

    @Override
    public VerificationActivityResult verifyGrounding(
            VerificationActivityRequest request
    ) {
        Objects.requireNonNull(request, "request must not be null");

        MissionResult missionResult =
                checkpointSerializer.deserialize(
                        loadSnapshot(
                                request.tenantId(),
                                request.missionId(),
                                request.missionResultRef()
                        ),
                        MissionResult.class
                );

        if (!request.missionId().equals(
                missionResult.missionId()
        )) {
            throw new IllegalStateException(
                    "mission result belongs to another mission"
            );
        }

        EvidenceRegistry registry = new EvidenceRegistry();
        missionResult.evidenceRefs().forEach(registry::register);

        CheckGroundingAction.CheckSpec effectiveSpec =
                new CheckGroundingAction.CheckSpec(
                        request.spec().decisionId(),
                        request.spec().claims(),
                        missionResult.citations(),
                        request.spec().verifiedEvidenceIds(),
                        request.spec().requirements()
                );

        CheckGroundingAction.Result verification =
                verifyGroundingStep.execute(
                        effectiveSpec,
                        registry
                );

        CheckpointRef reference = saveCheckpoint(
                "verification:" + request.effectId(),
                request.tenantId(),
                request.missionId(),
                CheckpointRef.CheckpointType.OBSERVATION,
                VERIFICATION_SCHEMA_VERSION,
                request.createdAt(),
                Map.of(
                        "effectId", request.effectId(),
                        "kind", "grounding-verification"
                ),
                verification
        );

        return new VerificationActivityResult(
                reference,
                verification.decision()
        );
    }

    @Override
    public MissionProgressEvent publishEvent(
            EventActivityRequest request
    ) {
        Objects.requireNonNull(request, "request must not be null");
        return eventPublisher.publish(
                request.tenantId(),
                request.event()
        );
    }

    @Override
    public TerminalizationResult terminalize(
            TerminalizationRequest request
    ) {
        Objects.requireNonNull(request, "request must not be null");

        Mission terminalMission = switch (request.kind()) {
            case COMPLETE -> terminalizer.complete(
                    request.tenantId(),
                    request.missionId(),
                    loadMissionResult(
                            request.tenantId(),
                            request.missionId(),
                            request.missionResultRef()
                    )
            );
            case FAIL_TERMINAL -> terminalizer.failTerminal(
                    request.tenantId(),
                    request.missionId(),
                    request.failure()
            );
            case CANCEL -> terminalizer.cancel(
                    request.tenantId(),
                    request.missionId(),
                    request.cancellationReason()
            );
        };

        return new TerminalizationResult(
                terminalMission.status(),
                Objects.requireNonNull(
                        terminalMission.completedAt(),
                        "terminal mission requires completedAt"
                )
        );
    }

    private MissionRequest loadMissionRequest(
            String tenantId,
            String missionId,
            CheckpointRef reference
    ) {
        MissionRequest request = checkpointSerializer.deserialize(
                loadSnapshot(tenantId, missionId, reference),
                MissionRequest.class
        );

        if (!tenantId.equals(request.context().tenantId())) {
            throw new IllegalStateException(
                    "mission input tenant mismatch"
            );
        }

        return request;
    }

    private MissionResult loadMissionResult(
            String tenantId,
            String missionId,
            CheckpointRef reference
    ) {
        MissionResult result = checkpointSerializer.deserialize(
                loadSnapshot(tenantId, missionId, reference),
                MissionResult.class
        );

        if (!missionId.equals(result.missionId())) {
            throw new IllegalStateException(
                    "mission result belongs to another mission"
            );
        }

        return result;
    }

    private List<StoredObservation> loadStoredObservations(
            String tenantId,
            String missionId,
            List<CheckpointRef> references
    ) {
        List<StoredObservation> observations = new ArrayList<>();

        for (CheckpointRef reference : references) {
            observations.add(
                    checkpointSerializer.deserialize(
                            loadSnapshot(
                                    tenantId,
                                    missionId,
                                    reference
                            ),
                            StoredObservation.class
                    )
            );
        }

        return List.copyOf(observations);
    }

    private CheckpointSnapshot loadSnapshot(
            String tenantId,
            String missionId,
            CheckpointRef expected
    ) {
        CheckpointSnapshot snapshot = checkpointStore.findById(
                tenantId,
                missionId,
                expected.checkpointId()
        ).orElseThrow(() -> new IllegalArgumentException(
                "checkpoint not found: " + expected.checkpointId()
        ));

        if (!expected.equals(snapshot.reference())) {
            throw new IllegalStateException(
                    "checkpoint reference mismatch: "
                            + expected.checkpointId()
            );
        }

        return snapshot;
    }

    private CheckpointRef saveCheckpoint(
            String checkpointId,
            String tenantId,
            String missionId,
            CheckpointRef.CheckpointType checkpointType,
            int schemaVersion,
            Instant createdAt,
            Map<String, String> metadata,
            Object value
    ) {
        CheckpointSnapshot snapshot = checkpointSerializer.serialize(
                checkpointId,
                tenantId,
                missionId,
                checkpointType,
                schemaVersion,
                createdAt,
                metadata,
                value
        );

        CheckpointRef persisted = Objects.requireNonNull(
                checkpointStore.save(snapshot),
                "checkpointStore.save must not return null"
        );

        if (!snapshot.reference().equals(persisted)) {
            throw new IllegalStateException(
                    "CHECKPOINT_IDEMPOTENCY_CONFLICT: "
                            + checkpointId
            );
        }

        return persisted;
    }
}