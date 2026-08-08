package com.sparrowx.agentic.exceptions;

import java.util.Map;

public final class PolicyViolationException
        extends AgenticServiceException {

    private final String policyName;

    public PolicyViolationException(String message) {
        this("", message, null);
    }

    public PolicyViolationException(
            String message,
            Throwable cause
    ) {
        this("", message, cause);
    }

    public PolicyViolationException(
            String policyName,
            String message
    ) {
        this(policyName, message, null);
    }

    public PolicyViolationException(
            String policyName,
            String message,
            Throwable cause
    ) {
        super(
                "POLICY_VIOLATION",
                message,
                false,
                details(policyName),
                cause
        );
        this.policyName = normalize(policyName);
    }

    public String policyName() {
        return policyName;
    }

    private static Map<String, Object> details(String policyName) {
        String normalized = normalize(policyName);

        return normalized.isEmpty()
                ? Map.of()
                : Map.of("policyName", normalized);
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}