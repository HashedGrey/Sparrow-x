package com.sparrowx.agentic.grpc;

import com.sparrowx.agentic.engine.AgentEngine;
import com.sparrowx.agentic.engine.ExecutionContext;
import com.sparrowx.agentic.proto.stubs.AgenticRequest;
import com.sparrowx.agentic.proto.stubs.AgenticResponse;
import com.sparrowx.agentic.proto.stubs.AgenticServiceGrpc;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AgenticServiceGrpcImpl
        extends AgenticServiceGrpc.AgenticServiceImplBase {

    private final AgentEngine agentEngine;

    @Override
    public void execute(
            AgenticRequest request,
            StreamObserver<AgenticResponse> responseObserver
    ) {

        log.info(
                "Received agentic mission [userId={}, promptLength={}]",
                request.getUserId(),
                request.getPrompt().length()
        );

        try {
            // 1. Bootstrap execution context (traceId, budgets, memory handles)
            ExecutionContext context = ExecutionContext.from(request.getUserId());

            // 2. Execute full agentic pipeline
            String result = agentEngine.execute(
                    request.getPrompt(),
                    context
            );

            // 3. Build successful response
            AgenticResponse response = AgenticResponse.newBuilder()
                    .setOutput(result)
                    .setSuccess(true)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception ex) {
            log.error("Agentic mission failed", ex);

            AgenticResponse errorResponse = AgenticResponse.newBuilder()
                    .setSuccess(false)
                    .setError(ex.getMessage())
                    .build();

            responseObserver.onNext(errorResponse);
            responseObserver.onCompleted();
        }
    }
}
