package com.sparrowx.agentic.tools.document;

import com.sparrowx.agentic.adapters.document.DocumentGrpcClient;
import com.sparrowx.document.proto.BuildDocumentEvidenceRequest;
import com.sparrowx.document.proto.BuildDocumentEvidenceResponse;
import com.sparrowx.document.proto.GetDocumentRequest;
import com.sparrowx.document.proto.GetDocumentResponse;
import com.sparrowx.document.proto.SearchDocumentSpansRequest;
import com.sparrowx.document.proto.SearchDocumentSpansResponse;
import com.sparrowx.document.proto.VerifyEvidenceGraphRequest;
import com.sparrowx.document.proto.VerifyEvidenceGraphResponse;

import java.util.Objects;

public final class DocumentTool {

    private final DocumentGrpcClient client;

    public DocumentTool(DocumentGrpcClient client) {
        this.client = Objects.requireNonNull(client, "client must not be null");
    }

    public GetDocumentResponse getDocument(MissionContext context, GetDocumentRequest request) {
        return client.getDocument(context, request);
    }

    public SearchDocumentSpansResponse searchSpans(
            MissionContext context,
            SearchDocumentSpansRequest request) {
        return client.searchDocumentSpans(context, request);
    }

    public BuildDocumentEvidenceResponse buildEvidence(
            MissionContext context,
            BuildDocumentEvidenceRequest request) {
        return client.buildDocumentEvidence(context, request);
    }

    public VerifyEvidenceGraphResponse verifyEvidence(
            MissionContext context,
            VerifyEvidenceGraphRequest request) {
        return client.verifyEvidenceGraph(context, request);
    }
}
