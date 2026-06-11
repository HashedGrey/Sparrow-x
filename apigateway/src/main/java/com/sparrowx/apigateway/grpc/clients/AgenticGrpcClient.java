package com.sparrowx.apigateway.grpc.clients;

import com.sparrowx.agentic.contracts.AgenticRequest;
import com.sparrowx.agentic.contracts.AgenticResponse;
import com.sparrowx.agentic.contracts.AgenticServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.stereotype.Component;

@Component
public class AgenticGrpcClient {

    private final AgenticServiceGrpc.AgenticServiceBlockingStub stub;

    public AgenticGrpcClient() {
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("agentic-service", 6565)
                .usePlaintext()
                .build();

        stub = AgenticServiceGrpc.newBlockingStub(channel);
    }

    public AgenticResponse send(AgenticRequest request) {
        return stub.execute(request);
    }
}