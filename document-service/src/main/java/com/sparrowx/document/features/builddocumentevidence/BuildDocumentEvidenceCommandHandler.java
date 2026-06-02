package com.sparrowx.document.features.builddocumentevidence;

import buildingblocks.core.commands.CommandHandler;
import com.sparrowx.document.dice.DocumentDiceRuntime;
import com.sparrowx.document.exceptions.InvalidDocumentException;
import com.sparrowx.document.exceptions.RetrievalFailedException;
import com.sparrowx.document.observability.EvidenceBuildLogger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class BuildDocumentEvidenceCommandHandler
        implements CommandHandler<BuildDocumentEvidenceCommand, BuildDocumentEvidenceResult> {

    private final DocumentDiceRuntime documentDiceRuntime;
    private final EvidenceBuildLogger evidenceBuildLogger;

    public BuildDocumentEvidenceCommandHandler(
            DocumentDiceRuntime documentDiceRuntime,
            EvidenceBuildLogger evidenceBuildLogger
    ) {
        this.documentDiceRuntime = documentDiceRuntime;
        this.evidenceBuildLogger = evidenceBuildLogger;
    }

    @Override
    @Transactional(readOnly = true)
    public BuildDocumentEvidenceResult handle(BuildDocumentEvidenceCommand command) {
        validate(command);

        evidenceBuildLogger.buildRequested(
                command.tenantId(),
                command.userId(),
                command.projectId(),
                command.teamId(),
                command.spec().goal(),
                command.scope().documentIds().size(),
                command.limit(),
                command.allowClaimCache(),
                command.requireVerification()
        );

        try {
            BuildDocumentEvidenceResult result = documentDiceRuntime.build(command);

            evidenceBuildLogger.buildCompleted(
                    command.tenantId(),
                    command.userId(),
                    result.graph() == null ? "" : result.graph().graphId(),
                    result.graph() == null ? 0 : result.graph().nodes().size(),
                    result.graph() == null ? 0 : result.graph().edges().size(),
                    result.usedChunkRetrieval(),
                    result.usedClaimCache(),
                    result.coverageScore()
            );

            return result;

        } catch (RuntimeException exception) {
            evidenceBuildLogger.buildFailed(
                    command.tenantId(),
                    command.userId(),
                    exception.getMessage()
            );

            if (exception instanceof RetrievalFailedException) {
                throw exception;
            }

            throw new RetrievalFailedException(
                    "Build document evidence failed",
                    exception
            );
        }
    }

    private void validate(BuildDocumentEvidenceCommand command) {
        if (command == null) {
            throw InvalidDocumentException.nullQuery("BuildDocumentEvidenceCommand");
        }

        if (command.tenantId() == null) {
            throw InvalidDocumentException.blankField("tenantId");
        }

        if (command.userId() == null) {
            throw InvalidDocumentException.blankField("userId");
        }

        if (command.scope() == null) {
            throw InvalidDocumentException.blankField("scope");
        }

        if (command.spec() == null) {
            throw InvalidDocumentException.blankField("spec");
        }

        if (command.buildContext() == null) {
            throw InvalidDocumentException.blankField("buildContext");
        }

        if (command.limit() < 0) {
            throw InvalidDocumentException.blankField("limit");
        }
    }
}