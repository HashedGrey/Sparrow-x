package buildingblocks.infrastructure.grpc.interceptors;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
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
                runGuarded(call, () -> delegate.onMessage(message), policy);
            }

            @Override
            public void onHalfClose() {
                runGuarded(call, delegate::onHalfClose, policy);
            }
        };
    }

    private void runGuarded(
            ServerCall<?, ?> call,
            Runnable target,
            ResiliencePolicy policy
    ) {
        try {
            Runnable guarded = target;

            RateLimiter rateLimiter = policy.getRateLimiter();
            if (rateLimiter != null) {
                guarded = RateLimiter.decorateRunnable(rateLimiter, guarded);
            }

            Bulkhead bulkhead = policy.getBulkhead();
            if (bulkhead != null) {
                guarded = Bulkhead.decorateRunnable(bulkhead, guarded);
            }

            CircuitBreaker circuitBreaker = policy.getCircuitBreaker();
            if (circuitBreaker != null) {
                guarded = CircuitBreaker.decorateRunnable(circuitBreaker, guarded);
            }

            guarded.run();

        } catch (CallNotPermittedException ex) {
            call.close(
                    Status.UNAVAILABLE
                            .withDescription("Circuit breaker is open")
                            .withCause(ex),
                    new Metadata()
            );

        } catch (RequestNotPermitted | BulkheadFullException ex) {
            call.close(
                    Status.RESOURCE_EXHAUSTED
                            .withDescription("Rejected by resilience policy")
                            .withCause(ex),
                    new Metadata()
            );

        } catch (Exception ex) {
            call.close(
                    Status.INTERNAL
                            .withDescription("Unexpected resilience interceptor failure")
                            .withCause(ex),
                    new Metadata()
            );
        }
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