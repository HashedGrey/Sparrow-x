package com.sparrowx.document.grpc.interceptors;

import buildingblocks.infrastructure.grpc.interceptors.GrpcAuthInterceptor;
import buildingblocks.shared.context.AuthContext;
import io.grpc.Context;
import io.grpc.ForwardingServerCallListener;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class GrpcPolicyEnforcementInterceptor implements ServerInterceptor {

    private static final Set<String> READ_METHODS = Set.of(
            "GetDocument",
            "GetIngestionJob",
            "HybridDocumentSearch",
            "VerifyCitation"
    );

    private static final Set<String> WRITE_METHODS = Set.of(
            "UploadDocument"
    );

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next
    ) {
        String fullMethodName = call.getMethodDescriptor().getFullMethodName();
        String methodName = extractMethodName(fullMethodName);

        AuthContext authContext = GrpcAuthInterceptor.REQUEST_CTX_KEY.get();

        if (!isAllowed(methodName, authContext)) {
            call.close(
                    Status.PERMISSION_DENIED
                            .withDescription("Access denied for gRPC method: " + fullMethodName),
                    new Metadata()
            );

            return new ServerCall.Listener<>() {
            };
        }

        ServerCall.Listener<ReqT> listener = next.startCall(call, headers);

        return new ForwardingServerCallListener.SimpleForwardingServerCallListener<>(listener) {
        };
    }

    private boolean isAllowed(
            String methodName,
            AuthContext authContext
    ) {
        if (methodName == null || methodName.isBlank()) {
            return false;
        }

        if (authContext == null) {
            return false;
        }

        if (READ_METHODS.contains(methodName)) {
            return true;
        }

        if (WRITE_METHODS.contains(methodName)) {
            return true;
        }

        return false;
    }

    private String extractMethodName(String fullMethodName) {
        if (fullMethodName == null || fullMethodName.isBlank()) {
            return "";
        }

        int slashIndex = fullMethodName.lastIndexOf('/');
        if (slashIndex < 0 || slashIndex == fullMethodName.length() - 1) {
            return fullMethodName;
        }

        return fullMethodName.substring(slashIndex + 1);
    }
}