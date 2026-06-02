package com.sparrowx.document.domain.models;

import com.sparrowx.document.domain.valueobjects.VerificationStatus;

import java.time.Instant;
import java.util.List;

public record DocumentEvidenceGraph(
        String graphId,
        EvidenceGoal goal,
        String customGoal,
        List<DocumentEvidenceNode> nodes,
        List<DocumentEvidenceEdge> edges,
        List<SourceSpan> sourcePool,
        VerificationStatus verificationStatus,
        double confidence,
        double coverageScore,
        List<String> warnings,
        List<String> missingNodeTypes,
        String outputSchemaRef,
        String outputSchemaVersion,
        Instant createdAt
) {
    public DocumentEvidenceGraph {
        graphId = graphId == null ? "" : graphId;
        goal = goal == null ? EvidenceGoal.UNSPECIFIED : goal;
        customGoal = customGoal == null ? "" : customGoal;
        nodes = nodes == null ? List.of() : List.copyOf(nodes);
        edges = edges == null ? List.of() : List.copyOf(edges);
        sourcePool = sourcePool == null ? List.of() : List.copyOf(sourcePool);
        verificationStatus = verificationStatus == null
                ? VerificationStatus.UNVERIFIED
                : verificationStatus;
        warnings = warnings == null ? List.of() : List.copyOf(warnings);
        missingNodeTypes = missingNodeTypes == null ? List.of() : List.copyOf(missingNodeTypes);
        outputSchemaRef = outputSchemaRef == null ? "" : outputSchemaRef;
        outputSchemaVersion = outputSchemaVersion == null ? "" : outputSchemaVersion;
        createdAt = createdAt == null ? Instant.now() : createdAt;
    }

    public enum EvidenceGoal {
        UNSPECIFIED,
        EXTRACTION,
        COMPARISON,
        CONTRADICTION_DETECTION,
        COMPLIANCE_AUDIT,
        CUSTOM
    }
}