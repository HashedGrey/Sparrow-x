package com.sparrowx.document.dice;

import com.sparrowx.document.domain.models.DocumentEvidenceNode;
import com.sparrowx.document.domain.models.SourceSpan;
import com.sparrowx.document.features.builddocumentevidence.BuildDocumentEvidenceCommand;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NoopDocumentDiceProjectionAdapter implements DocumentDiceProjectionPort {

    @Override
    public ProjectionResult project(
            BuildDocumentEvidenceCommand command,
            List<SourceSpan> sourceSpans
    ) {
        return new ProjectionResult(
                List.of(),
                List.of("No concrete Embabel/DICE projection adapter wired yet; using heuristic EvidenceNormalizer.")
        );
    }
}