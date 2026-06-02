package com.sparrowx.document.features.verifyevidencegraph;

import com.sparrowx.document.domain.models.DocumentEvidenceGraph;
import com.sparrowx.document.domain.valueobjects.VerificationStatus;

import java.util.List;

public record VerifyEvidenceGraphResult(
        boolean supported,
        VerificationStatus verificationStatus,
        double confidence,
        double coverageScore,
        DocumentEvidenceGraph verifiedGraph,
        List<String> unsupportedNodeIds,
        List<String> unsupportedEdgeIds,
        List<String> warnings,
        String explanation
) {
    public VerifyEvidenceGraphResult {
        unsupportedNodeIds = unsupportedNodeIds == null ? List.of() : List.copyOf(unsupportedNodeIds);
        unsupportedEdgeIds = unsupportedEdgeIds == null ? List.of() : List.copyOf(unsupportedEdgeIds);
        warnings = warnings == null ? List.of() : List.copyOf(warnings);
        explanation = explanation == null ? "" : explanation;
    }
}