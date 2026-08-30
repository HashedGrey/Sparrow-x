package com.sparrowx.agentic.actions.document;

import com.embabel.agent.api.annotation.Action;
import com.sparrowx.agentic.mission.model.MissionContext;
import com.sparrowx.agentic.tools.document.DocumentIngestionTool;
import com.sparrowx.agentic.tools.document.UploadDocumentRequestBuilder;
import com.sparrowx.agentic.tools.document.UploadDocumentRequestBuilder.UploadSpec;
import com.sparrowx.document.proto.DocumentStatusProto;
import com.sparrowx.document.proto.UploadDocumentRequest;
import com.sparrowx.document.proto.UploadDocumentResponse;

import java.util.Objects;

public final class UploadDocumentAction {

    private final UploadDocumentRequestBuilder requestBuilder;
    private final DocumentIngestionTool ingestionTool;

    public UploadDocumentAction(
            UploadDocumentRequestBuilder requestBuilder,
            DocumentIngestionTool ingestionTool) {

        this.requestBuilder = Objects.requireNonNull(
                requestBuilder,
                "requestBuilder must not be null");

        this.ingestionTool = Objects.requireNonNull(
                ingestionTool,
                "ingestionTool must not be null");
    }

    @Action
    public Result execute(
            MissionContext context,
            UploadSpec spec) {

        UploadDocumentRequest request =
                requestBuilder.build(context, spec);

        UploadDocumentResponse response =
                ingestionTool.uploadOne(context, request);

        return new Result(
                response.getDocumentId(),
                response.getIngestionJobId(),
                response.getStatus());
    }

    public record Result(
            String documentId,
            String ingestionJobId,
            DocumentStatusProto status) {

        public Result {
            documentId = requireText(documentId, "documentId");
            ingestionJobId = requireText(
                    ingestionJobId,
                    "ingestionJobId");

            status = Objects.requireNonNull(
                    status,
                    "status must not be null");
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