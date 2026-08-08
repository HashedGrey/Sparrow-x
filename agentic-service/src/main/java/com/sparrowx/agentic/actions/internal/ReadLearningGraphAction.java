package com.sparrowx.agentic.actions.internal;

import com.embabel.agent.api.annotation.Action;
import com.sparrowx.agentic.mission.evidence.EvidenceRef;
import com.sparrowx.agentic.tools.internal.InternalContextMapper;
import com.sparrowx.agentic.tools.internal.InternalContextTool;
import com.sparrowx.agentic.tools.internal.InternalGraphRequestBuilder;
import com.sparrowx.agentic.tools.internal.InternalGraphRequestBuilder.GraphSpec;
import com.sparrowx.internal.grpc.InternalGraph;
import com.sparrowx.internal.grpc.ReadLearningGraphRequest;
import com.sparrowx.internal.grpc.ReadLearningGraphResponse;

import java.util.List;
import java.util.Objects;

public final class ReadLearningGraphAction {

    private final InternalGraphRequestBuilder requestBuilder;
    private final InternalContextTool contextTool;
    private final InternalContextMapper contextMapper;

    public ReadLearningGraphAction(
            InternalGraphRequestBuilder requestBuilder,
            InternalContextTool contextTool,
            InternalContextMapper contextMapper) {

        this.requestBuilder = Objects.requireNonNull(
                requestBuilder,
                "requestBuilder must not be null");

        this.contextTool = Objects.requireNonNull(
                contextTool,
                "contextTool must not be null");

        this.contextMapper = Objects.requireNonNull(
                contextMapper,
                "contextMapper must not be null");
    }

    @Action
    public Result execute(
            MissionContext context,
            GraphSpec spec) {

        ReadLearningGraphRequest request =
                requestBuilder.buildLearningGraph(context, spec);

        ReadLearningGraphResponse response =
                contextTool.readLearningGraph(context, request);

        if (!response.hasGraph()) {
            throw new IllegalStateException(
                    "Internal Service returned no learning graph");
        }

        return new Result(
                response.getGraph(),
                contextMapper.fromLearningGraph(response));
    }

    public record Result(
            InternalGraph graph,
            List<EvidenceRef> evidenceRefs) {

        public Result {
            graph = Objects.requireNonNull(
                    graph,
                    "graph must not be null");

            evidenceRefs = evidenceRefs == null
                    ? List.of()
                    : List.copyOf(evidenceRefs);
        }
    }
}