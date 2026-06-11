package com.sparrowx.apigateway.features.agentic;

public record AgenticRequestDto(
        String userId,
        String prompt
        // String context;
        //Integer maxTokens,
        //Double temperature
) {}
