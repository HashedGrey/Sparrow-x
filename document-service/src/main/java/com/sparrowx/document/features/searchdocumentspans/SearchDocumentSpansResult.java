package com.sparrowx.document.features.searchdocumentspans;

import com.sparrowx.document.domain.models.SourceSpan;

import java.util.List;

public record SearchDocumentSpansResult(
        List<SourceSpan> spans,
        double coverageScore,
        List<String> warnings
) {
    public SearchDocumentSpansResult {
        spans = spans == null ? List.of() : List.copyOf(spans);
        warnings = warnings == null ? List.of() : List.copyOf(warnings);
    }
}