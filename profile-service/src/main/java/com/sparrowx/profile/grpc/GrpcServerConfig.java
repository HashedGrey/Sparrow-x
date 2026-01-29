package com.sparrowx.profile.grpc;

import buildingblocks.infrastructure.cache.RedisCacheProvider;
import buildingblocks.infrastructure.grpc.interceptors.*;
import com.sparrowx.profile.grpc.policies.ProfileCachePolicy;
import com.sparrowx.profile.grpc.policies.ProfileResiliencePolicy;
import net.devh.boot.grpc.server.serverfactory.GrpcServerConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class GrpcServerConfig {

    private final GrpcAuthInterceptor grpcAuthInterceptor;
    private final GrpcLoggingInterceptor grpcLoggingInterceptor;
    private final RedisCacheProvider redisCacheProvider;
    private final GrpcMetricsInterceptor grpcMetricsInterceptor;
    private final GrpcTracingInterceptor grpcTracingInterceptor;
    private final GrpcDebugInterceptor grpcDebugInterceptor;

    public GrpcServerConfig(GrpcAuthInterceptor grpcAuthInterceptor,
                            RedisCacheProvider redisCacheProvider,
                            GrpcLoggingInterceptor grpcLoggingInterceptor,
                            GrpcMetricsInterceptor grpcMetricsInterceptor,
                            GrpcTracingInterceptor grpcTracingInterceptor,
                            GrpcDebugInterceptor grpcDebugInterceptor) {
        this.grpcAuthInterceptor = grpcAuthInterceptor;
        this.redisCacheProvider = redisCacheProvider;
        this.grpcLoggingInterceptor = grpcLoggingInterceptor;
        this.grpcMetricsInterceptor = grpcMetricsInterceptor;
        this.grpcTracingInterceptor = grpcTracingInterceptor;
        this.grpcDebugInterceptor = grpcDebugInterceptor;
    }

    @Bean
    public GrpcCachingInterceptor grpcCachingInterceptor() {
        ProfileCachePolicy policy = new ProfileCachePolicy(
                redisCacheProvider,
                Duration.ofSeconds(60),  // TTL for Profile service
                "profile:"               // Key prefix
        );
        return new GrpcCachingInterceptor(policy);
    }

    @Bean
    public GrpcResilienceInterceptor grpcResilienceInterceptor(ProfileResiliencePolicy policy) {
        return new GrpcResilienceInterceptor(policy);
    }

    @Bean
    public GrpcServerConfigurer grpcServerConfigurer(GrpcCachingInterceptor grpcCachingInterceptor,
                                                     GrpcResilienceInterceptor grpcResilienceInterceptor) {
        return serverBuilder -> {
            // 1. SECURITY
            serverBuilder.intercept(grpcAuthInterceptor);

            // 2. OBSERVABILITY
            serverBuilder.intercept(grpcTracingInterceptor);
            serverBuilder.intercept(grpcMetricsInterceptor);
            serverBuilder.intercept(grpcLoggingInterceptor);

            // 3. FEATURE TOGGLES
            serverBuilder.intercept(grpcDebugInterceptor);

            // 4. CACHE
            serverBuilder.intercept(grpcCachingInterceptor);

            // 5. RESILIENCE
            serverBuilder.intercept(grpcResilienceInterceptor);
        };
    }
}
