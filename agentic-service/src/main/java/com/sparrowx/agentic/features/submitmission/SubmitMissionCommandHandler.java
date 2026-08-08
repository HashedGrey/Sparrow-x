package com.sparrowx.agentic.features.submitmission;

import buildingblocks.core.commands.CommandHandler;
import com.sparrowx.agentic.mission.artifact.ArtifactType;
import com.sparrowx.agentic.mission.artifact.InputArtifact;
import com.sparrowx.agentic.mission.model.Mission;
import com.sparrowx.agentic.mission.model.MissionBudget;
import com.sparrowx.agentic.mission.model.MissionPath;
import com.sparrowx.agentic.mission.model.MissionRequest;
import com.sparrowx.agentic.exceptions.AgenticServiceException;
import com.sparrowx.agentic.mission.MissionSubmissionService;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Locale;

@Component
public final class SubmitMissionCommandHandler
        implements CommandHandler<SubmitMissionCommand, SubmitMissionResult> {

    private static final String ARTIFACT_TYPE_PREFIX = "ARTIFACT_TYPE_";
    private static final String MISSION_PATH_PREFIX = "MISSION_PATH_";

    private final SubmitMissionCommandValidator validator;
    private final MissionSubmissionService submissionService;

    public SubmitMissionCommandHandler(
            SubmitMissionCommandValidator validator,
            MissionSubmissionService submissionService
    ) {
        this.validator = validator;
        this.submissionService = submissionService;
    }

    @Override
    public SubmitMissionResult handle(SubmitMissionCommand command) {
        validator.validate(command);

        MissionRequest request = toMissionRequest(command);
        Mission mission = submissionService.submit(request);

        if (mission == null) {
            throw new AgenticServiceException(
                    "Mission submission returned no durable mission state."
            );
        }

        if (!command.tenantId().equals(mission.context().tenantId())) {
            throw new AgenticServiceException(
                    "Mission submission returned state for a different tenant."
            );
        }

        if (!command.requestId().equals(mission.context().requestId())) {
            throw new AgenticServiceException(
                    "Mission submission returned state for a different request."
            );
        }

        return new SubmitMissionResult(
                mission.missionId(),
                mission.status(),
                mission.selectedPath(),
                mission.submittedAt()
        );
    }

    private static MissionRequest toMissionRequest(SubmitMissionCommand command) {
        MissionContext context = new MissionContext(
                command.requestId(),
                command.tenantId(),
                command.userId(),
                command.username(),
                command.projectId(),
                command.teamId(),
                command.traceId(),
                command.callerService(),
                command.sessionId(),
                command.conversationId(),
                command.clientChannel(),
                command.metadata()
        );

        List<InputArtifact> artifacts = command.inputArtifacts().stream()
                .map(SubmitMissionCommandHandler::toInputArtifact)
                .toList();

        SubmitMissionCommand.MissionConstraintsInput constraintsInput =
                command.constraints();

        MissionConstraints constraints = new MissionConstraints(
                toMissionPath(constraintsInput.preferredPath()),
                constraintsInput.allowedTools(),
                constraintsInput.allowedSourceServices(),
                constraintsInput.requiredOutputSections(),
                constraintsInput.requireCitations(),
                constraintsInput.requireHumanReview(),
                constraintsInput.allowExternalSources(),
                Duration.ofSeconds(constraintsInput.maxRuntimeSeconds()),
                constraintsInput.policyHints()
        );

        SubmitMissionCommand.MissionBudgetInput budgetInput = command.budget();

        MissionBudget budget = new MissionBudget(
                budgetInput.maxLlmCalls(),
                budgetInput.maxToolCalls(),
                budgetInput.maxRetrievalQueries(),
                budgetInput.maxItemsToHydrate(),
                budgetInput.maxInputTokens(),
                budgetInput.maxOutputTokens(),
                budgetInput.maxCostMicros()
        );

        return new MissionRequest(
                context,
                command.query(),
                artifacts,
                constraints,
                budget
        );
    }

    private static InputArtifact toInputArtifact(
            SubmitMissionCommand.InputArtifactInput input
    ) {
        return new InputArtifact(
                input.artifactId(),
                toArtifactType(input.type()),
                contentMode(input),
                input.objectUri(),
                input.inlineBytes(),
                input.externalUri(),
                input.inlineText(),
                input.filename(),
                input.contentType(),
                input.sha256(),
                input.metadata()
        );
    }

    private static InputArtifact.ContentMode contentMode(
            SubmitMissionCommand.InputArtifactInput input
    ) {
        if (!input.objectUri().isBlank()) {
            return InputArtifact.ContentMode.OBJECT_URI;
        }
        if (input.inlineBytes().length > 0) {
            return InputArtifact.ContentMode.INLINE_BYTES;
        }
        if (!input.externalUri().isBlank()) {
            return InputArtifact.ContentMode.EXTERNAL_URI;
        }
        if (!input.inlineText().isBlank()) {
            return InputArtifact.ContentMode.INLINE_TEXT;
        }
        return InputArtifact.ContentMode.UNSPECIFIED;
    }

    private static ArtifactType toArtifactType(String value) {
        String normalized = normalizeEnum(value, ARTIFACT_TYPE_PREFIX);

        try {
            return ArtifactType.valueOf(normalized);
        } catch (IllegalArgumentException exception) {
            return ArtifactType.UNSPECIFIED;
        }
    }

    private static MissionPath toMissionPath(String value) {
        String normalized = normalizeEnum(value, MISSION_PATH_PREFIX);

        try {
            return MissionPath.valueOf(normalized);
        } catch (IllegalArgumentException exception) {
            return MissionPath.UNSPECIFIED;
        }
    }

    private static String normalizeEnum(String value, String prefix) {
        String normalized = value == null
                ? ""
                : value.trim().toUpperCase(Locale.ROOT);

        if (normalized.startsWith(prefix)) {
            normalized = normalized.substring(prefix.length());
        }

        return normalized.isEmpty() ? "UNSPECIFIED" : normalized;
    }
}