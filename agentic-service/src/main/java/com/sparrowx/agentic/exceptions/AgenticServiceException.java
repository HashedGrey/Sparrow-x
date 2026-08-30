package com.sparrowx.agentic.exceptions;

import lombok.Getter;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@Getter
public class AgenticServiceException extends RuntimeException {

    private static final String DEFAULT_CODE = "AGENTIC_SERVICE_ERROR";

    private final String code;
    private final boolean retryable;
    private final Map<String, Object> details;

    public AgenticServiceException(String message) {
        this(DEFAULT_CODE, message, false, Map.of(), null);
    }

    public AgenticServiceException(String message, Throwable cause) {
        this(DEFAULT_CODE, message, false, Map.of(), cause);
    }

    public AgenticServiceException(String code, String message) {
        this(code, message, false, Map.of(), null);
    }

    public AgenticServiceException(
            String code,
            String message,
            boolean retryable
    ) {
        this(code, message, retryable, Map.of(), null);
    }

    public AgenticServiceException(
            String code,
            String message,
            boolean retryable,
            Map<String, Object> details
    ) {
        this(code, message, retryable, details, null);
    }

    public AgenticServiceException(
            String code,
            String message,
            boolean retryable,
            Map<String, Object> details,
            Throwable cause
    ) {
        super(normalizeMessage(message), cause);
        this.code = normalizeCode(code);
        this.retryable = retryable;
        this.details = immutableDetails(details);
    }

    public String code() {
        return code;
    }

    public boolean retryable() {
        return retryable;
    }

    public Map<String, Object> details() {
        return details;
    }

    private static String normalizeCode(String value) {
        return value == null || value.isBlank()
                ? DEFAULT_CODE
                : value.trim();
    }

    private static String normalizeMessage(String value) {
        return value == null || value.isBlank()
                ? "Agentic service operation failed."
                : value.trim();
    }

    private static Map<String, Object> immutableDetails(
            Map<String, Object> value
    ) {
        if (value == null || value.isEmpty()) {
            return Map.of();
        }

        return Collections.unmodifiableMap(new LinkedHashMap<>(value));
    }
}