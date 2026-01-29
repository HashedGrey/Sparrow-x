package com.sparrowx.apigateway.mappers;

import com.sparrowx.apigateway.dtos.AgenticRequestDto;
import com.sparrowx.apigateway.dtos.AgenticResultDto;
import com.sparrowx.apigateway.features.agentic.query.AgenticQuery;
import com.sparrowx.apigateway.proto.stubs.AgenticRequest;
import com.sparrowx.apigateway.proto.stubs.AgenticResponse;
import org.springframework.stereotype.Component;

@Component
public class AgenticGatewayMapper {

    public AgenticQuery toQuery(AgenticRequestDto agenticRequestDto) {
        return new AgenticQuery(
                agenticRequestDto.userId(),
                agenticRequestDto.prompt()
        );
    }


    public AgenticRequest toGrpcRequest(AgenticQuery agenticQuery) {
        return AgenticRequest.newBuilder()
                .setUserId(agenticQuery.userId())
                .setPrompt(agenticQuery.prompt())
                .build();
    }


    public AgenticResultDto toResultDto(AgenticResponse agenticResponse) {
        return new AgenticResultDto(
                agenticResponse.getOutput(),
                agenticResponse.getSuccess(),
                agenticResponse.getError()
        );
    }

    public AgenticQuery toAgenticQuery(AgenticRequestDto requestDto) {
        return new AgenticQuery(
                requestDto.userId(),
                requestDto.prompt()
        );
    }

}
