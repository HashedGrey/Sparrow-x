package com.sparrowx.profile.grpc.policies;

import buildingblocks.infrastructure.grpc.interceptors.GrpcResilienceInterceptor.ResiliencePolicy;
import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class ProfileResiliencePolicy implements ResiliencePolicy {

    private final CircuitBreaker circuitBreaker;
    private final Bulkhead bulkhead;
    private final RateLimiter rateLimiter;
    private final Retry retry;
    private final TimeLimiter timeLimiter;

    public ProfileResiliencePolicy() {
        this.circuitBreaker = CircuitBreaker.of("profileCircuitBreaker", CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .waitDurationInOpenState(Duration.ofSeconds(10))
                .slidingWindowSize(20)
                .build());

        this.bulkhead = Bulkhead.of("profileBulkhead", BulkheadConfig.custom()
                .maxConcurrentCalls(50)
                .build());

        this.rateLimiter = RateLimiter.of("profileRateLimiter", RateLimiterConfig.custom()
                .limitRefreshPeriod(Duration.ofSeconds(1))
                .limitForPeriod(100)
                .timeoutDuration(Duration.ofMillis(0))
                .build());

        this.retry = Retry.of("profileRetry", RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofMillis(200))
                .build());

        this.timeLimiter = TimeLimiter.of("profileTimeLimiter", TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(2))
                .build());
    }

    @Override
    public CircuitBreaker getCircuitBreaker() { return circuitBreaker; }

    @Override
    public Bulkhead getBulkhead() { return bulkhead; }

    @Override
    public RateLimiter getRateLimiter() { return rateLimiter; }

    @Override
    public Retry getRetry() { return retry; }

    @Override
    public TimeLimiter getTimeLimiter() { return timeLimiter; }
}
