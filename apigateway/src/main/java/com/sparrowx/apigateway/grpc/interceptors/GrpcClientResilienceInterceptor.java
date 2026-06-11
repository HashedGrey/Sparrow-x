package com.sparrowx.apigateway.grpc.interceptors;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.Status;
import org.springframework.stereotype.Component;

@Component
public class GrpcClientResilienceInterceptor implements ClientInterceptor {

    private final ResiliencePolicy policy;

    public GrpcClientResilienceInterceptor(ResiliencePolicy policy) {
        this.policy = policy;
    }

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
            MethodDescriptor<ReqT, RespT> method,
            CallOptions callOptions,
            Channel next
    ) {
        return new ForwardingClientCall.SimpleForwardingClientCall<>(
                next.newCall(method, callOptions)
        ) {
            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                try {
                    if (!policy.getRateLimiter().acquirePermission()) {
                        responseListener.onClose(
                                Status.RESOURCE_EXHAUSTED
                                        .withDescription("Rejected by client rate limiter"),
                                new Metadata()
                        );
                        return;
                    }

                    if (!policy.getBulkhead().tryAcquirePermission()) {
                        responseListener.onClose(
                                Status.RESOURCE_EXHAUSTED
                                        .withDescription("Rejected by client bulkhead"),
                                new Metadata()
                        );
                        return;
                    }

                    if (!policy.getCircuitBreaker().tryAcquirePermission()) {
                        responseListener.onClose(
                                Status.UNAVAILABLE
                                        .withDescription("Rejected by client circuit breaker"),
                                new Metadata()
                        );
                        return;
                    }

                    Listener<RespT> guardedListener = new ForwardingClientCallListenerAdapter<>(
                            responseListener,
                            policy.getCircuitBreaker(),
                            policy.getBulkhead()
                    );

                    super.start(guardedListener, headers);

                } catch (RequestNotPermitted ex) {
                    responseListener.onClose(
                            Status.RESOURCE_EXHAUSTED
                                    .withDescription("Rejected by client rate limiter")
                                    .withCause(ex),
                            new Metadata()
                    );
                } catch (BulkheadFullException ex) {
                    responseListener.onClose(
                            Status.RESOURCE_EXHAUSTED
                                    .withDescription("Rejected by client bulkhead")
                                    .withCause(ex),
                            new Metadata()
                    );
                } catch (CallNotPermittedException ex) {
                    responseListener.onClose(
                            Status.UNAVAILABLE
                                    .withDescription("Rejected by client circuit breaker")
                                    .withCause(ex),
                            new Metadata()
                    );
                } catch (Exception ex) {
                    responseListener.onClose(
                            Status.UNAVAILABLE
                                    .withDescription("Client resilience interception failed")
                                    .withCause(ex),
                            new Metadata()
                    );
                }
            }
        };
    }

    private static class ForwardingClientCallListenerAdapter<RespT>
            extends io.grpc.ForwardingClientCallListener.SimpleForwardingClientCallListener<RespT> {

        private final CircuitBreaker circuitBreaker;
        private final Bulkhead bulkhead;

        protected ForwardingClientCallListenerAdapter(
                ClientCall.Listener<RespT> delegate,
                CircuitBreaker circuitBreaker,
                Bulkhead bulkhead
        ) {
            super(delegate);
            this.circuitBreaker = circuitBreaker;
            this.bulkhead = bulkhead;
        }

        @Override
        public void onClose(Status status, Metadata trailers) {
            try {
                if (status.isOk()) {
                    circuitBreaker.onSuccess(0, java.util.concurrent.TimeUnit.NANOSECONDS);
                } else {
                    circuitBreaker.onError(
                            0,
                            java.util.concurrent.TimeUnit.NANOSECONDS,
                            status.asRuntimeException(trailers)
                    );
                }
            } finally {
                bulkhead.releasePermission();
            }

            super.onClose(status, trailers);
        }
    }

    public interface ResiliencePolicy {
        CircuitBreaker getCircuitBreaker();
        Bulkhead getBulkhead();
        RateLimiter getRateLimiter();
    }
}