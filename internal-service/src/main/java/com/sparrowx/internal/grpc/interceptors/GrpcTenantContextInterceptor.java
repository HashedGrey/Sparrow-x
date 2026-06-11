package com.sparrowx.internal.grpc.interceptors;

import io.grpc.ForwardingServerCall;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@GrpcGlobalServerInterceptor
public class GrpcTenantContextInterceptor implements ServerInterceptor {

    private static final Logger log =
            LoggerFactory.getLogger(GrpcTenantContextInterceptor.class);

    private static final Metadata.Key<String> TENANT_ID_HEADER =
            Metadata.Key.of("x-tenant-id", Metadata.ASCII_STRING_MARSHALLER);

    private static final Metadata.Key<String> ACTOR_ID_HEADER =
            Metadata.Key.of("x-actor-id", Metadata.ASCII_STRING_MARSHALLER);

    private static final Metadata.Key<String> REQUEST_ID_HEADER =
            Metadata.Key.of("x-request-id", Metadata.ASCII_STRING_MARSHALLER);

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next
    ) {
        var tenantId = headers.get(TENANT_ID_HEADER);
        var actorId = headers.get(ACTOR_ID_HEADER);
        var requestId = headers.get(REQUEST_ID_HEADER);

        log.debug(
                "Intsvc gRPC request method={} tenantId={} actorId={} requestId={}",
                call.getMethodDescriptor().getFullMethodName(),
                safe(tenantId),
                safe(actorId),
                safe(requestId)
        );

        return next.startCall(
                new ForwardingServerCall.SimpleForwardingServerCall<>(call) {
                },
                headers
        );
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "unknown" : value;
    }
}