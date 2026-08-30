package com.sparrowx.agentic.actions.document;

import com.embabel.agent.api.annotation.Action;
import com.sparrowx.agentic.adapters.document.DocumentClientMapper;
import com.sparrowx.agentic.mission.model.MissionContext;
import com.sparrowx.agentic.tools.document.DocumentTool;
import com.sparrowx.document.proto.DocumentEvidenceGraphProto;
import com.sparrowx.document.proto.VerificationStatusProto;
import com.sparrowx.document.proto.VerifyEvidenceGraphRequest;
import com.sparrowx.document.proto.VerifyEvidenceGraphResponse;

import java.util.List;
import java.util.Objects;

public final class VerifyEvidenceGraphAction {

    private final DocumentClientMapper clientMapper;
    private final DocumentTool documentTool;

    public VerifyEvidenceGraphAction(
            DocumentClientMapper clientMapper,
            DocumentTool documentTool) {

        this.clientMapper = Objects.requireNonNull(
                clientMapper,
                "clientMapper must not be null");

        this.documentTool = Objects.requireNonNull(
                documentTool,
                "documentTool must not be null");
    }

    @Action
    public Result execute(
            MissionContext context,
            VerificationSpec spec) {

        Objects.requireNonNull(context, "context must not be null");
        Objects.requireNonNull(spec, "spec must not be null");

        VerifyEvidenceGraphRequest request =
                VerifyEvidenceGraphRequest.newBuilder()
                        .setContext(clientMapper.toRequestContext(
                                context,
                                spec.requestId()))
                        .setGraph(spec.graph())
                        .setRequireAllNodesSupported(
                                spec.requireAllNodesSupported())
                        .setRequireAllEdgesSupported(
                                spec.requireAllEdgesSupported())
                        .build();

        VerifyEvidenceGraphResponse response =
                documentTool.verifyEvidence(context, request);

        if (!response.hasVerifiedGraph()) {
            throw new IllegalStateException(
                    "Document Service returned no verified graph");
        }

        return new Result(
                response.getSupported(),
                response.getVerificationStatus(),
                response.getConfidence(),
                response.getCoverageScore(),
                response.getVerifiedGraph(),
                response.getUnsupportedNodeIdsList(),
                response.getUnsupportedEdgeIdsList(),
                response.getWarningsList(),
                response.getExplanation());
    }

    public record VerificationSpec(
            String requestId,
            DocumentEvidenceGraphProto graph,
            boolean requireAllNodesSupported,
            boolean requireAllEdgesSupported) {

        public VerificationSpec {
            requestId = requireText(requestId, "requestId");

            graph = Objects.requireNonNull(
                    graph,
                    "graph must not be null");

            if (graph.getGraphId().isBlank()) {
                throw new IllegalArgumentException(
                        "graph.graphId must not be blank");
            }
        }
    }

    public record Result(
            boolean supported,
            VerificationStatusProto verificationStatus,
            double confidence,
            double coverageScore,
            DocumentEvidenceGraphProto verifiedGraph,
            List<String> unsupportedNodeIds,
            List<String> unsupportedEdgeIds,
            List<String> warnings,
            String explanation) {

        public Result {
            verificationStatus = Objects.requireNonNull(
                    verificationStatus,
                    "verificationStatus must not be null");

            verifiedGraph = Objects.requireNonNull(
                    verifiedGraph,
                    "verifiedGraph must not be null");

            unsupportedNodeIds = unsupportedNodeIds == null
                    ? List.of()
                    : List.copyOf(unsupportedNodeIds);

            unsupportedEdgeIds = unsupportedEdgeIds == null
                    ? List.of()
                    : List.copyOf(unsupportedEdgeIds);

            warnings = warnings == null
                    ? List.of()
                    : List.copyOf(warnings);

            explanation = explanation == null ? "" : explanation;
        }
    }

    private static String requireText(
            String value,
            String field) {

        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    field + " must not be blank");
        }

        return value;
    }
}