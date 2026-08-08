package com.sparrowx.agentic.temporal.model;

import com.sparrowx.agentic.governance.model.GovernanceDecision;
import com.sparrowx.agentic.mission.model.MissionBudget;
import com.sparrowx.agentic.mission.model.MissionStatus;
import com.sparrowx.agentic.planning.MissionIntent;
import com.sparrowx.agentic.planning.MissionPlan;
import com.sparrowx.agentic.planning.PlannedStep;
import com.sparrowx.agentic.runtime.checkpoint.CheckpointRef;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Complete deterministic reactor state carried across Continue-As-New.
 */
public record MissionWorkflowState(
        String missionId,
        String tenantId,
        MissionStatus status,
        MissionIntent intent,
        MissionPlan currentPlan,
        PlannedStep lastExecutedStep,
        Set<String> completedLogicalStepIds,
        List<CheckpointRef> observationRefs,
        BudgetCounters budgetCounters,
        PendingGate pendingGate,
        Set<String> reviewedGateIds,
        boolean cancellationRequested,
        String cancellationReason,
        Instant cancellationRequestedAt,
        Map<String, String> processedUpdateFingerprints,
        Instant approvedAt,
        Instant rejectedAt,
        Instant cancelledAt,
        CheckpointRef resultRef,
        CheckpointRef verificationRef,
        List<GovernanceDecision> governanceDecisions,
        String errorReference,
        int reactorIteration,
        int activityHopCount,
        int continueAsNewCount,
        int publishedEventCount,
        Instant startedAt,
        Instant completedAt,
        String lastReason
) {

    private static final int MAX_HISTORY_COLLECTION_ITEMS = 10_000;
    private static final int MAX_HISTORY_DEPTH = 12;
    private static final int MAX_HISTORY_STRING_LENGTH = 32_000;
    private static final int MAX_REFERENCE_METADATA_ENTRIES = 128;
    private static final int MAX_REFERENCE_METADATA_KEY_LENGTH = 256;
    private static final int MAX_REFERENCE_METADATA_VALUE_LENGTH = 4_096;

    public MissionWorkflowState {
        missionId = requireText(missionId, "missionId");
        tenantId = requireText(tenantId, "tenantId");
        status = Objects.requireNonNull(
                status,
                "status must not be null"
        );
        completedLogicalStepIds = immutableTextSet(
                completedLogicalStepIds,
                "completedLogicalStepIds"
        );
        observationRefs = immutableObservationRefs(
                observationRefs,
                tenantId,
                missionId
        );
        budgetCounters = Objects.requireNonNull(
                budgetCounters,
                "budgetCounters must not be null"
        );
        reviewedGateIds = immutableTextSet(
                reviewedGateIds,
                "reviewedGateIds"
        );
        cancellationReason = normalize(cancellationReason);
        processedUpdateFingerprints =
                immutableProcessedUpdates(
                        processedUpdateFingerprints
                );
        governanceDecisions = governanceDecisions == null
                ? List.of()
                : List.copyOf(governanceDecisions);
        errorReference = normalize(errorReference);
        startedAt = Objects.requireNonNull(
                startedAt,
                "startedAt must not be null"
        );
        lastReason = normalize(lastReason);

        if (intent != null) {
            if (!missionId.equals(intent.missionId())) {
                throw new IllegalArgumentException(
                        "intent belongs to another mission"
                );
            }
            requireHistorySafe(
                    intent.attributes(),
                    "intent.attributes"
            );
        }

        if (currentPlan != null) {
            if (!missionId.equals(currentPlan.missionId())) {
                throw new IllegalArgumentException(
                        "currentPlan belongs to another mission"
                );
            }
            requirePlanHistorySafe(currentPlan);
        }

        if (lastExecutedStep != null) {
            requireStepHistorySafe(lastExecutedStep);
        }

        if (pendingGate != null
                && status != MissionStatus.WAITING_APPROVAL) {
            throw new IllegalArgumentException(
                    "pendingGate requires WAITING_APPROVAL status"
            );
        }
        if (status == MissionStatus.WAITING_APPROVAL
                && pendingGate == null) {
            throw new IllegalArgumentException(
                    "WAITING_APPROVAL requires pendingGate"
            );
        }

        if (cancellationRequested
                && cancellationRequestedAt == null) {
            throw new IllegalArgumentException(
                    "cancellationRequested requires a timestamp"
            );
        }
        if (!cancellationRequested
                && (cancellationRequestedAt != null
                || !cancellationReason.isEmpty())) {
            throw new IllegalArgumentException(
                    "cancellation details require cancellationRequested"
            );
        }

        requireOptionalRef(
                resultRef,
                tenantId,
                missionId,
                CheckpointRef.CheckpointType.MISSION_RESULT,
                "resultRef"
        );
        requireOptionalRef(
                verificationRef,
                tenantId,
                missionId,
                CheckpointRef.CheckpointType.OBSERVATION,
                "verificationRef"
        );

        for (GovernanceDecision decision : governanceDecisions) {
            Objects.requireNonNull(
                    decision,
                    "governanceDecisions must not contain null"
            );
            requireHistorySafe(
                    decision.attributes(),
                    "governanceDecision.attributes"
            );
        }

        if (reactorIteration < 0
                || activityHopCount < 0
                || continueAsNewCount < 0
                || publishedEventCount < 0) {
            throw new IllegalArgumentException(
                    "Workflow counters must not be negative"
            );
        }
        if (activityHopCount > reactorIteration) {
            throw new IllegalArgumentException(
                    "activityHopCount exceeds reactorIteration"
            );
        }

        if (isTerminalStatus(status)) {
            if (completedAt == null) {
                throw new IllegalArgumentException(
                        "terminal state requires completedAt"
                );
            }
            if (pendingGate != null) {
                throw new IllegalArgumentException(
                        "terminal state must not retain pendingGate"
                );
            }
        } else if (completedAt != null) {
            throw new IllegalArgumentException(
                    "non-terminal state must not have completedAt"
            );
        }

        if (completedAt != null && completedAt.isBefore(startedAt)) {
            throw new IllegalArgumentException(
                    "completedAt must not precede startedAt"
            );
        }

        if (status == MissionStatus.COMPLETED
                && resultRef == null) {
            throw new IllegalArgumentException(
                    "COMPLETED state requires resultRef"
            );
        }
        if (status == MissionStatus.FAILED_TERMINAL
                && errorReference.isEmpty()) {
            throw new IllegalArgumentException(
                    "FAILED_TERMINAL requires errorReference"
            );
        }
        if (status != MissionStatus.FAILED_TERMINAL
                && !errorReference.isEmpty()) {
            throw new IllegalArgumentException(
                    "errorReference is reserved for terminal failure"
            );
        }
        if (status == MissionStatus.CANCELLED) {
            if (!cancellationRequested
                    || cancelledAt == null) {
                throw new IllegalArgumentException(
                        "CANCELLED state requires cancellation details"
                );
            }
        } else if (cancelledAt != null) {
            throw new IllegalArgumentException(
                    "cancelledAt requires CANCELLED status"
            );
        }
    }

    public static MissionWorkflowState initial(
            MissionWorkflowInput input,
            Instant startedAt
    ) {
        Objects.requireNonNull(input, "input must not be null");

        return new MissionWorkflowState(
                input.missionId(),
                input.tenantId(),
                MissionStatus.RUNNING,
                null,
                null,
                null,
                Set.of(),
                List.of(),
                BudgetCounters.from(input.budget()),
                null,
                Set.of(),
                false,
                "",
                null,
                Map.of(),
                null,
                null,
                null,
                null,
                null,
                List.of(),
                "",
                0,
                0,
                0,
                0,
                Objects.requireNonNull(
                        startedAt,
                        "startedAt must not be null"
                ),
                null,
                ""
        );
    }

    public boolean terminal() {
        return isTerminalStatus(status);
    }

    public int remainingLlmCalls() {
        return Math.toIntExact(
                budgetCounters.llmCalls().remaining()
        );
    }

    public int remainingToolCalls() {
        return Math.toIntExact(
                budgetCounters.toolCalls().remaining()
        );
    }

    public int remainingRetrievalQueries() {
        return Math.toIntExact(
                budgetCounters.retrievalQueries().remaining()
        );
    }

    public int remainingItemsToHydrate() {
        return Math.toIntExact(
                budgetCounters.itemsToHydrate().remaining()
        );
    }

    public MissionWorkflowState withIntent(
            MissionIntent parsedIntent
    ) {
        Builder builder = new Builder(this);
        builder.intent = Objects.requireNonNull(
                parsedIntent,
                "parsedIntent must not be null"
        );
        builder.budgetCounters =
                budgetCounters.consumeLlmCall();
        builder.lastReason = "Mission intent classified";
        return builder.build();
    }

    public MissionWorkflowState withPlanTurn(
            MissionPlan plan,
            String reason
    ) {
        Builder builder = new Builder(this);
        builder.currentPlan = plan == null ? currentPlan : plan;
        builder.lastExecutedStep = null;
        builder.budgetCounters =
                budgetCounters.consumeLlmCall();
        builder.reactorIteration = reactorIteration + 1;
        builder.lastReason = normalize(reason);
        return builder.build();
    }

    public MissionWorkflowState withObservation(
            PlannedStep step,
            CheckpointRef observationRef,
            boolean retrieval,
            int hydratedItems
    ) {
        Objects.requireNonNull(step, "step must not be null");
        requireOptionalRef(
                Objects.requireNonNull(
                        observationRef,
                        "observationRef must not be null"
                ),
                tenantId,
                missionId,
                CheckpointRef.CheckpointType.OBSERVATION,
                "observationRef"
        );

        TreeSet<String> completed =
                new TreeSet<>(completedLogicalStepIds);
        if (!completed.add(step.stepId())) {
            throw new IllegalStateException(
                    "logical step already completed: "
                            + step.stepId()
            );
        }

        ArrayList<CheckpointRef> references =
                new ArrayList<>(observationRefs);
        references.add(observationRef);

        Builder builder = new Builder(this);
        builder.lastExecutedStep = step;
        builder.completedLogicalStepIds = completed;
        builder.observationRefs = references;
        builder.budgetCounters = budgetCounters.consumeHop(
                retrieval,
                hydratedItems
        );
        builder.activityHopCount = activityHopCount + 1;
        builder.lastReason = "Completed one Activity hop";
        return builder.build();
    }

    public MissionWorkflowState waitingFor(
            PendingGate gate
    ) {
        Builder builder = new Builder(this);
        builder.status = MissionStatus.WAITING_APPROVAL;
        builder.pendingGate = Objects.requireNonNull(
                gate,
                "gate must not be null"
        );
        builder.lastReason = gate.reason();
        return builder.build();
    }

    public MissionWorkflowState approved(
            MissionWorkflowCommand command,
            Instant decidedAt
    ) {
        requireCommand(command);
        requirePendingGate(command.gateId());

        TreeSet<String> reviewed =
                new TreeSet<>(reviewedGateIds);
        reviewed.add(command.gateId());

        Builder builder = new Builder(this);
        builder.status = MissionStatus.RUNNING;
        builder.pendingGate = null;
        builder.reviewedGateIds = reviewed;
        builder.approvedAt = Objects.requireNonNull(
                decidedAt,
                "decidedAt must not be null"
        );
        builder.processedUpdateFingerprints =
                withProcessedUpdate(command);
        builder.lastReason = command.reason().isBlank()
                ? "Human gate approved"
                : command.reason();
        return builder.build();
    }

    public MissionWorkflowState cancellationRequested(
            String reason,
            Instant requestedAt
    ) {
        Builder builder = new Builder(this);
        builder.cancellationRequested = true;
        builder.cancellationReason = normalize(reason);
        builder.cancellationRequestedAt =
                Objects.requireNonNull(
                        requestedAt,
                        "requestedAt must not be null"
                );
        builder.lastReason = builder.cancellationReason;
        return builder.build();
    }

    public MissionWorkflowState rejected(
            MissionWorkflowCommand command,
            Instant rejectedAt,
            Instant terminalAt,
            String failureReference
    ) {
        requireCommand(command);

        Builder builder = new Builder(this);
        builder.status = MissionStatus.FAILED_TERMINAL;
        builder.pendingGate = null;
        builder.rejectedAt = Objects.requireNonNull(
                rejectedAt,
                "rejectedAt must not be null"
        );
        builder.errorReference = requireText(
                failureReference,
                "failureReference"
        );
        builder.completedAt = Objects.requireNonNull(
                terminalAt,
                "terminalAt must not be null"
        );
        builder.processedUpdateFingerprints =
                withProcessedUpdate(command);
        builder.lastReason = command.reason();
        return builder.build();
    }

    public MissionWorkflowState cancelled(
            MissionWorkflowCommand command,
            Instant terminalAt
    ) {
        requireCommand(command);

        Builder builder = new Builder(this);
        builder.status = MissionStatus.CANCELLED;
        builder.pendingGate = null;
        builder.cancelledAt = Objects.requireNonNull(
                terminalAt,
                "terminalAt must not be null"
        );
        builder.completedAt = terminalAt;
        builder.processedUpdateFingerprints =
                withProcessedUpdate(command);
        builder.lastReason = cancellationReason;
        return builder.build();
    }

    public MissionWorkflowState cancelledExternally(
            Instant terminalAt
    ) {
        Builder builder = new Builder(this);
        builder.status = MissionStatus.CANCELLED;
        builder.pendingGate = null;
        builder.cancelledAt = Objects.requireNonNull(
                terminalAt,
                "terminalAt must not be null"
        );
        builder.completedAt = terminalAt;
        builder.lastReason = cancellationReason;
        return builder.build();
    }

    public MissionWorkflowState withResult(
            CheckpointRef missionResultRef
    ) {
        requireOptionalRef(
                Objects.requireNonNull(
                        missionResultRef,
                        "missionResultRef must not be null"
                ),
                tenantId,
                missionId,
                CheckpointRef.CheckpointType.MISSION_RESULT,
                "missionResultRef"
        );

        Builder builder = new Builder(this);
        builder.resultRef = missionResultRef;
        builder.budgetCounters =
                budgetCounters.consumeLlmCall();
        builder.lastReason = "Mission answer synthesized";
        return builder.build();
    }

    public MissionWorkflowState withVerification(
            CheckpointRef groundingVerificationRef,
            GovernanceDecision decision
    ) {
        requireOptionalRef(
                Objects.requireNonNull(
                        groundingVerificationRef,
                        "groundingVerificationRef must not be null"
                ),
                tenantId,
                missionId,
                CheckpointRef.CheckpointType.OBSERVATION,
                "groundingVerificationRef"
        );

        ArrayList<GovernanceDecision> decisions =
                new ArrayList<>(governanceDecisions);
        decisions.add(
                Objects.requireNonNull(
                        decision,
                        "decision must not be null"
                )
        );

        Builder builder = new Builder(this);
        builder.verificationRef =
                groundingVerificationRef;
        builder.governanceDecisions = decisions;
        builder.lastReason = decision.reason();
        return builder.build();
    }

    public MissionWorkflowState completed(
            Instant terminalAt
    ) {
        if (resultRef == null) {
            throw new IllegalStateException(
                    "completion requires resultRef"
            );
        }

        Builder builder = new Builder(this);
        builder.status = MissionStatus.COMPLETED;
        builder.pendingGate = null;
        builder.completedAt = Objects.requireNonNull(
                terminalAt,
                "terminalAt must not be null"
        );
        builder.lastReason = "Mission completed";
        return builder.build();
    }

    public MissionWorkflowState failed(
            Instant terminalAt,
            String failureReference,
            String reason
    ) {
        Builder builder = new Builder(this);
        builder.status = MissionStatus.FAILED_TERMINAL;
        builder.pendingGate = null;
        builder.completedAt = Objects.requireNonNull(
                terminalAt,
                "terminalAt must not be null"
        );
        builder.errorReference = requireText(
                failureReference,
                "failureReference"
        );
        builder.lastReason = normalize(reason);
        return builder.build();
    }

    public MissionWorkflowState eventPublished() {
        Builder builder = new Builder(this);
        builder.publishedEventCount =
                publishedEventCount + 1;
        return builder.build();
    }

    public MissionWorkflowState continuedAsNew() {
        Builder builder = new Builder(this);
        builder.continueAsNewCount =
                continueAsNewCount + 1;
        return builder.build();
    }

    public void requireCommand(
            MissionWorkflowCommand command
    ) {
        Objects.requireNonNull(
                command,
                "command must not be null"
        );

        if (!tenantId.equals(command.tenantId())
                || !missionId.equals(command.missionId())) {
            throw new IllegalArgumentException(
                    "Workflow Update has the wrong mission scope"
            );
        }

        String processed = processedUpdateFingerprints.get(
                command.updateId()
        );
        if (processed != null
                && !processed.equals(command.fingerprint())) {
            throw new IllegalStateException(
                    "WORKFLOW_UPDATE_ID_CONFLICT: "
                            + command.updateId()
            );
        }
    }

    public boolean alreadyProcessed(
            MissionWorkflowCommand command
    ) {
        requireCommand(command);
        return processedUpdateFingerprints.containsKey(
                command.updateId()
        );
    }

    public void requirePendingGate(String gateId) {
        if (pendingGate == null
                || !pendingGate.gateId().equals(gateId)) {
            throw new IllegalStateException(
                    "WORKFLOW_GATE_NOT_PENDING: " + gateId
            );
        }
    }

    private Map<String, String> withProcessedUpdate(
            MissionWorkflowCommand command
    ) {
        TreeMap<String, String> processed =
                new TreeMap<>(processedUpdateFingerprints);
        String previous = processed.put(
                command.updateId(),
                command.fingerprint()
        );

        if (previous != null
                && !previous.equals(command.fingerprint())) {
            throw new IllegalStateException(
                    "WORKFLOW_UPDATE_ID_CONFLICT: "
                            + command.updateId()
            );
        }

        return processed;
    }

    private static boolean isTerminalStatus(
            MissionStatus value
    ) {
        return value == MissionStatus.COMPLETED
                || value == MissionStatus.FAILED_TERMINAL
                || value == MissionStatus.CANCELLED;
    }

    private static Set<String> immutableTextSet(
            Collection<String> values,
            String field
    ) {
        TreeSet<String> copied = new TreeSet<>();

        if (values != null) {
            for (String value : values) {
                copied.add(requireText(value, field));
            }
        }

        return Collections.unmodifiableSet(copied);
    }

    private static List<CheckpointRef> immutableObservationRefs(
            List<CheckpointRef> values,
            String tenantId,
            String missionId
    ) {
        List<CheckpointRef> copied = values == null
                ? List.of()
                : List.copyOf(values);

        for (CheckpointRef reference : copied) {
            requireOptionalRef(
                    reference,
                    tenantId,
                    missionId,
                    CheckpointRef.CheckpointType.OBSERVATION,
                    "observationRef"
            );
        }

        return copied;
    }

    private static Map<String, String>
    immutableProcessedUpdates(Map<String, String> values) {
        if (values == null || values.isEmpty()) {
            return Map.of();
        }

        TreeMap<String, String> copied = new TreeMap<>();
        for (Map.Entry<String, String> entry
                : values.entrySet()) {
            copied.put(
                    requireText(
                            entry.getKey(),
                            "processed update id"
                    ),
                    requireText(
                            entry.getValue(),
                            "processed update fingerprint"
                    )
            );
        }
        return Collections.unmodifiableMap(copied);
    }

    private static void requireOptionalRef(
            CheckpointRef reference,
            String tenantId,
            String missionId,
            CheckpointRef.CheckpointType type,
            String field
    ) {
        if (reference == null) {
            return;
        }

        if (!tenantId.equals(reference.tenantId())
                || !missionId.equals(reference.missionId())
                || type != reference.checkpointType()) {
            throw new IllegalArgumentException(
                    field + " has the wrong mission scope or type"
            );
        }
        requireText(
                reference.checkpointId(),
                field + ".checkpointId"
        );

        if (reference.metadata().size()
                > MAX_REFERENCE_METADATA_ENTRIES) {
            throw new IllegalArgumentException(
                    field + " metadata is too large"
            );
        }
        reference.metadata().forEach((key, value) -> {
            if (key == null
                    || key.isBlank()
                    || key.length()
                    > MAX_REFERENCE_METADATA_KEY_LENGTH
                    || value == null
                    || value.length()
                    > MAX_REFERENCE_METADATA_VALUE_LENGTH) {
                throw new IllegalArgumentException(
                        field + " metadata is not history-safe"
                );
            }
        });
    }

    private static void requirePlanHistorySafe(
            MissionPlan plan
    ) {
        requireHistorySafe(
                plan.attributes(),
                "currentPlan.attributes"
        );
        for (PlannedStep step : plan.steps()) {
            requireStepHistorySafe(step);
        }
    }

    private static void requireStepHistorySafe(
            PlannedStep step
    ) {
        Objects.requireNonNull(
                step,
                "plan steps must not contain null"
        );
        requireHistorySafe(
                step.arguments(),
                "plannedStep.arguments"
        );
        requireHistorySafe(
                step.attributes(),
                "plannedStep.attributes"
        );
    }

    private static void requireHistorySafe(
            Object value,
            String field
    ) {
        int items = historyItemCount(value, 0, field);
        if (items > MAX_HISTORY_COLLECTION_ITEMS) {
            throw new IllegalArgumentException(
                    field + " is too large for Workflow history"
            );
        }
    }

    private static int historyItemCount(
            Object value,
            int depth,
            String field
    ) {
        if (value == null) {
            return 1;
        }
        if (depth > MAX_HISTORY_DEPTH) {
            throw new IllegalArgumentException(
                    field + " exceeds maximum nesting depth"
            );
        }
        if (value instanceof byte[]
                || value instanceof java.nio.ByteBuffer) {
            throw new IllegalArgumentException(
                    field + " must not contain raw bytes"
            );
        }
        if (value instanceof String text) {
            if (text.length() > MAX_HISTORY_STRING_LENGTH) {
                throw new IllegalArgumentException(
                        field + " contains an oversized string"
                );
            }
            return 1;
        }
        if (value instanceof Number
                || value instanceof Boolean
                || value instanceof Enum<?>
                || value instanceof Instant) {
            return 1;
        }
        if (value instanceof Map<?, ?> map) {
            int count = 1;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (!(entry.getKey() instanceof String key)
                        || key.isBlank()) {
                    throw new IllegalArgumentException(
                            field + " map keys must be nonblank strings"
                    );
                }
                count += historyItemCount(
                        key,
                        depth + 1,
                        field
                );
                count += historyItemCount(
                        entry.getValue(),
                        depth + 1,
                        field
                );
                if (count > MAX_HISTORY_COLLECTION_ITEMS) {
                    return count;
                }
            }
            return count;
        }
        if (value instanceof Collection<?> collection) {
            int count = 1;
            for (Object item : collection) {
                count += historyItemCount(
                        item,
                        depth + 1,
                        field
                );
                if (count > MAX_HISTORY_COLLECTION_ITEMS) {
                    return count;
                }
            }
            return count;
        }

        throw new IllegalArgumentException(
                field + " contains unrestricted payload type "
                        + value.getClass().getName()
        );
    }

    private static String requireText(
            String value,
            String field
    ) {
        String normalized = normalize(value);
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(
                    field + " must not be blank"
            );
        }
        return normalized;
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    public record PendingGate(
            String gateId,
            String plannedStepId,
            String title,
            String reason,
            Set<String> requiredReviewerRoles,
            Instant createdAt,
            Instant expiresAt
    ) {

        public PendingGate {
            gateId = requireText(gateId, "gateId");
            plannedStepId = requireText(
                    plannedStepId,
                    "plannedStepId"
            );
            title = requireText(title, "title");
            reason = requireText(reason, "reason");
            requiredReviewerRoles = immutableTextSet(
                    requiredReviewerRoles,
                    "requiredReviewerRoles"
            );
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

    public record BudgetCounters(
            Counter llmCalls,
            Counter toolCalls,
            Counter retrievalQueries,
            Counter itemsToHydrate,
            Counter inputTokens,
            Counter outputTokens,
            Counter costMicros
    ) {

        public BudgetCounters {
            llmCalls = requireCounter(llmCalls, "llmCalls");
            toolCalls = requireCounter(toolCalls, "toolCalls");
            retrievalQueries = requireCounter(
                    retrievalQueries,
                    "retrievalQueries"
            );
            itemsToHydrate = requireCounter(
                    itemsToHydrate,
                    "itemsToHydrate"
            );
            inputTokens = requireCounter(
                    inputTokens,
                    "inputTokens"
            );
            outputTokens = requireCounter(
                    outputTokens,
                    "outputTokens"
            );
            costMicros = requireCounter(
                    costMicros,
                    "costMicros"
            );
        }

        public static BudgetCounters from(
                MissionBudget budget
        ) {
            Objects.requireNonNull(
                    budget,
                    "budget must not be null"
            );
            return new BudgetCounters(
                    new Counter(budget.maxLlmCalls(), 0L),
                    new Counter(budget.maxToolCalls(), 0L),
                    new Counter(
                            budget.maxRetrievalQueries(),
                            0L
                    ),
                    new Counter(
                            budget.maxItemsToHydrate(),
                            0L
                    ),
                    new Counter(
                            budget.maxInputTokens(),
                            0L
                    ),
                    new Counter(
                            budget.maxOutputTokens(),
                            0L
                    ),
                    new Counter(
                            budget.maxCostMicros(),
                            0L
                    )
            );
        }

        public void requireLlmCall() {
            llmCalls.requireAvailable(1L, "llmCalls");
        }

        public void requireHop(
                boolean retrieval,
                int hydratedItems
        ) {
            if (hydratedItems < 0) {
                throw new IllegalArgumentException(
                        "hydratedItems must not be negative"
                );
            }
            toolCalls.requireAvailable(1L, "toolCalls");
            if (retrieval) {
                retrievalQueries.requireAvailable(
                        1L,
                        "retrievalQueries"
                );
            }
            itemsToHydrate.requireAvailable(
                    hydratedItems,
                    "itemsToHydrate"
            );
        }

        public BudgetCounters consumeLlmCall() {
            requireLlmCall();
            return new BudgetCounters(
                    llmCalls.consume(1L, "llmCalls"),
                    toolCalls,
                    retrievalQueries,
                    itemsToHydrate,
                    inputTokens,
                    outputTokens,
                    costMicros
            );
        }

        public BudgetCounters consumeHop(
                boolean retrieval,
                int hydratedItems
        ) {
            requireHop(retrieval, hydratedItems);
            return new BudgetCounters(
                    llmCalls,
                    toolCalls.consume(1L, "toolCalls"),
                    retrieval
                            ? retrievalQueries.consume(
                            1L,
                            "retrievalQueries"
                    )
                            : retrievalQueries,
                    itemsToHydrate.consume(
                            hydratedItems,
                            "itemsToHydrate"
                    ),
                    inputTokens,
                    outputTokens,
                    costMicros
            );
        }

        private static Counter requireCounter(
                Counter value,
                String field
        ) {
            return Objects.requireNonNull(
                    value,
                    field + " must not be null"
            );
        }
    }

    public record Counter(long limit, long used) {

        public Counter {
            if (limit < 0L || used < 0L || used > limit) {
                throw new IllegalArgumentException(
                        "budget counter is inconsistent"
                );
            }
        }

        public long remaining() {
            return limit - used;
        }

        public void requireAvailable(
                long amount,
                String dimension
        ) {
            if (amount < 0L || amount > remaining()) {
                throw new IllegalStateException(
                        "MISSION_BUDGET_EXHAUSTED: "
                                + dimension
                );
            }
        }

        public Counter consume(
                long amount,
                String dimension
        ) {
            requireAvailable(amount, dimension);
            return new Counter(limit, used + amount);
        }
    }

    private static final class Builder {

        private String missionId;
        private String tenantId;
        private MissionStatus status;
        private MissionIntent intent;
        private MissionPlan currentPlan;
        private PlannedStep lastExecutedStep;
        private Collection<String> completedLogicalStepIds;
        private List<CheckpointRef> observationRefs;
        private BudgetCounters budgetCounters;
        private PendingGate pendingGate;
        private Collection<String> reviewedGateIds;
        private boolean cancellationRequested;
        private String cancellationReason;
        private Instant cancellationRequestedAt;
        private Map<String, String> processedUpdateFingerprints;
        private Instant approvedAt;
        private Instant rejectedAt;
        private Instant cancelledAt;
        private CheckpointRef resultRef;
        private CheckpointRef verificationRef;
        private List<GovernanceDecision> governanceDecisions;
        private String errorReference;
        private int reactorIteration;
        private int activityHopCount;
        private int continueAsNewCount;
        private int publishedEventCount;
        private Instant startedAt;
        private Instant completedAt;
        private String lastReason;

        private Builder(MissionWorkflowState state) {
            missionId = state.missionId;
            tenantId = state.tenantId;
            status = state.status;
            intent = state.intent;
            currentPlan = state.currentPlan;
            lastExecutedStep = state.lastExecutedStep;
            completedLogicalStepIds =
                    state.completedLogicalStepIds;
            observationRefs = state.observationRefs;
            budgetCounters = state.budgetCounters;
            pendingGate = state.pendingGate;
            reviewedGateIds = state.reviewedGateIds;
            cancellationRequested =
                    state.cancellationRequested;
            cancellationReason = state.cancellationReason;
            cancellationRequestedAt =
                    state.cancellationRequestedAt;
            processedUpdateFingerprints =
                    state.processedUpdateFingerprints;
            approvedAt = state.approvedAt;
            rejectedAt = state.rejectedAt;
            cancelledAt = state.cancelledAt;
            resultRef = state.resultRef;
            verificationRef = state.verificationRef;
            governanceDecisions = state.governanceDecisions;
            errorReference = state.errorReference;
            reactorIteration = state.reactorIteration;
            activityHopCount = state.activityHopCount;
            continueAsNewCount = state.continueAsNewCount;
            publishedEventCount = state.publishedEventCount;
            startedAt = state.startedAt;
            completedAt = state.completedAt;
            lastReason = state.lastReason;
        }

        private MissionWorkflowState build() {
            return new MissionWorkflowState(
                    missionId,
                    tenantId,
                    status,
                    intent,
                    currentPlan,
                    lastExecutedStep,
                    completedLogicalStepIds == null
                            ? Set.of()
                            : Set.copyOf(
                            completedLogicalStepIds
                    ),
                    observationRefs,
                    budgetCounters,
                    pendingGate,
                    reviewedGateIds == null
                            ? Set.of()
                            : Set.copyOf(reviewedGateIds),
                    cancellationRequested,
                    cancellationReason,
                    cancellationRequestedAt,
                    processedUpdateFingerprints,
                    approvedAt,
                    rejectedAt,
                    cancelledAt,
                    resultRef,
                    verificationRef,
                    governanceDecisions,
                    errorReference,
                    reactorIteration,
                    activityHopCount,
                    continueAsNewCount,
                    publishedEventCount,
                    startedAt,
                    completedAt,
                    lastReason
            );
        }
    }
}