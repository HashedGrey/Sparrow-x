package com.sparrowx.document.evidencegraph.embabel;

import com.embabel.agent.core.Semantics;
import com.embabel.agent.core.With;

public record DocumentEvidenceFact(
        String id,

        @Semantics({
                @With(key = "predicate", value = "states")
        })
        String statement,

        String sourceSpanId,
        String documentId,
        String chunkId,
        String citation,
        String evidenceType
) {
}