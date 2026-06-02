package com.sparrowx.document.ingestion.pipeline;

import com.sparrowx.document.domain.valueobjects.DocumentId;
import com.sparrowx.document.domain.valueobjects.IngestionJobId;

import java.util.List;

public record IngestionPipelineResult(
        IngestionJobId ingestionJobId,
        DocumentId documentId,
        String extractedText,
        int extractedCharacters,
        int pageCount,
        int chunksCreated,
        int chunksIndexed,
        List<IngestionPipelineStep> completedSteps
) {
    public IngestionPipelineResult {
        completedSteps = completedSteps == null ? List.of() : List.copyOf(completedSteps);
    }
}