package com.sparrowx.agentic.actions.document;

import com.embabel.agent.api.annotation.Action;
import com.sparrowx.agentic.mission.evidence.EvidenceRef;
import com.sparrowx.agentic.mission.model.MissionContext;
import com.sparrowx.agentic.tools.document.DocumentEvidenceMapper;
import com.sparrowx.agentic.tools.document.DocumentSpanSearchRequestBuilder;
import com.sparrowx.agentic.tools.document.DocumentSpanSearchRequestBuilder.SearchSpec;
import com.sparrowx.agentic.tools.document.DocumentTool;
import com.sparrowx.document.proto.SearchDocumentSpansRequest;
import com.sparrowx.document.proto.SearchDocumentSpansResponse;

import java.util.List;
import java.util.Objects;

public final class SearchDocumentSpansAction {

    private final DocumentSpanSearchRequestBuilder requestBuilder;
    private final DocumentTool documentTool;
    private final DocumentEvidenceMapper evidenceMapper;

    public SearchDocumentSpansAction(
            DocumentSpanSearchRequestBuilder requestBuilder,
            DocumentTool documentTool,
            DocumentEvidenceMapper evidenceMapper) {

        this.requestBuilder = Objects.requireNonNull(
                requestBuilder,
                "requestBuilder must not be null");

        this.documentTool = Objects.requireNonNull(
                documentTool,
                "documentTool must not be null");

        this.evidenceMapper = Objects.requireNonNull(
                evidenceMapper,
                "evidenceMapper must not be null");
    }

    @Action
    public Result execute(
            MissionContext context,
            SearchSpec spec) {

        SearchDocumentSpansRequest request =
                requestBuilder.build(context, spec);

        SearchDocumentSpansResponse response =
                documentTool.searchSpans(context, request);

        return new Result(
                evidenceMapper.fromSearch(response),
                response.getCoverageScore(),
                response.getWarningsList());
    }

    public record Result(
            List<EvidenceRef> evidenceRefs,
            double coverageScore,
            List<String> warnings) {

        public Result {
            evidenceRefs = evidenceRefs == null
                    ? List.of()
                    : List.copyOf(evidenceRefs);

            warnings = warnings == null
                    ? List.of()
                    : List.copyOf(warnings);
        }
    }
}