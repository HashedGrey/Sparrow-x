package com.sparrowx.agentic.adapters.llm;

import java.util.Map;

public record StructuredLlmResponse(
        Map<String, Object> parsedOutput,
        String rawText,
        String provider,
        String model,
        String finishReason,
        long inputTokens,
        long outputTokens,
        long costMicros,
        Map<String, String> metadata) {

    public StructuredLlmResponse {
        parsedOutput = parsedOutput == null ? Map.of() : Map.copyOf(parsedOutput);
        rawText = rawText == null ? "" : rawText;
        provider = requireText(provider, "provider");
        model = requireText(model, "model");
        finishReason = finishReason == null ? "" : finishReason;
        if (inputTokens < 0 || outputTokens < 0 || costMicros < 0) {
            throw new IllegalArgumentException("token usage and cost must not be negative");
        }
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    public long totalTokens() {
        return Math.addExact(inputTokens, outputTokens);
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value;
    }
}
