package com.sparrowx.agentic.tools.document;

import com.sparrowx.agentic.adapters.document.DocumentClientMapper;
import com.sparrowx.agentic.adapters.document.DocumentGrpcClient;
import com.sparrowx.agentic.mission.model.MissionContext;
import com.sparrowx.document.proto.GetIngestionJobRequest;
import com.sparrowx.document.proto.GetIngestionJobResponse;
import com.sparrowx.document.proto.IngestionStatusProto;
import com.sparrowx.document.proto.UploadDocumentRequest;
import com.sparrowx.document.proto.UploadDocumentResponse;

import java.util.Objects;

public final class DocumentIngestionTool {

    private final DocumentGrpcClient client;
    private final DocumentClientMapper clientMapper;

    public DocumentIngestionTool(DocumentGrpcClient client, DocumentClientMapper clientMapper) {
        this.client = Objects.requireNonNull(client, "client must not be null");
        this.clientMapper = Objects.requireNonNull(clientMapper, "clientMapper must not be null");
    }

    public UploadDocumentResponse uploadOne(
            MissionContext context,
            UploadDocumentRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        if (request.getContext().getRequestId().isBlank()) {
            throw new IllegalArgumentException("upload request requires a stable idempotency requestId");
        }
        return client.uploadDocument(context, request);
    }

    public GetIngestionJobResponse checkOnce(
            MissionContext context,
            String requestId,
            String ingestionJobId) {
        if (ingestionJobId == null || ingestionJobId.isBlank()) {
            throw new IllegalArgumentException("ingestionJobId must not be blank");
        }

        GetIngestionJobRequest request = GetIngestionJobRequest.newBuilder()
                .setContext(clientMapper.toRequestContext(context, requestId))
                .setIngestionJobId(ingestionJobId)
                .build();
        return client.getIngestionJob(context, request);
    }

    public boolean isTerminal(GetIngestionJobResponse response) {
        Objects.requireNonNull(response, "response must not be null");
        if (!response.hasJob()) {
            throw new IllegalArgumentException("ingestion response must contain a job");
        }
        IngestionStatusProto status = response.getJob().getStatus();
        return status == IngestionStatusProto.INGESTION_STATUS_COMPLETED
                || status == IngestionStatusProto.INGESTION_STATUS_FAILED;
    }
}
