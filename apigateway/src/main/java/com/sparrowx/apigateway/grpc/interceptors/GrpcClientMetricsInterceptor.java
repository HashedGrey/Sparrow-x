package com.sparrowx.apigateway.grpc.interceptors;

import buildingblocks.infrastructure.observability.MetricRegistryUtil;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall;
import io.grpc.ForwardingClientCallListener;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.Status;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Component
public class GrpcClientMetricsInterceptor implements ClientInterceptor {

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
            MethodDescriptor<ReqT, RespT> method,
            CallOptions callOptions,
            Channel next) {

        String fullMethod = method.getFullMethodName();

        return new ForwardingClientCall.SimpleForwardingClientCall<>(
                next.newCall(method, callOptions)
        ) {
            private Instant start;

            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                start = Instant.now();

                MetricRegistryUtil.counter(
                        "grpc.client.requests",
                        "method", fullMethod
                ).increment();

                super.start(
                        new ForwardingClientCallListener.SimpleForwardingClientCallListener<>(responseListener) {
                            @Override
                            public void onClose(Status status, Metadata trailers) {
                                Duration duration = Duration.between(start, Instant.now());

                                Timer.builder("grpc.client.latency")
                                        .tag("method", fullMethod)
                                        .tag("status", status.getCode().name())
                                        .register(MetricRegistryUtil.registry())
                                        .record(duration);

                                MetricRegistryUtil.counter(
                                        "grpc.client.responses",
                                        "method", fullMethod,
                                        "status", status.getCode().name()
                                ).increment();

                                super.onClose(status, trailers);
                            }
                        },
                        headers
                );
            }
        };
    }
}