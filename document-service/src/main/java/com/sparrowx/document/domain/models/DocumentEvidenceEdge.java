package com.sparrowx.document.domain.models;

import java.util.List;
import java.util.Map;

public record DocumentEvidenceEdge(
        String edgeId,
        String fromNodeId,
        String toNodeId,
        EvidenceRelationType relationType,
        String customRelationType,
        String rationale,
        List<String> sourceSpanIds,
        double confidence,
        List<String> warnings,
        Map<String, String> attributes
) {
    public DocumentEvidenceEdge {
        edgeId = edgeId == null ? "" : edgeId;
        fromNodeId = fromNodeId == null ? "" : fromNodeId;
        toNodeId = toNodeId == null ? "" : toNodeId;
        relationType = relationType == null
                ? EvidenceRelationType.UNSPECIFIED
                : relationType;
        customRelationType = customRelationType == null ? "" : customRelationType;
        rationale = rationale == null ? "" : rationale;
        sourceSpanIds = sourceSpanIds == null ? List.of() : List.copyOf(sourceSpanIds);
        warnings = warnings == null ? List.of() : List.copyOf(warnings);
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }

    public enum EvidenceRelationType {
        UNSPECIFIED,
        SUPPORTS,
        CONTRADICTS,
        MODIFIES,
        DEPENDS_ON,
        SIMILAR_TO,
        CUSTOM
    }
}