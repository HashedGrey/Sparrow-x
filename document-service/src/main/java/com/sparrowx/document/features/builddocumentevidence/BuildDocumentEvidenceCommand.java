package com.sparrowx.document.features.builddocumentevidence;

import buildingblocks.core.commands.Command;
import com.sparrowx.document.domain.models.DocumentEvidenceEdge;
import com.sparrowx.document.domain.models.DocumentEvidenceGraph;
import com.sparrowx.document.domain.models.DocumentEvidenceNode;
import com.sparrowx.document.domain.valueobjects.CallerService;
import com.sparrowx.document.domain.valueobjects.DocumentId;
import com.sparrowx.document.domain.valueobjects.ProjectId;
import com.sparrowx.document.domain.valueobjects.RequestId;
import com.sparrowx.document.domain.valueobjects.RetrievalMode;
import com.sparrowx.document.domain.valueobjects.TeamId;
import com.sparrowx.document.domain.valueobjects.TenantId;
import com.sparrowx.document.domain.valueobjects.TraceId;
import com.sparrowx.document.domain.valueobjects.UserId;

import java.util.List;
import java.util.Map;

public record BuildDocumentEvidenceCommand(
        RequestId requestId,
        TenantId tenantId,
        UserId userId,
        ProjectId projectId,
        TeamId teamId,
        TraceId traceId,
        CallerService callerService,
        DocumentScope scope,
        EvidenceBuildSpec spec,
        EvidenceBuildContext buildContext,
        RetrievalMode retrievalMode,
        int limit,
        boolean includeExcerpts,
        boolean allowClaimCache,
        boolean requireVerification
) implements Command<BuildDocumentEvidenceResult> {

    public record DocumentScope(
            List<DocumentId> documentIds,
            List<String> fileNames,
            List<String> collectionIds,
            List<String> tags,
            Map<String, String> metadataFilters
    ) {
        public DocumentScope {
            documentIds = documentIds == null ? List.of() : List.copyOf(documentIds);
            fileNames = fileNames == null ? List.of() : List.copyOf(fileNames);
            collectionIds = collectionIds == null ? List.of() : List.copyOf(collectionIds);
            tags = tags == null ? List.of() : List.copyOf(tags);
            metadataFilters = metadataFilters == null ? Map.of() : Map.copyOf(metadataFilters);
        }
    }

    public record EvidenceBuildSpec(
            DocumentEvidenceGraph.EvidenceGoal goal,
            String customGoal,
            List<DocumentEvidenceNode.EvidenceNodeType> requestedNodeTypes,
            List<DocumentEvidenceEdge.EvidenceRelationType> requestedRelationTypes,
            String outputSchemaRef,
            String outputSchemaVersion,
            Map<String, String> options
    ) {
        public EvidenceBuildSpec {
            goal = goal == null ? DocumentEvidenceGraph.EvidenceGoal.UNSPECIFIED : goal;
            customGoal = customGoal == null ? "" : customGoal;
            requestedNodeTypes = requestedNodeTypes == null ? List.of() : List.copyOf(requestedNodeTypes);
            requestedRelationTypes = requestedRelationTypes == null ? List.of() : List.copyOf(requestedRelationTypes);
            outputSchemaRef = outputSchemaRef == null ? "" : outputSchemaRef;
            outputSchemaVersion = outputSchemaVersion == null ? "" : outputSchemaVersion;
            options = options == null ? Map.of() : Map.copyOf(options);
        }
    }

    public record EvidenceBuildContext(
            String retrievalHint,
            List<String> topics,
            List<String> entityNames,
            List<String> keywords,
            Map<String, String> metadataFilters,
            String debugTaskInstruction
    ) {
        public EvidenceBuildContext {
            retrievalHint = retrievalHint == null ? "" : retrievalHint;
            topics = topics == null ? List.of() : List.copyOf(topics);
            entityNames = entityNames == null ? List.of() : List.copyOf(entityNames);
            keywords = keywords == null ? List.of() : List.copyOf(keywords);
            metadataFilters = metadataFilters == null ? Map.of() : Map.copyOf(metadataFilters);
            debugTaskInstruction = debugTaskInstruction == null ? "" : debugTaskInstruction;
        }
    }
}