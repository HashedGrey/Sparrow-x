package buildingblocks.infrastructure.grpc.interceptors;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.decorators.Decorators;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.grpc.*;

public class GrpcResilienceInterceptor implements ServerInterceptor {

    private final ResiliencePolicyResolver policyResolver;

    public GrpcResilienceInterceptor(ResiliencePolicyResolver policyResolver) {
        this.policyResolver = policyResolver;
    }

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next
    ) {

        ResiliencePolicy policy =
                policyResolver.resolve(call.getMethodDescriptor().getFullMethodName());

        ServerCall.Listener<ReqT> delegate = next.startCall(call, headers);

        return new ForwardingServerCallListener
                .SimpleForwardingServerCallListener<>(delegate) {

            @Override
            public void onMessage(ReqT message) {
                try {
                    Runnable guarded =
                            Decorators.ofRunnable(() -> delegate.onMessage(message))
                                    .withCircuitBreaker(policy.getCircuitBreaker())
                                    .withBulkhead(policy.getBulkhead())
                                    .withRateLimiter(policy.getRateLimiter())
                                    .decorate();

                    guarded.run();

                } catch (Exception ex) {
                    call.close(
                            Status.RESOURCE_EXHAUSTED
                                    .withDescription("Rejected by resilience policy")
                                    .withCause(ex),
                            new Metadata()
                    );
                }
            }
        };
    }



    public interface ResiliencePolicyResolver {
        ResiliencePolicy resolve(String fullMethodName);
    }

    public interface ResiliencePolicy {
        CircuitBreaker getCircuitBreaker();
        Bulkhead getBulkhead();
        RateLimiter getRateLimiter();
    }
}
