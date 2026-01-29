package buildingblocks.infrastructure.grpc.interceptors;

import io.grpc.*;
import buildingblocks.shared.context.AuthContext;
import org.springframework.stereotype.Component;

@Component
public class GrpcAuthInterceptor implements ServerInterceptor {

    public static final Context.Key<AuthContext> REQUEST_CTX_KEY = Context.key("requestContext");

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {

        // Extract headers forwarded by Linkerd / API Gateway / Keycloak
        String userId = headers.get(Metadata.Key.of("x-user-id", Metadata.ASCII_STRING_MARSHALLER));
        String tenantId = headers.get(Metadata.Key.of("x-tenant-id", Metadata.ASCII_STRING_MARSHALLER));
        String rolesHeader  = headers.get(Metadata.Key.of("x-roles", Metadata.ASCII_STRING_MARSHALLER));

        AuthContext authContext = AuthContext.builder()
                .userId(userId)
                .tenantId(tenantId)
                .build();

        if (rolesHeader != null && !rolesHeader.isEmpty()) {
            for (String role : rolesHeader.split(",")) {
                authContext.addRole(role.trim());
            }
        }

        // Store RequestContext in gRPC Context so it can be retrieved downstream
        Context grpcContext = Context.current().withValue(REQUEST_CTX_KEY, authContext);

        return Contexts.interceptCall(grpcContext, call, headers, next);
    }
}
