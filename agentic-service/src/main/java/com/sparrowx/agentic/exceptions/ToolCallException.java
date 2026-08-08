package com.sparrowx.agentic.exceptions;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ToolCallException extends AgenticServiceException {

    private final String toolName;
    private final String operation;

    public ToolCallException(String message) {
        this("", "", message, false, null);
    }

    public ToolCallException(String message, Throwable cause) {
        this("", "", message, false, cause);
    }

    public ToolCallException(
            String message,
            boolean retryable
    ) {
        this("", "", message, retryable, null);
    }

    public ToolCallException(
            String message,
            Throwable cause,
            boolean retryable
    ) {
        this("", "", message, retryable, cause);
    }

    public ToolCallException(
            String toolName,
            String message
    ) {
        this(toolName, "", message, false, null);
    }

    public ToolCallException(
            String toolName,
            String message,
            boolean retryable
    ) {
        this(toolName, "", message, retryable, null);
    }

    public ToolCallException(
            String toolName,
            String message,
            boolean retryable,
            Throwable cause
    ) {
        this(toolName, "", message, retryable, cause);
    }

    public ToolCallException(
            String toolName,
            String operation,
            String message,
            boolean retryable,
            Throwable cause
    ) {
        super(
                "TOOL_CALL_FAILED",
                message,
                retryable,
                details(toolName, operation),
                cause
        );
        this.toolName = normalize(toolName);
        this.operation = normalize(operation);
    }

    public String toolName() {
        return toolName;
    }

    public String operation() {
        return operation;
    }

    private static Map<String, Object> details(
            String toolName,
            String operation
    ) {
        Map<String, Object> details = new LinkedHashMap<>();

        String normalizedToolName = normalize(toolName);
        String normalizedOperation = normalize(operation);

        if (!normalizedToolName.isEmpty()) {
            details.put("toolName", normalizedToolName);
        }

        if (!normalizedOperation.isEmpty()) {
            details.put("operation", normalizedOperation);
        }

        return details;
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}