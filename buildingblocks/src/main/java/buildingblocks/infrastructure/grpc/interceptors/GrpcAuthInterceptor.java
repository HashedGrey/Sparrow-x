package buildingblocks.infrastructure.grpc.interceptors;

import io.grpc.*;
import buildingblocks.shared.context.AuthContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
public class GrpcAuthInterceptor implements ServerInterceptor {

    public static final Context.Key<AuthContext> REQUEST_CTX_KEY =
            Context.key("requestContext");

    // Internal service-to-service header
    private static final Metadata.Key<String> INTERNAL_HEADER =
            Metadata.Key.of("x-internal-call", Metadata.ASCII_STRING_MARSHALLER);

    private static final Metadata.Key<String> USER_ID_HEADER =
            Metadata.Key.of("x-user-id", Metadata.ASCII_STRING_MARSHALLER);

    private static final Metadata.Key<String> TENANT_ID_HEADER =
            Metadata.Key.of("x-tenant-id", Metadata.ASCII_STRING_MARSHALLER);

    private static final Metadata.Key<String> ROLES_HEADER =
            Metadata.Key.of("x-roles", Metadata.ASCII_STRING_MARSHALLER);

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {

        // Validate internal call header (service-to-service)
        if (headers.get(INTERNAL_HEADER) == null) {
            call.close(Status.UNAUTHENTICATED.withDescription("Missing internal call header"), new Metadata());
            return new ServerCall.Listener<>() {};
        }

        String userId = headers.get(USER_ID_HEADER);
        String tenantId = headers.get(TENANT_ID_HEADER);
        String rolesHeader = headers.get(ROLES_HEADER);

        AuthContext authContext = AuthContext.builder()
                .userId(userId)
                .tenantId(tenantId)
                .build();

        if (rolesHeader != null && !rolesHeader.isEmpty()) {
            for (String role : rolesHeader.split(",")) {
                authContext.addRole(role.trim());
            }
        }

        Context grpcContext = Context.current().withValue(REQUEST_CTX_KEY, authContext);

        return Contexts.interceptCall(grpcContext, call, headers, next);
    }
}