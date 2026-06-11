package com.sparrowx.apigateway.features.agentic;

import com.sparrowx.apigateway.features.agentic.query.AgenticQuery;
import com.sparrowx.agentic.contracts.AgenticRequest;
import com.sparrowx.agentic.contracts.AgenticResponse;
import org.springframework.stereotype.Component;

@Component
public class AgenticGatewayMapper {

    public AgenticQuery toQuery(AgenticRequestDto requestDto) {
        return new AgenticQuery(
                requestDto.userId(),
                requestDto.prompt()
        );
    }

    public AgenticRequest toGrpcRequest(AgenticQuery query) {
        return AgenticRequest.newBuilder()
                .setUserId(query.userId())
                .setPrompt(query.prompt())
                .build();
    }

    public AgenticResultDto toResultDto(AgenticResponse response) {
        return new AgenticResultDto(
                response.getOutput(),
                response.getSuccess(),
                response.getError()
        );
    }
}