package com.sparrowx.apigateway.grpc.interceptors;

import buildingblocks.shared.context.AuthContext;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import org.springframework.stereotype.Component;

@Component
public class GrpcClientAuthInterceptor implements ClientInterceptor {

    private static final Metadata.Key<String> USER_ID_HEADER =
            Metadata.Key.of("x-user-id", Metadata.ASCII_STRING_MARSHALLER);

    private static final Metadata.Key<String> TENANT_ID_HEADER =
            Metadata.Key.of("x-tenant-id", Metadata.ASCII_STRING_MARSHALLER);

    private static final Metadata.Key<String> ROLES_HEADER =
            Metadata.Key.of("x-roles", Metadata.ASCII_STRING_MARSHALLER);

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
                AuthContext authContext = AuthContext.get();

                if (authContext != null) {
                    if (authContext.getUserId() != null && !authContext.getUserId().isBlank()) {
                        headers.put(USER_ID_HEADER, authContext.getUserId());
                    }

                    if (authContext.getTenantId() != null && !authContext.getTenantId().isBlank()) {
                        headers.put(TENANT_ID_HEADER, authContext.getTenantId());
                    }

                    if (authContext.getRoles() != null && !authContext.getRoles().isEmpty()) {
                        headers.put(ROLES_HEADER, String.join(",", authContext.getRoles()));
                    }
                }

                super.start(responseListener, headers);
            }
        };
    }
}