package com.sparrowx.apigateway.dtos;

public record AgenticResultDto(
        String output,
        boolean success,
        String errorMessage
) {}
