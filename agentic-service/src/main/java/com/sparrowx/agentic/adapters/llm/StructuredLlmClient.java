package com.sparrowx.agentic.adapters.llm;

import com.sparrowx.agentic.adapters.llm.LlmFallbackPolicy.ModelRoute;
import io.temporal.activity.Activity;

import java.util.Map;
import java.util.Objects;

public final class StructuredLlmClient {

    private final Backend backend;

    public StructuredLlmClient(Backend backend) {
        this.backend = Objects.requireNonNull(backend, "backend must not be null");
    }

    public StructuredLlmResponse complete(ModelRoute route, Request request) {
        requireActivityExecution();
        Objects.requireNonNull(route, "route must not be null");
        Objects.requireNonNull(request, "request must not be null");

        return Objects.requireNonNull(
                backend.complete(route, request),
                "structured LLM backend returned null");
    }

    private static void requireActivityExecution() {
        try {
            Activity.getExecutionContext().getInfo();
        } catch (IllegalStateException exception) {
            throw new IllegalStateException(
                    "structured LLM calls are permitted only inside a Temporal Activity",
                    exception);
        }
    }

    @FunctionalInterface
    public interface Backend {
        StructuredLlmResponse complete(ModelRoute route, Request request);
    }

    public record Request(
            String missionId,
            String operationId,
            String idempotencyKey,
            String systemPrompt,
            String userPrompt,
            Map<String, Object> outputSchema,
            int maxOutputTokens,
            double temperature,
            Map<String, String> metadata) {

        public Request {
            missionId = requireText(missionId, "missionId");
            operationId = requireText(operationId, "operationId");
            idempotencyKey = requireText(idempotencyKey, "idempotencyKey");
            systemPrompt = systemPrompt == null ? "" : systemPrompt;
            userPrompt = requireText(userPrompt, "userPrompt");
            outputSchema = outputSchema == null ? Map.of() : Map.copyOf(outputSchema);
            if (maxOutputTokens <= 0) {
                throw new IllegalArgumentException("maxOutputTokens must be positive");
            }
            if (!Double.isFinite(temperature) || temperature < 0.0 || temperature > 2.0) {
                throw new IllegalArgumentException("temperature must be between 0.0 and 2.0");
            }
            metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
        }
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value;
    }
}
