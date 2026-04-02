package com.sparrowx.tweet.grpc;

import buildingblocks.infrastructure.cache.RedisCacheProvider;
import buildingblocks.infrastructure.grpc.interceptors.*;

import com.sparrowx.tweet.grpc.policies.TweetCachePolicy;
import com.sparrowx.tweet.grpc.policies.TweetResiliencePolicy;

import net.devh.boot.grpc.server.serverfactory.GrpcServerConfigurer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class GrpcServerConfig {

    //private final GrpcAuthInterceptor grpcAuthInterceptor;
    private final GrpcLoggingInterceptor grpcLoggingInterceptor;
    private final RedisCacheProvider redisCacheProvider;
    private final GrpcMetricsInterceptor grpcMetricsInterceptor;
    private final GrpcTracingInterceptor grpcTracingInterceptor;
    private final GrpcDebugInterceptor grpcDebugInterceptor;

    public GrpcServerConfig(
            GrpcAuthInterceptor grpcAuthInterceptor,
            RedisCacheProvider redisCacheProvider,
            GrpcLoggingInterceptor grpcLoggingInterceptor,
            GrpcMetricsInterceptor grpcMetricsInterceptor,
            GrpcTracingInterceptor grpcTracingInterceptor,
            GrpcDebugInterceptor grpcDebugInterceptor
    ) {
        //this.grpcAuthInterceptor = grpcAuthInterceptor;
        this.redisCacheProvider = redisCacheProvider;
        this.grpcLoggingInterceptor = grpcLoggingInterceptor;
        this.grpcMetricsInterceptor = grpcMetricsInterceptor;
        this.grpcTracingInterceptor = grpcTracingInterceptor;
        this.grpcDebugInterceptor = grpcDebugInterceptor;
    }

    @Bean
    public GrpcCachingInterceptor grpcCachingInterceptor() {

        TweetCachePolicy policy = new TweetCachePolicy(
                redisCacheProvider,
                Duration.ofSeconds(30),
                "tweet:"
        );

        return new GrpcCachingInterceptor(policy);
    }

    @Bean
    public GrpcResilienceInterceptor.ResiliencePolicyResolver tweetResiliencePolicyResolver(
            TweetResiliencePolicy policy
    ) {
        return fullMethodName -> policy;
    }

    @Bean
    public GrpcResilienceInterceptor grpcResilienceInterceptor(
            GrpcResilienceInterceptor.ResiliencePolicyResolver resolver
    ) {
        return new GrpcResilienceInterceptor(resolver);
    }

    @Bean
    public GrpcServerConfigurer grpcServerConfigurer(
            GrpcCachingInterceptor grpcCachingInterceptor,
            GrpcResilienceInterceptor grpcResilienceInterceptor
    ) {

        return serverBuilder -> {

            // 1. SECURITY
            //serverBuilder.intercept(grpcAuthInterceptor);

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