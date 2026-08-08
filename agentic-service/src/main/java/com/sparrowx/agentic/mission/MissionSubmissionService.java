package com.sparrowx.agentic.mission;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparrowx.agentic.exceptions.MissionValidationException;
import com.sparrowx.agentic.governance.BudgetPolicy;
import com.sparrowx.agentic.mission.artifact.ArtifactPreparationResult;
import com.sparrowx.agentic.mission.artifact.InputArtifact;
import com.sparrowx.agentic.mission.model.Mission;
import com.sparrowx.agentic.mission.model.MissionBudget;
import com.sparrowx.agentic.mission.model.MissionPath;
import com.sparrowx.agentic.mission.model.MissionRequest;
import com.sparrowx.agentic.mission.model.MissionStatus;
import com.sparrowx.agentic.mission.model.MissionVersionSnapshot;
import com.sparrowx.agentic.mission.store.MissionStore;
import com.sparrowx.agentic.runtime.checkpoint.CheckpointRef;
import com.sparrowx.agentic.runtime.checkpoint.CheckpointSerializer;
import com.sparrowx.agentic.runtime.checkpoint.CheckpointSnapshot;
import com.sparrowx.agentic.runtime.checkpoint.CheckpointStore;
import com.sparrowx.agentic.steps.PrepareInputArtifactsStep;
import com.sparrowx.agentic.temporal.client.TemporalMissionClient;
import com.sparrowx.agentic.temporal.model.MissionWorkflowInput;
import com.sparrowx.agentic.util.Hashing;
import com.sparrowx.agentic.validation.MissionRequestValidator;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class MissionSubmissionService {

    private static final int INPUT_SCHEMA_VERSION = 1;
    private static final int PREPARED_ARTIFACTS_SCHEMA_VERSION = 1;

    private final MissionStore missionStore;
    private final CheckpointStore checkpointStore;
    private final CheckpointSerializer checkpointSerializer;
    private final PrepareInputArtifactsStep prepareInputArtifactsStep;
    private final MissionRequestValidator requestValidator;
    private final BudgetPolicy budgetPolicy;
    private final VersionSnapshotProvider versionSnapshotProvider;
    private final TemporalMissionClient temporalMissionClient;
    private final ObjectMapper objectMapper;

    public MissionSubmissionService(
            MissionStore missionStore,
            CheckpointStore checkpointStore,
            CheckpointSerializer checkpointSerializer,
            PrepareInputArtifactsStep prepareInputArtifactsStep,
            MissionRequestValidator requestValidator,
            BudgetPolicy budgetPolicy,
            VersionSnapshotProvider versionSnapshotProvider,
            TemporalMissionClient temporalMissionClient,
            ObjectMapper objectMapper
    ) {
        this.missionStore = Objects.requireNonNull(
                missionStore,
                "missionStore"
        );
        this.checkpointStore = Objects.requireNonNull(
                checkpointStore,
                "checkpointStore"
        );
        this.checkpointSerializer = Objects.requireNonNull(
                checkpointSerializer,
                "checkpointSerializer"
        );
        this.prepareInputArtifactsStep = Objects.requireNonNull(
                prepareInputArtifactsStep,
                "prepareInputArtifactsStep"
        );
        this.requestValidator = Objects.requireNonNull(
                requestValidator,
                "requestValidator"
        );
        this.budgetPolicy = Objects.requireNonNull(
                budgetPolicy,
                "budgetPolicy"
        );
        this.versionSnapshotProvider = Objects.requireNonNull(
                versionSnapshotProvider,
                "versionSnapshotProvider"
        );
        this.temporalMissionClient = Objects.requireNonNull(
                temporalMissionClient,
                "temporalMissionClient"
        );
        this.objectMapper = Objects.requireNonNull(
                objectMapper,
                "objectMapper"
        );
    }

    public Mission submit(MissionRequest request) {
        Objects.requireNonNull(request, "request");

        MissionBudget normalizedBudget = budgetPolicy.normalize(
                request.budget()
        );

        MissionRequest normalizedRequest = new MissionRequest(
                request.context(),
                request.query(),
                request.inputArtifacts(),
                request.constraints(),
                normalizedBudget
        );

        requestValidator.validate(normalizedRequest);

        String fingerprint = fingerprint(normalizedRequest);
        String tenantId = normalizedRequest.context().tenantId();
        String requestId = normalizedRequest.context().requestId();

        Mission existing = missionStore.findByRequestId(
                tenantId,
                requestId
        ).orElse(null);

        if (existing != null) {
            verifyFingerprint(existing, fingerprint);
            startOrGet(existing, normalizedRequest);
            return existing;
        }

        Instant submittedAt = Instant.now();
        String missionId = stableMissionId(tenantId, requestId);

        MissionVersionSnapshot versionSnapshot =
                Objects.requireNonNull(
                        versionSnapshotProvider.currentSnapshot(),
                        "version snapshot"
                );

        Mission candidate = new Mission(
                missionId,
                normalizedRequest.context(),
                fingerprint,
                normalizedRequest.query(),
                List.of(),
                normalizedRequest.constraints(),
                normalizedRequest.budget(),
                selectPath(normalizedRequest),
                MissionStatus.SUBMITTED,
                versionSnapshot,
                null,
                null,
                submittedAt,
                null,
                submittedAt,
                null
        );

        Mission stored = missionStore.createOrGet(candidate);
        verifyFingerprint(stored, fingerprint);
        startOrGet(stored, normalizedRequest);
        return stored;
    }

    private void startOrGet(
            Mission mission,
            MissionRequest normalizedRequest
    ) {
        CheckpointRef inputReference = persistMissionInput(
                mission,
                normalizedRequest
        );

        ArtifactPreparationResult prepared =
                prepareInputArtifactsStep.execute(
                        mission.missionId(),
                        mission.context().requestId(),
                        normalizedRequest,
                        mission.submittedAt()
                );

        CheckpointRef preparedReference =
                persistPreparedArtifacts(mission, prepared);

        ensureWorkflowStarted(
                mission,
                inputReference,
                preparedReference
        );
    }

    private CheckpointRef persistMissionInput(
            Mission mission,
            MissionRequest request
    ) {
        String checkpointId = "input_" + mission.missionId();

        CheckpointSnapshot snapshot = checkpointSerializer.serialize(
                checkpointId,
                mission.tenantId(),
                mission.missionId(),
                CheckpointRef.CheckpointType.MISSION_INPUT,
                INPUT_SCHEMA_VERSION,
                mission.submittedAt(),
                Map.of(
                        "requestId", mission.context().requestId(),
                        "requestFingerprint",
                        mission.requestFingerprint()
                ),
                request
        );

        return saveAndRequireSame(snapshot);
    }

    private CheckpointRef persistPreparedArtifacts(
            Mission mission,
            ArtifactPreparationResult prepared
    ) {
        String checkpointId =
                "prepared_" + mission.missionId();

        CheckpointSnapshot snapshot = checkpointSerializer.serialize(
                checkpointId,
                mission.tenantId(),
                mission.missionId(),
                CheckpointRef.CheckpointType.PREPARED_ARTIFACTS,
                PREPARED_ARTIFACTS_SCHEMA_VERSION,
                mission.submittedAt(),
                Map.of(
                        "requestId", mission.context().requestId(),
                        "requestFingerprint",
                        mission.requestFingerprint(),
                        "artifactCount",
                        Integer.toString(
                                prepared.preparedArtifacts().size()
                        )
                ),
                prepared
        );

        return saveAndRequireSame(snapshot);
    }

    private CheckpointRef saveAndRequireSame(
            CheckpointSnapshot snapshot
    ) {
        CheckpointRef persisted = Objects.requireNonNull(
                checkpointStore.save(snapshot),
                "checkpointStore.save must not return null"
        );

        if (!snapshot.reference().equals(persisted)) {
            throw new IllegalStateException(
                    "CHECKPOINT_IDEMPOTENCY_CONFLICT: "
                            + snapshot.reference().checkpointId()
            );
        }

        return persisted;
    }

    private void ensureWorkflowStarted(
            Mission mission,
            CheckpointRef inputReference,
            CheckpointRef preparedReference
    ) {
        MissionWorkflowInput workflowInput =
                new MissionWorkflowInput(
                        mission.missionId(),
                        mission.tenantId(),
                        mission.context().requestId(),
                        inputReference,
                        preparedReference,
                        mission.versionSnapshot().snapshotId(),
                        mission.selectedPath(),
                        mission.budget(),
                        mission.context().traceId()
                );

        temporalMissionClient.startOrGet(workflowInput);
    }

    private String fingerprint(MissionRequest request) {
        FingerprintInput input = new FingerprintInput(
                request.context().tenantId(),
                request.context().userId(),
                request.context().projectId(),
                request.context().teamId(),
                request.context().metadata(),
                request.query(),
                request.inputArtifacts(),
                request.constraints(),
                request.budget()
        );

        try {
            return Hashing.sha256Hex(
                    objectMapper.writeValueAsBytes(input)
            );
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException(
                    "Unable to fingerprint mission request",
                    exception
            );
        }
    }

    private static void verifyFingerprint(
            Mission mission,
            String requestedFingerprint
    ) {
        if (!mission.requestFingerprint().equals(
                requestedFingerprint
        )) {
            throw new MissionValidationException(
                    "requestId is already associated with "
                            + "different mission input"
            );
        }
    }

    private static String stableMissionId(
            String tenantId,
            String requestId
    ) {
        String identity = tenantId + '\u001f' + requestId;
        String hash = Hashing.sha256Hex(
                identity.getBytes(StandardCharsets.UTF_8)
        );
        return "msn_" + hash.substring(0, 32);
    }

    private static MissionPath selectPath(
            MissionRequest request
    ) {
        MissionPath preferred =
                request.constraints().preferredPath();

        if (preferred != MissionPath.UNSPECIFIED) {
            return preferred;
        }

        if (request.constraints().requireHumanReview()) {
            return MissionPath.GOVERNED;
        }

        if (request.constraints().requireCitations()
                || !request.inputArtifacts().isEmpty()) {
            return MissionPath.RESEARCH;
        }

        return MissionPath.FAST;
    }

    @FunctionalInterface
    public interface VersionSnapshotProvider {

        MissionVersionSnapshot currentSnapshot();
    }

    private record FingerprintInput(
            String tenantId,
            String userId,
            String projectId,
            String teamId,
            Map<String, String> contextMetadata,
            String query,
            List<InputArtifact> inputArtifacts,
            MissionConstraints constraints,
            MissionBudget budget
    ) {
    }
}