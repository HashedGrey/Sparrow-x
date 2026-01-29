package com.sparrowx.apigateway.grpc.stubs;

import com.sparrowx.apigateway.proto.stubs.AgenticRequest;
import com.sparrowx.apigateway.proto.stubs.AgenticResponse;
import com.sparrowx.apigateway.proto.stubs.AgenticServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.stereotype.Component;

@Component
public class AgenticGrpcClient {

    private final AgenticServiceGrpc.AgenticServiceBlockingStub stub;

    public AgenticGrpcClient() {
        // TODO: externalize host/port to properties
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
