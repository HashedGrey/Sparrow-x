package com.sparrowx.agentic.actions.synthesis;

import com.embabel.agent.api.annotation.Action;
import com.sparrowx.agentic.adapters.llm.LlmFallbackPolicy;
import com.sparrowx.agentic.adapters.llm.LlmFallbackPolicy.ModelRoute;
import com.sparrowx.agentic.adapters.llm.LlmFallbackPolicy.Selection;
import com.sparrowx.agentic.adapters.llm.StructuredLlmClient;
import com.sparrowx.agentic.adapters.llm.StructuredLlmClient.Request;
import com.sparrowx.agentic.adapters.llm.StructuredLlmResponse;

import java.util.Objects;

public final class ComposeAnswerAction {

    private final LlmFallbackPolicy fallbackPolicy;
    private final StructuredLlmClient llmClient;

    public ComposeAnswerAction(
            LlmFallbackPolicy fallbackPolicy,
            StructuredLlmClient llmClient) {

        this.fallbackPolicy = Objects.requireNonNull(
                fallbackPolicy,
                "fallbackPolicy must not be null");

        this.llmClient = Objects.requireNonNull(
                llmClient,
                "llmClient must not be null");
    }

    @Action
    public Result execute(ComposeSpec spec) {
        Objects.requireNonNull(spec, "spec must not be null");

        ModelRoute route =
                fallbackPolicy.selectInitial(spec.selection());

        StructuredLlmResponse response =
                llmClient.complete(route, spec.request());

        return new Result(route.key(), response);
    }

    public record ComposeSpec(
            Selection selection,
            Request request) {

        public ComposeSpec {
            selection = Objects.requireNonNull(
                    selection,
                    "selection must not be null");

            request = Objects.requireNonNull(
                    request,
                    "request must not be null");
        }
    }

    public record Result(
            String routeKey,
            StructuredLlmResponse response) {

        public Result {
            routeKey = requireText(routeKey, "routeKey");

            response = Objects.requireNonNull(
                    response,
                    "response must not be null");
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