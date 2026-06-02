package com.sparrowx.document.retrieval;

import com.sparrowx.document.domain.models.SourceSpan;
import com.sparrowx.document.features.builddocumentevidence.BuildDocumentEvidenceCommand;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ClaimCacheRetriever {

    public ClaimCacheResult retrieve(BuildDocumentEvidenceCommand command) {
        return new ClaimCacheResult(
                List.of(),
                List.of("Claim cache retriever is not backed by persistence yet.")
        );
    }

    public record ClaimCacheResult(
            List<SourceSpan> spans,
            List<String> warnings
    ) {
        public ClaimCacheResult {
            spans = spans == null ? List.of() : List.copyOf(spans);
            warnings = warnings == null ? List.of() : List.copyOf(warnings);
        }
    }
}