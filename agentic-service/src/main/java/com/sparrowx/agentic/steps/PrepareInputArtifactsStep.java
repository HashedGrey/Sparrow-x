package com.sparrowx.agentic.steps;

import com.sparrowx.agentic.actions.document.UploadDocumentAction;
import com.sparrowx.agentic.mission.artifact.ArtifactPreparationResult;
import com.sparrowx.agentic.mission.artifact.InputArtifact;
import com.sparrowx.agentic.mission.artifact.PreparedArtifact;
import com.sparrowx.agentic.mission.model.MissionRequest;
import com.sparrowx.agentic.runtime.checkpoint.CheckpointRef;
import com.sparrowx.agentic.runtime.checkpoint.CheckpointSerializer;
import com.sparrowx.agentic.runtime.checkpoint.CheckpointSnapshot;
import com.sparrowx.agentic.runtime.checkpoint.CheckpointStore;
import com.sparrowx.agentic.tools.document.UploadDocumentRequestBuilder.UploadSpec;
import com.sparrowx.agentic.validation.ArtifactValidator;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Component
public final class PrepareInputArtifactsStep {

    private static final int INLINE_TEXT_SCHEMA_VERSION = 1;

    private final ArtifactValidator artifactValidator;
    private final UploadDocumentAction uploadDocumentAction;
    private final CheckpointSerializer checkpointSerializer;
    private final CheckpointStore checkpointStore;

    public PrepareInputArtifactsStep(
            ArtifactValidator artifactValidator,
            UploadDocumentAction uploadDocumentAction,
            CheckpointSerializer checkpointSerializer,
            CheckpointStore checkpointStore
    ) {
        this.artifactValidator = Objects.requireNonNull(
                artifactValidator,
                "artifactValidator must not be null"
        );
        this.uploadDocumentAction = Objects.requireNonNull(
                uploadDocumentAction,
                "uploadDocumentAction must not be null"
        );
        this.checkpointSerializer = Objects.requireNonNull(
                checkpointSerializer,
                "checkpointSerializer must not be null"
        );
        this.checkpointStore = Objects.requireNonNull(
                checkpointStore,
                "checkpointStore must not be null"
        );
    }

    public ArtifactPreparationResult execute(
            String missionId,
            String requestId,
            MissionRequest request,
            Instant preparedAt
    ) {
        missionId = requireText(missionId, "missionId");
        requestId = requireText(requestId, "requestId");
        Objects.requireNonNull(request, "request must not be null");
        Objects.requireNonNull(preparedAt, "preparedAt must not be null");

        List<PreparedArtifact> prepared = new ArrayList<>();
        Map<String, String> hashes = new LinkedHashMap<>();
        Map<String, String> outcomes = new LinkedHashMap<>();
        List<String> warnings = new ArrayList<>();
        Set<String> artifactIds = new LinkedHashSet<>();

        for (InputArtifact artifact : request.inputArtifacts()) {
            artifactValidator.validate(artifact);

            if (!artifactIds.add(artifact.artifactId())) {
                throw new IllegalArgumentException(
                        "artifactId must be unique: "
                                + artifact.artifactId()
                );
            }

            PreparedArtifact resolved = prepareOne(
                    missionId,
                    requestId,
                    request,
                    artifact,
                    preparedAt
            );

            prepared.add(resolved);
            hashes.put(artifact.artifactId(), artifact.sha256());
            outcomes.put(
                    artifact.artifactId(),
                    preparationOutcome(artifact, resolved)
            );
        }

        return new ArtifactPreparationResult(
                prepared,
                hashes,
                outcomes,
                warnings
        );
    }

    private PreparedArtifact prepareOne(
            String missionId,
            String requestId,
            MissionRequest request,
            InputArtifact artifact,
            Instant preparedAt
    ) {
        return switch (artifact.contentMode()) {
            case OBJECT_URI -> reference(
                    artifact,
                    "",
                    "",
                    artifact.objectUri(),
                    "",
                    ""
            );
            case EXTERNAL_URI -> reference(
                    artifact,
                    "",
                    "",
                    "",
                    artifact.externalUri(),
                    ""
            );
            case INLINE_TEXT -> checkpointInlineText(
                    missionId,
                    requestId,
                    request,
                    artifact,
                    preparedAt
            );
            case INLINE_BYTES -> uploadInlineBytes(
                    missionId,
                    requestId,
                    request,
                    artifact
            );
            case UNSPECIFIED -> throw new IllegalArgumentException(
                    "artifact content mode must be specified"
            );
        };
    }

    private PreparedArtifact checkpointInlineText(
            String missionId,
            String requestId,
            MissionRequest request,
            InputArtifact artifact,
            Instant preparedAt
    ) {
        String checkpointId = "artifact-text:"
                + missionId
                + ":"
                + artifact.artifactId();

        CheckpointSnapshot snapshot = checkpointSerializer.serialize(
                checkpointId,
                request.context().tenantId(),
                missionId,
                CheckpointRef.CheckpointType.PREPARED_ARTIFACTS,
                INLINE_TEXT_SCHEMA_VERSION,
                preparedAt,
                Map.of(
                        "artifactId", artifact.artifactId(),
                        "effectId", artifactEffectId(
                                requestId,
                                artifact.artifactId()
                        )
                ),
                new InlineTextPayload(
                        artifact.artifactId(),
                        artifact.inlineText()
                )
        );

        CheckpointRef persisted = checkpointStore.save(snapshot);
        requireSameCheckpoint(snapshot.reference(), persisted);

        return reference(
                artifact,
                "",
                "",
                "",
                "",
                "checkpoint:" + persisted.checkpointId()
        );
    }

    private PreparedArtifact uploadInlineBytes(
            String missionId,
            String requestId,
            MissionRequest request,
            InputArtifact artifact
    ) {
        PreparedArtifact uploadCandidate = reference(
                artifact,
                "",
                "",
                "",
                "",
                ""
        );

        String effectId = artifactEffectId(
                requestId,
                artifact.artifactId()
        );

        UploadDocumentAction.Result uploaded =
                uploadDocumentAction.execute(
                        request.context(),
                        new UploadSpec(
                                effectId,
                                uploadCandidate,
                                artifact.inlineBytes(),
                                artifact.filename(),
                                List.of(),
                                Map.of(
                                        "idempotency_key", effectId,
                                        "mission_id", missionId
                                )
                        )
                );

        return reference(
                artifact,
                uploaded.documentId(),
                uploaded.ingestionJobId(),
                "",
                "",
                ""
        );
    }

    private static PreparedArtifact reference(
            InputArtifact artifact,
            String documentId,
            String ingestionJobId,
            String objectUri,
            String externalUri,
            String textReference
    ) {
        return new PreparedArtifact(
                artifact.artifactId(),
                artifact.type(),
                documentId,
                ingestionJobId,
                objectUri,
                externalUri,
                textReference,
                artifact.filename(),
                artifact.contentType(),
                artifact.sha256(),
                artifact.metadata()
        );
    }

    private static String preparationOutcome(
            InputArtifact original,
            PreparedArtifact prepared
    ) {
        if (!prepared.documentId().isBlank()) {
            return "UPLOADED";
        }
        if (!prepared.textReference().isBlank()) {
            return "CHECKPOINTED";
        }
        return original.contentMode().name();
    }

    private static String artifactEffectId(
            String requestId,
            String artifactId
    ) {
        return requestId + ":artifact:" + artifactId;
    }

    private static void requireSameCheckpoint(
            CheckpointRef expected,
            CheckpointRef actual
    ) {
        Objects.requireNonNull(
                actual,
                "checkpointStore.save must not return null"
        );

        if (!expected.equals(actual)) {
            throw new IllegalStateException(
                    "CHECKPOINT_IDEMPOTENCY_CONFLICT: "
                            + expected.checkpointId()
            );
        }
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    field + " must not be blank"
            );
        }
        return value;
    }

    public record InlineTextPayload(
            String artifactId,
            String text
    ) {
        public InlineTextPayload {
            artifactId = requireText(artifactId, "artifactId");
            text = requireText(text, "text");
        }
    }
}