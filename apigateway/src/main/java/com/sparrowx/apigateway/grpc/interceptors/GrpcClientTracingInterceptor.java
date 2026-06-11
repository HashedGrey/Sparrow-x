package com.sparrowx.apigateway.grpc.interceptors;

import buildingblocks.shared.context.CorrelationContext;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import org.springframework.stereotype.Component;

@Component
public class GrpcClientTracingInterceptor implements ClientInterceptor {

    private static final Metadata.Key<String> CORRELATION_ID_HEADER =
            Metadata.Key.of("x-correlation-id", Metadata.ASCII_STRING_MARSHALLER);

    private static final Metadata.Key<String> TRACE_ID_HEADER =
            Metadata.Key.of("x-trace-id", Metadata.ASCII_STRING_MARSHALLER);

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
            MethodDescriptor<ReqT, RespT> method,
            CallOptions callOptions,
            Channel next) {

        return new ForwardingClientCall.SimpleForwardingClientCall<>(
                next.newCall(method, callOptions)
        ) {
            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                String correlationId = CorrelationContext.getCorrelationId();
                String traceId = CorrelationContext.getTraceId();

                if (correlationId != null && !correlationId.isBlank()) {
                    headers.put(CORRELATION_ID_HEADER, correlationId);
                }

                if (traceId != null && !traceId.isBlank()) {
                    headers.put(TRACE_ID_HEADER, traceId);
                }

                super.start(responseListener, headers);
            }
        };
    }
}