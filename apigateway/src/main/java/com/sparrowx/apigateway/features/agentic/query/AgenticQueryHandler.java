package com.sparrowx.apigateway.features.agentic.query;

import buildingblocks.core.queries.QueryHandler;
import com.sparrowx.apigateway.dtos.AgenticResultDto;
import com.sparrowx.apigateway.grpc.stubs.AgenticGrpcClient;
import com.sparrowx.apigateway.mappers.AgenticGatewayMapper;
import com.sparrowx.apigateway.proto.stubs.AgenticRequest;
import com.sparrowx.apigateway.proto.stubs.AgenticResponse;
import org.springframework.stereotype.Component;

@Component
public class AgenticQueryHandler implements QueryHandler<AgenticQuery, AgenticResultDto> {

    private final AgenticGrpcClient grpcClient;
    private final AgenticGatewayMapper agenticGatewayMapper;

    public AgenticQueryHandler(AgenticGrpcClient grpcClient, AgenticGatewayMapper agenticGatewayMapper) {
        this.grpcClient = grpcClient;
        this.agenticGatewayMapper = agenticGatewayMapper;
    }

    @Override
    public AgenticResultDto handle(AgenticQuery query) {

        // 1️⃣ Map internal Query -> gRPC request
        AgenticRequest grpcRequest = agenticGatewayMapper.toGrpcRequest(query);

        // 2️⃣ Send request to Agentic service (Milvus + Embabel + BGE + ChatGPT happens inside)
        AgenticResponse grpcResponse = grpcClient.send(grpcRequest);

        // 3️⃣ Map gRPC response -> REST DTO
        return agenticGatewayMapper.toResultDto(grpcResponse);
    }
}
