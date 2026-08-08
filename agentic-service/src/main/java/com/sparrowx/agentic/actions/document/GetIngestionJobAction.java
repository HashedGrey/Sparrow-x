package com.sparrowx.agentic.actions.document;

import com.embabel.agent.api.annotation.Action;
import com.google.protobuf.Timestamp;
import com.sparrowx.agentic.tools.document.DocumentIngestionTool;
import com.sparrowx.document.proto.GetIngestionJobResponse;
import com.sparrowx.document.proto.IngestionJobProto;
import com.sparrowx.document.proto.IngestionStatusProto;

import java.time.Instant;
import java.util.Objects;

public final class GetIngestionJobAction {

    private final DocumentIngestionTool ingestionTool;

    public GetIngestionJobAction(
            DocumentIngestionTool ingestionTool) {

        this.ingestionTool = Objects.requireNonNull(
                ingestionTool,
                "ingestionTool must not be null");
    }

    @Action
    public Result execute(
            MissionContext context,
            String requestId,
            String ingestionJobId) {

        GetIngestionJobResponse response =
                ingestionTool.checkOnce(
                        context,
                        requestId,
                        ingestionJobId);

        if (!response.hasJob()) {
            throw new IllegalStateException(
                    "Document Service returned no ingestion job");
        }

        IngestionJobProto job = response.getJob();

        return new Result(
                job.getIngestionJobId(),
                job.getDocumentId(),
                job.getStatus(),
                job.getFailureReason(),
                job.getChunksCreated(),
                job.getChunksIndexed(),
                toInstant(job.getCreatedAt()),
                toInstant(job.getCompletedAt()),
                ingestionTool.isTerminal(response),
                job.getStatus()
                        == IngestionStatusProto
                        .INGESTION_STATUS_COMPLETED);
    }

    private static Instant toInstant(Timestamp timestamp) {
        return timestamp.equals(Timestamp.getDefaultInstance())
                ? null
                : Instant.ofEpochSecond(
                timestamp.getSeconds(),
                timestamp.getNanos());
    }

    public record Result(
            String ingestionJobId,
            String documentId,
            IngestionStatusProto status,
            String failureReason,
            int chunksCreated,
            int chunksIndexed,
            Instant createdAt,
            Instant completedAt,
            boolean terminal,
            boolean completed) {

        public Result {
            ingestionJobId = requireText(
                    ingestionJobId,
                    "ingestionJobId");

            documentId = documentId == null
                    ? ""
                    : documentId;

            status = Objects.requireNonNull(
                    status,
                    "status must not be null");

            failureReason = failureReason == null
                    ? ""
                    : failureReason;
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