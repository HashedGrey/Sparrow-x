package com.sparrowx.agentic.temporal.workflow;

import com.sparrowx.agentic.actions.governance.CheckGroundingAction;
import com.sparrowx.agentic.agents.MissionAgent;
import com.sparrowx.agentic.governance.GroundingPolicy;
import com.sparrowx.agentic.governance.model.GovernanceDecisionType;
import com.sparrowx.agentic.mission.model.MissionFailure;
import com.sparrowx.agentic.mission.model.MissionFailureReason;
import com.sparrowx.agentic.mission.model.MissionProgressEvent;
import com.sparrowx.agentic.mission.model.MissionStatus;
import com.sparrowx.agentic.planning.MissionPlan;
import com.sparrowx.agentic.planning.PlannedStep;
import com.sparrowx.agentic.planning.StepKind;
import com.sparrowx.agentic.runtime.checkpoint.CheckpointRef;
import com.sparrowx.agentic.runtime.gate.ApprovalService;
import com.sparrowx.agentic.runtime.model.StepStatus;
import com.sparrowx.agentic.steps.ResolveInternalContextStep;
import com.sparrowx.agentic.temporal.activity.MissionActivities;
import com.sparrowx.agentic.temporal.model.MissionWorkflowCommand;
import com.sparrowx.agentic.temporal.model.MissionWorkflowInput;
import com.sparrowx.agentic.temporal.model.MissionWorkflowOutcome;
import com.sparrowx.agentic.temporal.model.MissionWorkflowState;
import com.sparrowx.agentic.temporal.model.MissionWorkflowState.PendingGate;
import com.sparrowx.agentic.tools.document.DocumentEvidenceRequestBuilder.BuildSpec;
import com.sparrowx.agentic.tools.document.DocumentSpanSearchRequestBuilder.Scope;
import com.sparrowx.agentic.tools.internal.InternalEntitySearchRequestBuilder.SearchSpec;
import com.sparrowx.agentic.tools.internal.InternalGraphRequestBuilder.GraphSpec;
import com.sparrowx.document.proto.EvidenceGoalProto;
import com.sparrowx.document.proto.EvidenceNodeTypeProto;
import com.sparrowx.document.proto.EvidenceRelationTypeProto;
import com.sparrowx.document.proto.RetrievalModeProto;
import com.sparrowx.internal.grpc.InternalGraphNodeType;
import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.failure.CanceledFailure;
import io.temporal.workflow.CancellationScope;
import io.temporal.workflow.Workflow;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

/**
 * Deterministic Temporal owner of the durable one-hop mission reactor.
 */
public final class MissionWorkflowImpl
        implements MissionWorkflow {

    private static final int CONTINUE_AS_NEW_AFTER_HOPS = 50;
    private static final int DEFAULT_HYDRATION_LIMIT = 20;
    private static final long DEFAULT_GATE_TIMEOUT_SECONDS =
            7L * 24L * 60L * 60L;
    private static final long MAX_GATE_TIMEOUT_SECONDS =
            30L * 24L * 60L * 60L;

    private final MissionActivities activities =
            Workflow.newActivityStub(
                    MissionActivities.class,
                    ActivityOptions.newBuilder()
                            .setStartToCloseTimeout(
                                    Duration.ofMinutes(10)
                            )
                            .setRetryOptions(
                                    RetryOptions.newBuilder()
                                            .setInitialInterval(
                                                    Duration.ofSeconds(1)
                                            )
                                            .setBackoffCoefficient(2.0d)
                                            .setMaximumInterval(
                                                    Duration.ofMinutes(1)
                                            )
                                            .setMaximumAttempts(5)
                                            .build()
                            )
                            .build()
            );

    private MissionWorkflowInput input;
    private MissionWorkflowState workflowState;
    private CancellationScope reactorScope;

    @Override
    public MissionWorkflowOutcome run(
            MissionWorkflowInput workflowInput,
            MissionWorkflowState continuedState
    ) {
        input = Objects.requireNonNull(
                workflowInput,
                "workflowInput must not be null"
        );

        if (continuedState == null) {
            workflowState = MissionWorkflowState.initial(
                    input,
                    now()
            );
        } else {
            requireContinuation(input, continuedState);
            workflowState = continuedState;
        }

        if (!workflowState.terminal()) {
            reactorScope = Workflow.newCancellationScope(
                    this::runReactor
            );

            try {
                reactorScope.run();
            } catch (CanceledFailure cancelled) {
                handleScopeCancellation(cancelled);
            } catch (RuntimeException failure) {
                if (!workflowState.terminal()) {
                    failTerminal(
                            "MISSION_REACTOR_FAILED",
                            safeMessage(failure),
                            currentStepId()
                    );
                }
            }
        }

        if (!workflowState.terminal()) {
            Workflow.await(() -> workflowState.terminal());
        }

        Workflow.await(
                () -> Workflow.isEveryHandlerFinished()
        );

        return terminalOutcome();
    }

    @Override
    public MissionWorkflowState approve(
            MissionWorkflowCommand command
    ) {
        awaitInitialized();
        requireCommandType(
                command,
                MissionWorkflowCommand.CommandType.APPROVE
        );
        workflowState.requireCommand(command);

        if (workflowState.alreadyProcessed(command)) {
            return workflowState;
        }
        requireOpenWorkflow();
        workflowState.requirePendingGate(command.gateId());

        Instant decidedAt = now();
        activities.recordHumanGateDecision(
                new MissionActivities
                        .GateDecisionActivityRequest(
                        gateDecisionEffectId(command),
                        MissionActivities.GateDecisionKind.APPROVE,
                        new ApprovalService.DecisionRequest(
                                command.tenantId(),
                                command.missionId(),
                                command.gateId(),
                                command.actorUserId(),
                                command.actorRoles(),
                                command.reason(),
                                decidedAt
                        ),
                        null
                )
        );

        workflowState = workflowState.approved(
                command,
                decidedAt
        );
        return workflowState;
    }

    @Override
    public MissionWorkflowState reject(
            MissionWorkflowCommand command
    ) {
        awaitInitialized();
        requireCommandType(
                command,
                MissionWorkflowCommand.CommandType.REJECT
        );
        workflowState.requireCommand(command);

        if (workflowState.alreadyProcessed(command)) {
            return workflowState;
        }
        requireOpenWorkflow();
        workflowState.requirePendingGate(command.gateId());

        Instant decidedAt = now();
        activities.recordHumanGateDecision(
                new MissionActivities
                        .GateDecisionActivityRequest(
                        gateDecisionEffectId(command),
                        MissionActivities.GateDecisionKind.REJECT,
                        new ApprovalService.DecisionRequest(
                                command.tenantId(),
                                command.missionId(),
                                command.gateId(),
                                command.actorUserId(),
                                command.actorRoles(),
                                command.reason(),
                                decidedAt
                        ),
                        null
                )
        );

        String failureReference = failureReference(
                "HUMAN_GATE_REJECTED"
        );
        MissionActivities.TerminalizationResult terminal =
                terminalizeDetached(
                        terminalFailureRequest(
                                "HUMAN_GATE_REJECTED",
                                command.reason(),
                                command.gateId(),
                                failureReference
                        )
                );

        requireTerminalStatus(
                terminal,
                MissionStatus.FAILED_TERMINAL
        );
        workflowState = workflowState.rejected(
                command,
                decidedAt,
                terminal.completedAt(),
                failureReference
        );
        return workflowState;
    }

    @Override
    public MissionWorkflowState cancel(
            MissionWorkflowCommand command
    ) {
        awaitInitialized();
        requireCommandType(
                command,
                MissionWorkflowCommand.CommandType.CANCEL
        );
        workflowState.requireCommand(command);

        if (workflowState.alreadyProcessed(command)) {
            return workflowState;
        }
        requireOpenWorkflow();

        if (workflowState.cancellationRequested()) {
            throw new IllegalStateException(
                    "MISSION_CANCELLATION_ALREADY_REQUESTED"
            );
        }

        workflowState =
                workflowState.cancellationRequested(
                        command.reason(),
                        now()
                );

        if (reactorScope != null) {
            reactorScope.cancel();
        }

        MissionActivities.TerminalizationResult terminal =
                terminalizeDetached(
                        cancellationRequest(
                                command.reason()
                        )
                );

        requireTerminalStatus(
                terminal,
                MissionStatus.CANCELLED
        );
        workflowState = workflowState.cancelled(
                command,
                terminal.completedAt()
        );
        return workflowState;
    }

    @Override
    public MissionWorkflowState state() {
        return workflowState;
    }

    private void runReactor() {
        if (workflowState.intent() == null) {
            workflowState.budgetCounters().requireLlmCall();

            workflowState = workflowState.withIntent(
                    activities.parseIntent(
                            new MissionActivities
                                    .IntentActivityRequest(
                                    intentEffectId(),
                                    input.tenantId(),
                                    input.missionId(),
                                    input.missionInputRef(),
                                    input.preparedArtifactsRef()
                            )
                    )
            );
        }

        while (!workflowState.terminal()
                && !workflowState.cancellationRequested()) {
            PlannedStep reviewedStep =
                    workflowState.lastExecutedStep();
            workflowState.budgetCounters().requireLlmCall();

            MissionAgent.TurnDecision turn =
                    activities.planOrReview(
                            new MissionActivities
                                    .PlanActivityRequest(
                                    planEffectId(),
                                    input.tenantId(),
                                    input.missionId(),
                                    input.missionInputRef(),
                                    workflowState.intent(),
                                    workflowState.currentPlan(),
                                    reviewedStep,
                                    workflowState
                                            .completedLogicalStepIds(),
                                    workflowState.observationRefs(),
                                    workflowState.intent()
                                            .allowedTools(),
                                    workflowState
                                            .remainingToolCalls(),
                                    workflowState
                                            .remainingLlmCalls(),
                                    workflowState
                                            .cancellationRequested()
                            )
                    );

            workflowState = workflowState.withPlanTurn(
                    turn.plan(),
                    turn.reason()
            );

            switch (turn.type()) {
                case COMPLETE -> {
                    synthesizeVerifyAndComplete();
                    return;
                }
                case FAIL -> {
                    failTerminal(
                            "MISSION_PLANNING_FAILED",
                            turn.reason(),
                            turn.nextStep() == null
                                    ? ""
                                    : turn.nextStep().stepId()
                    );
                    return;
                }
                case WAIT_FOR_APPROVAL -> {
                    PlannedStep step = requireNextStep(turn);
                    if (!waitForApproval(step, turn.reason())) {
                        return;
                    }
                }
                case EXECUTE_STEP -> {
                    PlannedStep step = requireNextStep(turn);

                    if (step.requiresHumanApproval()
                            && !isGateReviewed(step)) {
                        if (!waitForApproval(
                                step,
                                turn.reason()
                        )) {
                            return;
                        }
                    }

                    if (!executeOneHop(step)) {
                        return;
                    }
                    maybeContinueAsNew();
                }
            }
        }
    }

    private boolean executeOneHop(PlannedStep step) {
        if (workflowState.completedLogicalStepIds()
                .contains(step.stepId())) {
            failTerminal(
                    "DUPLICATE_LOGICAL_STEP",
                    "Planner selected an already completed step",
                    step.stepId()
            );
            return false;
        }
        if (!step.dependenciesSatisfied(
                workflowState.completedLogicalStepIds()
        )) {
            failTerminal(
                    "UNSATISFIED_STEP_DEPENDENCY",
                    "Planner selected a step before its dependencies",
                    step.stepId()
            );
            return false;
        }

        Set<String> allowedTools =
                workflowState.intent().allowedTools();
        if (!allowedTools.isEmpty()
                && !allowedTools.contains(step.capability())) {
            failTerminal(
                    "UNAUTHORIZED_TOOL",
                    "Planner selected an unauthorized capability",
                    step.stepId()
            );
            return false;
        }

        String effectId = hopEffectId(step);
        HopSpec hopSpec;
        try {
            hopSpec = buildHopSpec(step, effectId);
            workflowState.budgetCounters().requireHop(
                    true,
                    hopSpec.hydratedItems()
            );
        } catch (RuntimeException invalidStep) {
            failTerminal(
                    "INVALID_ACTIVITY_HOP",
                    safeMessage(invalidStep),
                    step.stepId()
            );
            return false;
        }

        MissionActivities.OneHopActivityResult result =
                activities.executeOneHop(
                        new MissionActivities
                                .OneHopActivityRequest(
                                effectId,
                                input.tenantId(),
                                input.missionId(),
                                input.missionInputRef(),
                                step.stepId(),
                                step.kind(),
                                hopSpec.documentSpec(),
                                hopSpec.internalRequest(),
                                now()
                        )
                );

        workflowState = workflowState.withObservation(
                step,
                result.observationRef(),
                true,
                hopSpec.hydratedItems()
        );

        publishHopEvent(step);
        return true;
    }

    private boolean waitForApproval(
            PlannedStep step,
            String planningReason
    ) {
        String gateId = gateId(step);

        if (workflowState.reviewedGateIds().contains(gateId)) {
            return true;
        }

        Map<String, Object> arguments = step.arguments();
        Instant createdAt = now();
        long timeoutSeconds = boundedLong(
                arguments,
                "approvalTimeoutSeconds",
                DEFAULT_GATE_TIMEOUT_SECONDS,
                1L,
                MAX_GATE_TIMEOUT_SECONDS
        );
        Instant expiresAt =
                createdAt.plusSeconds(timeoutSeconds);
        String reason = firstText(
                text(arguments, "approvalReason", ""),
                planningReason,
                step.objective()
        );
        PendingGate gate = new PendingGate(
                gateId,
                step.stepId(),
                firstText(
                        text(arguments, "approvalTitle", ""),
                        "Approve mission step"
                ),
                reason,
                stringSet(
                        arguments.get(
                                "requiredReviewerRoles"
                        )
                ),
                createdAt,
                expiresAt
        );

        workflowState = workflowState.waitingFor(gate);

        activities.requestHumanApproval(
                new MissionActivities.GateActivityRequest(
                        gateId,
                        new ApprovalService.OpenRequest(
                                gateId,
                                input.tenantId(),
                                input.missionId(),
                                gate.title(),
                                gate.reason(),
                                List.copyOf(
                                        gate.requiredReviewerRoles()
                                ),
                                Map.of(
                                        "planId",
                                        workflowState
                                                .currentPlan()
                                                .planId(),
                                        "stepId", step.stepId(),
                                        "stepKind",
                                        step.kind().name(),
                                        "objective",
                                        step.objective()
                                ),
                                createdAt,
                                expiresAt
                        )
                )
        );

        long waitMillis = Math.max(
                1L,
                expiresAt.toEpochMilli()
                        - Workflow.currentTimeMillis()
        );
        boolean decided = Workflow.await(
                Duration.ofMillis(waitMillis),
                () -> workflowState.pendingGate() == null
                        || workflowState.terminal()
                        || workflowState.cancellationRequested()
        );

        if (!decided
                && workflowState.pendingGate() != null
                && !workflowState.terminal()
                && !workflowState.cancellationRequested()) {
            Instant expiredAt = now();
            activities.recordHumanGateDecision(
                    new MissionActivities
                            .GateDecisionActivityRequest(
                            "gate-expire:" + gateId,
                            MissionActivities
                                    .GateDecisionKind.EXPIRE,
                            null,
                            new ApprovalService.ExpiryRequest(
                                    input.tenantId(),
                                    input.missionId(),
                                    gateId,
                                    expiredAt
                            )
                    )
            );
            failTerminal(
                    "HUMAN_GATE_EXPIRED",
                    "Human approval gate expired",
                    step.stepId()
            );
            return false;
        }

        return !workflowState.terminal()
                && !workflowState.cancellationRequested()
                && workflowState.reviewedGateIds()
                .contains(gateId);
    }

    private void synthesizeVerifyAndComplete() {
        MissionPlan finalPlan =
                workflowState.currentPlan();
        if (finalPlan == null) {
            failTerminal(
                    "MISSION_PLAN_REQUIRED",
                    "Reactor completed without a final plan",
                    ""
            );
            return;
        }

        workflowState.budgetCounters().requireLlmCall();
        CheckpointRef resultRef =
                activities.synthesizeAnswer(
                        new MissionActivities
                                .SynthesisActivityRequest(
                                synthesisEffectId(finalPlan),
                                input.tenantId(),
                                input.missionId(),
                                input.missionInputRef(),
                                workflowState.intent(),
                                finalPlan,
                                workflowState.observationRefs(),
                                workflowState.governanceDecisions(),
                                now()
                        )
                );
        workflowState = workflowState.withResult(resultRef);

        MissionActivities.VerificationActivityResult verification =
                activities.verifyGrounding(
                        new MissionActivities
                                .VerificationActivityRequest(
                                verificationEffectId(finalPlan),
                                input.tenantId(),
                                input.missionId(),
                                resultRef,
                                groundingSpec(finalPlan),
                                now()
                        )
                );
        workflowState = workflowState.withVerification(
                verification.verificationRef(),
                verification.decision()
        );

        if (verification.decision().decision()
                != GovernanceDecisionType.ALLOWED) {
            failTerminal(
                    "GROUNDING_VERIFICATION_FAILED",
                    verification.decision().reason(),
                    ""
            );
            return;
        }

        MissionActivities.TerminalizationResult terminal =
                activities.terminalize(
                        new MissionActivities
                                .TerminalizationRequest(
                                terminalEffectId("complete"),
                                input.tenantId(),
                                input.missionId(),
                                MissionActivities
                                        .TerminalizationKind.COMPLETE,
                                workflowState.resultRef(),
                                null,
                                ""
                        )
                );

        requireTerminalStatus(
                terminal,
                MissionStatus.COMPLETED
        );
        workflowState = workflowState.completed(
                terminal.completedAt()
        );
    }

    private void publishHopEvent(PlannedStep step) {
        String eventId = input.missionId()
                + ":event:hop:"
                + workflowState.activityHopCount();

        MissionPlan plan = workflowState.currentPlan();
        int totalSteps = plan == null
                ? workflowState.activityHopCount()
                : Math.max(1, plan.steps().size());
        double progress = Math.min(
                99.0d,
                100.0d
                        * workflowState
                        .completedLogicalStepIds()
                        .size()
                        / totalSteps
        );

        MissionProgressEvent event =
                new MissionProgressEvent(
                        input.missionId(),
                        MissionStatus.RUNNING,
                        "reactor",
                        "Durable mission reactor",
                        step.stepId(),
                        step.objective(),
                        StepStatus.SUCCEEDED,
                        null,
                        "Completed "
                                + step.kind().name()
                                + " Activity hop",
                        progress,
                        eventId,
                        now(),
                        Map.of(
                                "effectId",
                                hopEffectId(step),
                                "planId",
                                plan == null
                                        ? ""
                                        : plan.planId(),
                                "planRevision",
                                plan == null
                                        ? "0"
                                        : Integer.toString(
                                        plan.revision()
                                ),
                                "observationCheckpointId",
                                workflowState
                                        .observationRefs()
                                        .get(
                                                workflowState
                                                        .observationRefs()
                                                        .size()
                                                        - 1
                                        )
                                        .checkpointId()
                        )
                );

        activities.publishEvent(
                new MissionActivities.EventActivityRequest(
                        eventId,
                        input.tenantId(),
                        event
                )
        );
        workflowState = workflowState.eventPublished();
    }

    private HopSpec buildHopSpec(
            PlannedStep step,
            String effectId
    ) {
        Map<String, Object> arguments = step.arguments();
        int limit = hydrationLimit(arguments);

        if (step.kind() == StepKind.BUILD_DOCUMENT_EVIDENCE) {
            BuildSpec documentSpec = new BuildSpec(
                    effectId,
                    documentScope(arguments),
                    enumValue(
                            EvidenceGoalProto.class,
                            requiredText(arguments, "goal"),
                            "goal"
                    ),
                    text(arguments, "customGoal", ""),
                    enumList(
                            EvidenceNodeTypeProto.class,
                            arguments.get(
                                    "requestedNodeTypes"
                            ),
                            "requestedNodeTypes"
                    ),
                    enumList(
                            EvidenceRelationTypeProto.class,
                            arguments.get(
                                    "requestedRelationTypes"
                            ),
                            "requestedRelationTypes"
                    ),
                    text(
                            arguments,
                            "outputSchemaRef",
                            ""
                    ),
                    text(
                            arguments,
                            "outputSchemaVersion",
                            ""
                    ),
                    stringMap(arguments.get("options")),
                    firstText(
                            text(
                                    arguments,
                                    "retrievalHint",
                                    ""
                            ),
                            step.objective()
                    ),
                    stringListOrDefault(
                            arguments.get("topics"),
                            workflowState.intent().topics()
                    ),
                    stringListOrDefault(
                            arguments.get("entityNames"),
                            workflowState.intent()
                                    .targetEntities()
                    ),
                    stringList(arguments.get("keywords")),
                    stringMap(
                            arguments.get("metadataFilters")
                    ),
                    text(
                            arguments,
                            "debugTaskInstruction",
                            ""
                    ),
                    enumValue(
                            RetrievalModeProto.class,
                            requiredText(
                                    arguments,
                                    "retrievalMode"
                            ),
                            "retrievalMode"
                    ),
                    limit,
                    bool(
                            arguments,
                            "includeExcerpts",
                            true
                    ),
                    bool(
                            arguments,
                            "allowClaimCache",
                            true
                    ),
                    bool(
                            arguments,
                            "requireVerification",
                            workflowState.intent()
                                    .requiresVerification()
                    )
            );

            return new HopSpec(
                    documentSpec,
                    null,
                    limit
            );
        }

        if (!step.kind().isInternalOperation()) {
            throw new IllegalArgumentException(
                    "unsupported reactor hop: " + step.kind()
            );
        }

        InternalGraphNodeType rootNodeType =
                enumValue(
                        InternalGraphNodeType.class,
                        requiredText(
                                arguments,
                                "rootNodeType"
                        ),
                        "rootNodeType"
                );
        int depth = boundedInt(
                arguments,
                "depth",
                1,
                1,
                20
        );

        ResolveInternalContextStep.Request request =
                switch (step.kind()) {
                    case SEARCH_INTERNAL_ENTITIES ->
                            new ResolveInternalContextStep.Request(
                                    ResolveInternalContextStep
                                            .Operation
                                            .SEARCH_ENTITIES,
                                    new SearchSpec(
                                            effectId,
                                            firstText(
                                                    text(
                                                            arguments,
                                                            "query",
                                                            ""
                                                    ),
                                                    step.objective()
                                            ),
                                            enumList(
                                                    InternalGraphNodeType
                                                            .class,
                                                    arguments.get(
                                                            "nodeTypes"
                                                    ),
                                                    "nodeTypes"
                                            ),
                                            text(
                                                    arguments,
                                                    "rootEntityId",
                                                    ""
                                            ),
                                            rootNodeType,
                                            depth,
                                            limit,
                                            bool(
                                                    arguments,
                                                    "includeFuzzyMatches",
                                                    false
                                            ),
                                            stringMap(
                                                    arguments.get(
                                                            "filters"
                                                    )
                                            )
                                    ),
                                    null
                            );
                    case READ_INTERNAL_COMPANY_GRAPH ->
                            new ResolveInternalContextStep.Request(
                                    ResolveInternalContextStep
                                            .Operation
                                            .READ_COMPANY_GRAPH,
                                    null,
                                    new GraphSpec(
                                            effectId,
                                            requiredText(
                                                    arguments,
                                                    "rootEntityId"
                                            ),
                                            rootNodeType,
                                            depth,
                                            limit
                                    )
                            );
                    case READ_LEARNING_GRAPH ->
                            new ResolveInternalContextStep.Request(
                                    ResolveInternalContextStep
                                            .Operation
                                            .READ_LEARNING_GRAPH,
                                    null,
                                    new GraphSpec(
                                            effectId,
                                            requiredText(
                                                    arguments,
                                                    "rootEntityId"
                                            ),
                                            rootNodeType,
                                            depth,
                                            limit
                                    )
                            );
                    default -> throw new IllegalArgumentException(
                            "unsupported internal reactor hop"
                    );
                };

        return new HopSpec(null, request, limit);
    }

    private CheckGroundingAction.CheckSpec groundingSpec(
            MissionPlan plan
    ) {
        Map<String, Object> attributes = plan.attributes();
        boolean requireEvidence = bool(
                attributes,
                "requireEvidence",
                workflowState.intent().requiresRetrieval()
                        || workflowState.intent()
                        .requiresCitations()
        );
        boolean requireCitations = bool(
                attributes,
                "requireCitations",
                workflowState.intent().requiresCitations()
        );
        boolean requireVerification = bool(
                attributes,
                "requireVerification",
                workflowState.intent().requiresVerification()
        );

        GroundingPolicy.Requirements requirements =
                new GroundingPolicy.Requirements(
                        requireEvidence,
                        requireCitations,
                        requireVerification,
                        ratio(
                                attributes,
                                "minimumClaimCoverage",
                                requireEvidence ? 1.0d : 0.0d
                        ),
                        ratio(
                                attributes,
                                "minimumCitationCoverage",
                                requireCitations ? 1.0d : 0.0d
                        ),
                        ratio(
                                attributes,
                                "minimumConfidence",
                                0.0d
                        ),
                        boundedInt(
                                attributes,
                                "minimumEvidencePerMaterialClaim",
                                1,
                                1,
                                100
                        )
                );

        return new CheckGroundingAction.CheckSpec(
                input.missionId()
                        + ":grounding:"
                        + plan.planId()
                        + ":"
                        + plan.revision(),
                groundingClaims(
                        attributes.get("groundingClaims")
                ),
                List.of(),
                stringSet(
                        attributes.get("verifiedEvidenceIds")
                ),
                requirements
        );
    }

    private List<GroundingPolicy.ClaimEvidence>
    groundingClaims(Object value) {
        if (value == null) {
            return List.of();
        }
        if (!(value instanceof Collection<?> collection)) {
            throw new IllegalArgumentException(
                    "groundingClaims must be a collection"
            );
        }

        ArrayList<GroundingPolicy.ClaimEvidence> claims =
                new ArrayList<>();
        for (Object item : collection) {
            if (!(item instanceof Map<?, ?> raw)) {
                throw new IllegalArgumentException(
                        "grounding claim must be an object"
                );
            }

            Map<String, Object> claim = objectMap(raw);
            claims.add(
                    new GroundingPolicy.ClaimEvidence(
                            requiredText(claim, "claimId"),
                            stringList(
                                    claim.get("evidenceIds")
                            ),
                            ratio(
                                    claim,
                                    "confidence",
                                    1.0d
                            ),
                            bool(
                                    claim,
                                    "material",
                                    true
                            )
                    )
            );
        }
        return List.copyOf(claims);
    }

    private void maybeContinueAsNew() {
        int nextThreshold =
                (workflowState.continueAsNewCount() + 1)
                        * CONTINUE_AS_NEW_AFTER_HOPS;

        if (workflowState.activityHopCount() >= nextThreshold
                && workflowState.pendingGate() == null
                && Workflow.isEveryHandlerFinished()) {
            workflowState =
                    workflowState.continuedAsNew();
            Workflow.continueAsNew(input, workflowState);
        }
    }

    private void handleScopeCancellation(
            CanceledFailure cancelled
    ) {
        if (workflowState.terminal()) {
            return;
        }

        if (workflowState.cancellationRequested()) {
            Workflow.await(() -> workflowState.terminal());
            return;
        }

        workflowState =
                workflowState.cancellationRequested(
                        firstText(
                                cancelled.getMessage(),
                                "Temporal cancellation requested"
                        ),
                        now()
                );

        MissionActivities.TerminalizationResult terminal =
                terminalizeDetached(
                        cancellationRequest(
                                workflowState.cancellationReason()
                        )
                );

        requireTerminalStatus(
                terminal,
                MissionStatus.CANCELLED
        );
        workflowState =
                workflowState.cancelledExternally(
                        terminal.completedAt()
                );
    }

    private void failTerminal(
            String code,
            String message,
            String failedStepId
    ) {
        if (workflowState.terminal()) {
            return;
        }

        String reference = failureReference(code);
        MissionActivities.TerminalizationResult terminal =
                terminalizeDetached(
                        terminalFailureRequest(
                                code,
                                firstText(
                                        message,
                                        "Mission failed"
                                ),
                                failedStepId,
                                reference
                        )
                );

        requireTerminalStatus(
                terminal,
                MissionStatus.FAILED_TERMINAL
        );
        workflowState = workflowState.failed(
                terminal.completedAt(),
                reference,
                firstText(message, code)
        );
    }

    private MissionActivities.TerminalizationRequest
    terminalFailureRequest(
            String code,
            String message,
            String failedStepId,
            String reference
    ) {
        return new MissionActivities.TerminalizationRequest(
                terminalEffectId("fail:" + code),
                input.tenantId(),
                input.missionId(),
                MissionActivities
                        .TerminalizationKind.FAIL_TERMINAL,
                null,
                new MissionFailure(
                        code,
                        message,
                        MissionFailureReason.UNSPECIFIED,
                        false,
                        "reactor",
                        normalize(failedStepId),
                        "temporal-mission-workflow",
                        Map.of(
                                "errorReference", reference
                        )
                ),
                ""
        );
    }

    private MissionActivities.TerminalizationRequest
    cancellationRequest(String reason) {
        return new MissionActivities.TerminalizationRequest(
                terminalEffectId("cancel"),
                input.tenantId(),
                input.missionId(),
                MissionActivities.TerminalizationKind.CANCEL,
                null,
                null,
                normalize(reason)
        );
    }

    private MissionActivities.TerminalizationResult
    terminalizeDetached(
            MissionActivities.TerminalizationRequest request
    ) {
        TerminalHolder holder = new TerminalHolder();
        CancellationScope detached =
                Workflow.newDetachedCancellationScope(
                        () -> holder.result =
                                activities.terminalize(request)
                );
        detached.run();
        return Objects.requireNonNull(
                holder.result,
                "terminalization Activity returned null"
        );
    }

    private MissionWorkflowOutcome terminalOutcome() {
        if (!workflowState.terminal()) {
            throw new IllegalStateException(
                    "Workflow outcome requires terminal state"
            );
        }

        return new MissionWorkflowOutcome(
                input.missionId(),
                input.tenantId(),
                workflowState.status(),
                workflowState.status() == MissionStatus.COMPLETED
                        ? workflowState.resultRef()
                        : null,
                workflowState.status()
                        == MissionStatus.FAILED_TERMINAL
                        ? workflowState.errorReference()
                        : "",
                workflowState.startedAt(),
                workflowState.completedAt(),
                workflowState.cancelledAt()
        );
    }

    private void awaitInitialized() {
        Workflow.await(
                () -> input != null && workflowState != null
        );
    }

    private void requireOpenWorkflow() {
        if (workflowState.terminal()) {
            throw new IllegalStateException(
                    "MISSION_ALREADY_TERMINAL: "
                            + workflowState.status()
            );
        }
    }

    private static void requireCommandType(
            MissionWorkflowCommand command,
            MissionWorkflowCommand.CommandType expected
    ) {
        Objects.requireNonNull(
                command,
                "command must not be null"
        );
        if (command.type() != expected) {
            throw new IllegalArgumentException(
                    "Workflow Update command type mismatch"
            );
        }

        String temporalUpdateId = Workflow
                .getCurrentUpdateInfo()
                .orElseThrow(() -> new IllegalStateException(
                        "Workflow Update context is unavailable"
                ))
                .getUpdateId();

        if (!command.updateId().equals(temporalUpdateId)) {
            throw new IllegalArgumentException(
                    "Workflow command updateId does not match "
                            + "the Temporal Update ID"
            );
        }
    }

    private static void requireTerminalStatus(
            MissionActivities.TerminalizationResult result,
            MissionStatus expected
    ) {
        Objects.requireNonNull(
                result,
                "terminalization result must not be null"
        );
        if (result.status() != expected) {
            throw new IllegalStateException(
                    "terminalization returned "
                            + result.status()
                            + " instead of "
                            + expected
            );
        }
    }

    private static PlannedStep requireNextStep(
            MissionAgent.TurnDecision turn
    ) {
        return Objects.requireNonNull(
                turn.nextStep(),
                "planner decision requires nextStep"
        );
    }

    private boolean isGateReviewed(PlannedStep step) {
        return workflowState.reviewedGateIds().contains(
                gateId(step)
        );
    }

    private String intentEffectId() {
        return input.missionId()
                + ":intent:"
                + input.frozenVersionRef();
    }

    private String planEffectId() {
        return input.missionId()
                + ":plan:"
                + (workflowState.reactorIteration() + 1);
    }

    private String hopEffectId(PlannedStep step) {
        return input.missionId()
                + ":hop:"
                + step.stepId();
    }

    private String gateId(PlannedStep step) {
        MissionPlan plan = Objects.requireNonNull(
                workflowState.currentPlan(),
                "gate requires currentPlan"
        );
        return input.missionId()
                + ":gate:"
                + plan.planId()
                + ":"
                + step.stepId();
    }

    private String synthesisEffectId(MissionPlan plan) {
        return input.missionId()
                + ":synthesis:"
                + plan.planId()
                + ":"
                + plan.revision();
    }

    private String verificationEffectId(MissionPlan plan) {
        return input.missionId()
                + ":verification:"
                + plan.planId()
                + ":"
                + plan.revision();
    }

    private String terminalEffectId(String kind) {
        return input.missionId() + ":terminal:" + kind;
    }

    private String failureReference(String code) {
        return "mission-failure:"
                + input.tenantId()
                + ":"
                + input.missionId()
                + ":"
                + code;
    }

    private static String gateDecisionEffectId(
            MissionWorkflowCommand command
    ) {
        return command.missionId()
                + ":gate-decision:"
                + command.updateId();
    }

    private String currentStepId() {
        PlannedStep step = workflowState.lastExecutedStep();
        return step == null ? "" : step.stepId();
    }

    private int hydrationLimit(Map<String, Object> arguments) {
        int remaining =
                workflowState.remainingItemsToHydrate();
        if (remaining < 1) {
            throw new IllegalStateException(
                    "MISSION_BUDGET_EXHAUSTED: itemsToHydrate"
            );
        }

        int requested = boundedInt(
                arguments,
                "limit",
                Math.min(DEFAULT_HYDRATION_LIMIT, remaining),
                1,
                Integer.MAX_VALUE
        );
        return Math.min(requested, remaining);
    }

    private static void requireContinuation(
            MissionWorkflowInput input,
            MissionWorkflowState state
    ) {
        Objects.requireNonNull(
                state,
                "continuedState must not be null"
        );
        if (!input.missionId().equals(state.missionId())
                || !input.tenantId().equals(state.tenantId())) {
            throw new IllegalArgumentException(
                    "continued state belongs to another mission"
            );
        }

        MissionWorkflowState.BudgetCounters expected =
                MissionWorkflowState.BudgetCounters.from(
                        input.budget()
                );
        requireSameLimit(
                expected.llmCalls(),
                state.budgetCounters().llmCalls(),
                "llmCalls"
        );
        requireSameLimit(
                expected.toolCalls(),
                state.budgetCounters().toolCalls(),
                "toolCalls"
        );
        requireSameLimit(
                expected.retrievalQueries(),
                state.budgetCounters().retrievalQueries(),
                "retrievalQueries"
        );
        requireSameLimit(
                expected.itemsToHydrate(),
                state.budgetCounters().itemsToHydrate(),
                "itemsToHydrate"
        );
        requireSameLimit(
                expected.inputTokens(),
                state.budgetCounters().inputTokens(),
                "inputTokens"
        );
        requireSameLimit(
                expected.outputTokens(),
                state.budgetCounters().outputTokens(),
                "outputTokens"
        );
        requireSameLimit(
                expected.costMicros(),
                state.budgetCounters().costMicros(),
                "costMicros"
        );
    }

    private static void requireSameLimit(
            MissionWorkflowState.Counter expected,
            MissionWorkflowState.Counter actual,
            String dimension
    ) {
        if (expected.limit() != actual.limit()) {
            throw new IllegalArgumentException(
                    "continued state changed budget dimension "
                            + dimension
            );
        }
    }

    private static <E extends Enum<E>> E enumValue(
            Class<E> enumType,
            String value,
            String field
    ) {
        try {
            return Enum.valueOf(
                    enumType,
                    value.trim().toUpperCase(
                            java.util.Locale.ROOT
                    )
            );
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    field + " has invalid value " + value,
                    exception
            );
        }
    }

    private static <E extends Enum<E>> List<E> enumList(
            Class<E> enumType,
            Object value,
            String field
    ) {
        List<String> names = stringList(value);
        ArrayList<E> result = new ArrayList<>();
        for (String name : names) {
            result.add(enumValue(enumType, name, field));
        }
        return List.copyOf(result);
    }

    private static int boundedInt(
            Map<String, Object> values,
            String key,
            int fallback,
            int minimum,
            int maximum
    ) {
        Object value = values.get(key);
        int resolved;

        if (value == null) {
            resolved = fallback;
        } else if (value instanceof Number number) {
            resolved = number.intValue();
        } else {
            try {
                resolved = Integer.parseInt(
                        value.toString().trim()
                );
            } catch (NumberFormatException exception) {
                throw new IllegalArgumentException(
                        key + " must be an integer",
                        exception
                );
            }
        }

        if (resolved < minimum || resolved > maximum) {
            throw new IllegalArgumentException(
                    key + " is outside its allowed range"
            );
        }
        return resolved;
    }

    private static long boundedLong(
            Map<String, Object> values,
            String key,
            long fallback,
            long minimum,
            long maximum
    ) {
        Object value = values.get(key);
        long resolved;

        if (value == null) {
            resolved = fallback;
        } else if (value instanceof Number number) {
            resolved = number.longValue();
        } else {
            try {
                resolved = Long.parseLong(
                        value.toString().trim()
                );
            } catch (NumberFormatException exception) {
                throw new IllegalArgumentException(
                        key + " must be an integer",
                        exception
                );
            }
        }

        if (resolved < minimum || resolved > maximum) {
            throw new IllegalArgumentException(
                    key + " is outside its allowed range"
            );
        }
        return resolved;
    }

    private static double ratio(
            Map<String, Object> values,
            String key,
            double fallback
    ) {
        Object value = values.get(key);
        if (value == null) {
            return fallback;
        }

        double resolved;
        if (value instanceof Number number) {
            resolved = number.doubleValue();
        } else {
            try {
                resolved = Double.parseDouble(
                        value.toString().trim()
                );
            } catch (NumberFormatException exception) {
                throw new IllegalArgumentException(
                        key + " must be a ratio",
                        exception
                );
            }
        }

        if (!Double.isFinite(resolved)
                || resolved < 0.0d
                || resolved > 1.0d) {
            throw new IllegalArgumentException(
                    key + " must be between 0 and 1"
            );
        }
        return resolved;
    }

    private static boolean bool(
            Map<String, Object> values,
            String key,
            boolean fallback
    ) {
        Object value = values.get(key);
        if (value == null) {
            return fallback;
        }
        if (value instanceof Boolean bool) {
            return bool;
        }
        if ("true".equalsIgnoreCase(value.toString())) {
            return true;
        }
        if ("false".equalsIgnoreCase(value.toString())) {
            return false;
        }
        throw new IllegalArgumentException(
                key + " must be boolean"
        );
    }

    private static String requiredText(
            Map<String, Object> values,
            String key
    ) {
        String result = text(values, key, "");
        if (result.isEmpty()) {
            throw new IllegalArgumentException(
                    key + " must not be blank"
            );
        }
        return result;
    }

    private static String text(
            Map<String, Object> values,
            String key,
            String fallback
    ) {
        Object value = values.get(key);
        return value == null
                ? normalize(fallback)
                : normalize(value.toString());
    }

    private static List<String> stringListOrDefault(
            Object value,
            Collection<String> fallback
    ) {
        return value == null
                ? sortedStrings(fallback)
                : stringList(value);
    }

    private static List<String> stringList(Object value) {
        if (value == null) {
            return List.of();
        }

        Collection<?> source;
        if (value instanceof Collection<?> collection) {
            source = collection;
        } else {
            source = List.of(value);
        }

        TreeSet<String> normalized = new TreeSet<>();
        for (Object item : source) {
            if (item != null) {
                String text = normalize(item.toString());
                if (!text.isEmpty()) {
                    normalized.add(text);
                }
            }
        }
        return List.copyOf(normalized);
    }

    private static List<String> sortedStrings(
            Collection<String> values
    ) {
        return List.copyOf(stringSet(values));
    }

    private static Set<String> stringSet(Object value) {
        return Collections.unmodifiableSet(
                new TreeSet<>(stringList(value))
        );
    }

    private static Map<String, String> stringMap(
            Object value
    ) {
        if (value == null) {
            return Map.of();
        }
        if (!(value instanceof Map<?, ?> source)) {
            throw new IllegalArgumentException(
                    "expected a string map"
            );
        }

        java.util.TreeMap<String, String> result =
                new java.util.TreeMap<>();
        for (Map.Entry<?, ?> entry : source.entrySet()) {
            if (entry.getKey() == null) {
                throw new IllegalArgumentException(
                        "map key must not be null"
                );
            }
            String key = normalize(entry.getKey().toString());
            if (key.isEmpty()) {
                throw new IllegalArgumentException(
                        "map key must not be blank"
                );
            }
            result.put(
                    key,
                    entry.getValue() == null
                            ? ""
                            : entry.getValue().toString()
            );
        }
        return Collections.unmodifiableMap(result);
    }

    private static Map<String, Object> objectMap(
            Map<?, ?> source
    ) {
        LinkedHashMap<String, Object> result =
                new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : source.entrySet()) {
            if (entry.getKey() == null) {
                throw new IllegalArgumentException(
                        "object key must not be null"
                );
            }
            String key = normalize(entry.getKey().toString());
            if (key.isEmpty()) {
                throw new IllegalArgumentException(
                        "object key must not be blank"
                );
            }
            result.put(key, entry.getValue());
        }
        return Collections.unmodifiableMap(result);
    }

    private static String firstText(String... values) {
        for (String value : values) {
            String normalized = normalize(value);
            if (!normalized.isEmpty()) {
                return normalized;
            }
        }
        return "";
    }

    private static String safeMessage(Throwable failure) {
        String message = normalize(failure.getMessage());
        if (message.isEmpty()) {
            message = failure.getClass().getSimpleName();
        }
        return message.length() <= 4_000
                ? message
                : message.substring(0, 4_000);
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private static Instant now() {
        return Instant.ofEpochMilli(
                Workflow.currentTimeMillis()
        );
    }

    private record HopSpec(
            BuildSpec documentSpec,
            ResolveInternalContextStep.Request internalRequest,
            int hydratedItems
    ) {
    }

    private static final class TerminalHolder {

        private MissionActivities.TerminalizationResult result;
    }

    private static Scope documentScope(
            Map<String, Object> arguments
    ) {
        Object value = arguments.get("scope");

        if (!(value instanceof Map<?, ?> rawScope)) {
            throw new IllegalArgumentException(
                    "scope must be an object"
            );
        }

        Map<String, Object> scope = objectMap(rawScope);

        return new Scope(
                stringList(scope.get("documentIds")),
                stringList(scope.get("fileNames")),
                stringList(scope.get("collectionIds")),
                stringList(scope.get("tags")),
                stringMap(scope.get("metadataFilters"))
        );
    }
}