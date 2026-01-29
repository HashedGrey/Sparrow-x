package com.sparrowx.apigateway.dtos;

public record AgenticRequestDto(
        String userId,
        String prompt
        // String context;
        //Integer maxTokens,
        //Double temperature
) {}
