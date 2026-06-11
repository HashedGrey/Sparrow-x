package com.sparrowx.apigateway.features.agentic;

public record AgenticResultDto(
        String output,
        boolean success,
        String errorMessage
) {}
