package com.sparrowx.agentic.grpc;

import com.sparrowx.agentic.exceptions.CheckpointCorruptionException;
import com.sparrowx.agentic.exceptions.HumanGateException;
import com.sparrowx.agentic.exceptions.MissionExecutionException;
import com.sparrowx.agentic.exceptions.MissionNotFoundException;
import com.sparrowx.agentic.exceptions.MissionValidationException;
import com.sparrowx.agentic.exceptions.PolicyViolationException;
import com.sparrowx.agentic.exceptions.ToolCallException;
import io.grpc.Status;
import io.grpc.StatusException;
import io.grpc.StatusRuntimeException;
import org.springframework.stereotype.Component;

import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

@Component
public final class GrpcExceptionHandler {

    public StatusRuntimeException toStatusRuntimeException(
            Throwable throwable
    ) {
        Throwable root = unwrap(throwable);

        if (root instanceof StatusRuntimeException statusException) {
            return statusException;
        }

        if (root instanceof StatusException statusException) {
            return statusException.getStatus()
                    .withCause(root)
                    .asRuntimeException();
        }

        Status status = mapStatus(root);
        String description = description(root, status);

        return status
                .withDescription(description)
                .withCause(root)
                .asRuntimeException();
    }

    private static Status mapStatus(Throwable throwable) {
        if (throwable instanceof MissionValidationException
                || throwable instanceof IllegalArgumentException) {
            return Status.INVALID_ARGUMENT;
        }

        if (throwable instanceof MissionNotFoundException) {
            return Status.NOT_FOUND;
        }

        if (throwable instanceof PolicyViolationException
                || throwable instanceof SecurityException) {
            return Status.PERMISSION_DENIED;
        }

        if (throwable instanceof HumanGateException
                || throwable instanceof IllegalStateException) {
            return Status.FAILED_PRECONDITION;
        }

        if (throwable instanceof CheckpointCorruptionException) {
            return Status.DATA_LOSS;
        }

        if (throwable instanceof ToolCallException) {
            return Status.UNAVAILABLE;
        }

        if (throwable instanceof TimeoutException) {
            return Status.DEADLINE_EXCEEDED;
        }

        if (throwable instanceof CancellationException
                || throwable instanceof InterruptedException) {
            return Status.CANCELLED;
        }

        if (throwable instanceof MissionExecutionException) {
            return Status.INTERNAL;
        }

        return Status.INTERNAL;
    }

    private static String description(
            Throwable throwable,
            Status status
    ) {
        String message = throwable.getMessage();

        if (message != null && !message.isBlank()) {
            return message;
        }

        return switch (status.getCode()) {
            case INVALID_ARGUMENT -> "Invalid agentic request";
            case NOT_FOUND -> "Mission was not found";
            case PERMISSION_DENIED -> "Caller is not authorized";
            case FAILED_PRECONDITION ->
                    "Mission is not in the required state";
            case DATA_LOSS -> "Persisted mission data is invalid";
            case UNAVAILABLE ->
                    "A required downstream capability is unavailable";
            case DEADLINE_EXCEEDED ->
                    "Agentic operation exceeded its deadline";
            case CANCELLED -> "Agentic operation was cancelled";
            default -> "Agentic service operation failed";
        };
    }

    private static Throwable unwrap(Throwable throwable) {
        if (throwable == null) {
            return new IllegalStateException(
                    "Unknown Agentic service failure"
            );
        }

        Throwable current = throwable;

        while ((current instanceof CompletionException
                || current instanceof ExecutionException)
                && current.getCause() != null) {
            current = current.getCause();
        }

        return current;
    }
}