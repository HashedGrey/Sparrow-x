package com.sparrowx.document.features.builddocumentevidence;

import com.sparrowx.document.domain.models.DocumentEvidenceGraph;

import java.util.List;

public record BuildDocumentEvidenceResult(
        DocumentEvidenceGraph graph,
        boolean usedChunkRetrieval,
        boolean usedClaimCache,
        double coverageScore,
        List<String> warnings
) {
    public BuildDocumentEvidenceResult {
        warnings = warnings == null ? List.of() : List.copyOf(warnings);
    }
}