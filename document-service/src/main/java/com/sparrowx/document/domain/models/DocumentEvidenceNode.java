package com.sparrowx.document.domain.models;

import com.sparrowx.document.domain.valueobjects.VerificationStatus;

import java.util.List;
import java.util.Map;

public record DocumentEvidenceNode(
        String nodeId,
        EvidenceNodeType nodeType,
        String customNodeType,
        String title,
        String summary,
        String normalizedText,
        List<String> sourceSpanIds,
        VerificationStatus verificationStatus,
        double confidence,
        double coverageScore,
        boolean requiresSourceContext,
        List<String> tags,
        List<String> warnings,
        Map<String, String> attributes
) {
    public DocumentEvidenceNode {
        nodeId = nodeId == null ? "" : nodeId;
        nodeType = nodeType == null ? EvidenceNodeType.UNSPECIFIED : nodeType;
        customNodeType = customNodeType == null ? "" : customNodeType;
        title = title == null ? "" : title;
        summary = summary == null ? "" : summary;
        normalizedText = normalizedText == null ? "" : normalizedText;
        sourceSpanIds = sourceSpanIds == null ? List.of() : List.copyOf(sourceSpanIds);
        verificationStatus = verificationStatus == null
                ? VerificationStatus.UNVERIFIED
                : verificationStatus;
        tags = tags == null ? List.of() : List.copyOf(tags);
        warnings = warnings == null ? List.of() : List.copyOf(warnings);
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }

    public enum EvidenceNodeType {
        UNSPECIFIED,
        CLAIM,
        ENTITY,
        FRAMEWORK,
        METRIC,
        OBLIGATION,
        CUSTOM
    }
}