package com.sparrowx.document.features.builddocumentevidence;

import buildingblocks.core.commands.CommandHandler;
import com.sparrowx.document.domain.valueobjects.DocumentId;
import com.sparrowx.document.evidencegraph.DocumentEvidenceRuntime;
import com.sparrowx.document.exceptions.InvalidDocumentException;
import com.sparrowx.document.exceptions.RetrievalFailedException;
import com.sparrowx.document.observability.EvidenceBuildLogger;
import com.sparrowx.document.retrieval.DocumentScopeResolver;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class BuildDocumentEvidenceCommandHandler
        implements CommandHandler<BuildDocumentEvidenceCommand, BuildDocumentEvidenceResult> {

    private final DocumentEvidenceRuntime documentEvidenceRuntime;
    private final EvidenceBuildLogger evidenceBuildLogger;
    private final DocumentScopeResolver documentScopeResolver;

    public BuildDocumentEvidenceCommandHandler(
            DocumentEvidenceRuntime documentEvidenceRuntime,
            EvidenceBuildLogger evidenceBuildLogger,
            DocumentScopeResolver documentScopeResolver
    ) {
        this.documentEvidenceRuntime = documentEvidenceRuntime;
        this.evidenceBuildLogger = evidenceBuildLogger;
        this.documentScopeResolver = documentScopeResolver;
    }

    @Override
    public BuildDocumentEvidenceResult handle(BuildDocumentEvidenceCommand command) {
        validate(command);

        BuildDocumentEvidenceCommand resolvedCommand =
                resolveDocumentScope(command);

        evidenceBuildLogger.buildRequested(
                resolvedCommand.tenantId(),
                resolvedCommand.userId(),
                resolvedCommand.projectId(),
                resolvedCommand.teamId(),
                resolvedCommand.spec().goal(),
                resolvedCommand.scope().documentIds().size(),
                resolvedCommand.limit(),
                resolvedCommand.allowClaimCache(),
                resolvedCommand.requireVerification()
        );

        try {
            BuildDocumentEvidenceResult result = documentEvidenceRuntime.build(resolvedCommand);
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

            if (exception instanceof InvalidDocumentException
                    || exception instanceof RetrievalFailedException) {
                throw exception;
            }

            throw new RetrievalFailedException(
                    "Build document evidence failed",
                    exception
            );
        }
    }

    private BuildDocumentEvidenceCommand resolveDocumentScope(
            BuildDocumentEvidenceCommand command
    ) {
        Set<DocumentId> resolvedDocumentIds =
                documentScopeResolver.resolve(
                        command.tenantId(),
                        command.scope().documentIds(),
                        command.scope().fileNames()
                );

        validateUnsupportedScope(
                command.scope(),
                resolvedDocumentIds
        );

        BuildDocumentEvidenceCommand.DocumentScope resolvedScope =
                new BuildDocumentEvidenceCommand.DocumentScope(
                        resolvedDocumentIds.stream().toList(),
                        List.of(),
                        command.scope().collectionIds(),
                        command.scope().tags(),
                        command.scope().metadataFilters()
                );

        return new BuildDocumentEvidenceCommand(
                command.requestId(),
                command.tenantId(),
                command.userId(),
                command.projectId(),
                command.teamId(),
                command.traceId(),
                command.callerService(),
                resolvedScope,
                command.spec(),
                command.buildContext(),
                command.retrievalMode(),
                command.limit(),
                command.includeExcerpts(),
                command.allowClaimCache(),
                command.requireVerification()
        );
    }

    private void validateUnsupportedScope(
            BuildDocumentEvidenceCommand.DocumentScope scope,
            Set<DocumentId> resolvedDocumentIds
    ) {
        if (scope == null || !resolvedDocumentIds.isEmpty()) {
            return;
        }

        boolean hasUnsupportedScope =
                !scope.collectionIds().isEmpty()
                        || !scope.tags().isEmpty()
                        || !scope.metadataFilters().isEmpty();

        if (hasUnsupportedScope) {
            throw InvalidDocumentException.unsupportedScopeOnly();
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