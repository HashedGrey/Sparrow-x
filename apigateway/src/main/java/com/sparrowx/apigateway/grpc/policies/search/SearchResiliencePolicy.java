package com.sparrowx.apigateway.grpc.policies.search;

import com.sparrowx.apigateway.grpc.interceptors.GrpcClientResilienceInterceptor.ResiliencePolicy;
import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class SearchResiliencePolicy implements ResiliencePolicy {

    private final CircuitBreaker circuitBreaker;
    private final Bulkhead bulkhead;
    private final RateLimiter rateLimiter;

    public SearchResiliencePolicy() {
        this.circuitBreaker = CircuitBreaker.of(
                "searchClientCircuitBreaker",
                CircuitBreakerConfig.custom()
                        .failureRateThreshold(50)
                        .waitDurationInOpenState(Duration.ofSeconds(10))
                        .slidingWindowSize(20)
                        .minimumNumberOfCalls(10)
                        .build()
        );

        this.bulkhead = Bulkhead.of(
                "searchClientBulkhead",
                BulkheadConfig.custom()
                        .maxConcurrentCalls(50)
                        .maxWaitDuration(Duration.ofMillis(0))
                        .build()
        );

        this.rateLimiter = RateLimiter.of(
                "searchClientRateLimiter",
                RateLimiterConfig.custom()
                        .limitRefreshPeriod(Duration.ofSeconds(1))
                        .limitForPeriod(100)
                        .timeoutDuration(Duration.ofMillis(0))
                        .build()
        );
    }

    @Override
    public CircuitBreaker getCircuitBreaker() {
        return circuitBreaker;
    }

    @Override
    public Bulkhead getBulkhead() {
        return bulkhead;
    }

    @Override
    public RateLimiter getRateLimiter() {
        return rateLimiter;
    }
}