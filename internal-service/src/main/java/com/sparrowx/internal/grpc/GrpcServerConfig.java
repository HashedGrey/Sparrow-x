package com.sparrowx.internal.grpc;

import buildingblocks.infrastructure.grpc.interceptors.GrpcAuthInterceptor;
import buildingblocks.infrastructure.grpc.interceptors.GrpcExceptionInterceptor;
import buildingblocks.infrastructure.grpc.interceptors.GrpcLoggingInterceptor;
import buildingblocks.infrastructure.grpc.interceptors.GrpcMetricsInterceptor;
import buildingblocks.infrastructure.grpc.interceptors.GrpcTracingInterceptor;
import com.sparrowx.internal.grpc.interceptors.GrpcTenantContextInterceptor;
import io.grpc.ServerInterceptor;
import io.opentelemetry.api.trace.Tracer;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.grpc.server.GlobalServerInterceptor;

@Configuration(proxyBeanMethods = false)
public final class GrpcServerConfig {

    @Bean
    @Order(100)
    @GlobalServerInterceptor
    public GrpcTracingInterceptor grpcTracingInterceptor(
            Tracer tracer
    ) {
        return new GrpcTracingInterceptor(tracer);
    }

    @Bean
    @Order(200)
    @GlobalServerInterceptor
    public GrpcExceptionInterceptor grpcExceptionInterceptor() {
        return new GrpcExceptionInterceptor();
    }

    @Bean
    @Order(300)
    @GlobalServerInterceptor
    public GrpcLoggingInterceptor grpcLoggingInterceptor() {
        return new GrpcLoggingInterceptor();
    }

    @Bean
    @Order(400)
    @GlobalServerInterceptor
    public GrpcMetricsInterceptor grpcMetricsInterceptor() {
        return new GrpcMetricsInterceptor();
    }

    @Bean
    @Order(500)
    @GlobalServerInterceptor
    public GrpcAuthInterceptor grpcAuthInterceptor() {
        return new GrpcAuthInterceptor();
    }

    @Bean
    @Order(600)
    @GlobalServerInterceptor
    public GrpcTenantContextInterceptor grpcTenantContextInterceptor() {
        return new GrpcTenantContextInterceptor();
    }

    @Bean
    @Profile("dev")
    public ApplicationRunner grpcInterceptorProbe(
            ApplicationContext context
    ) {
        return args -> {
            System.out.println("=== GRPC INTERCEPTORS ===");

            context.getBeansOfType(ServerInterceptor.class)
                    .forEach((name, bean) ->
                            System.out.println(
                                    name + " -> "
                                            + bean.getClass().getName()
                            )
                    );
        };
    }
}