package com.sparrowx.document.dice;

import com.sparrowx.document.domain.models.DocumentEvidenceNode;
import com.sparrowx.document.domain.models.SourceSpan;
import com.sparrowx.document.features.builddocumentevidence.BuildDocumentEvidenceCommand;

import java.util.List;

public interface DocumentDiceProjectionPort {

    ProjectionResult project(
            BuildDocumentEvidenceCommand command,
            List<SourceSpan> sourceSpans
    );

    record ProjectionResult(
            List<DocumentEvidenceNode> nodes,
            List<String> warnings
    ) {
        public ProjectionResult {
            nodes = nodes == null ? List.of() : List.copyOf(nodes);
            warnings = warnings == null ? List.of() : List.copyOf(warnings);
        }
    }
}