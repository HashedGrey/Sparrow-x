package buildingblocks.infrastructure.grpc.interceptors;

import io.grpc.Context;
import io.grpc.Contexts;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;

import java.util.Locale;
import java.util.Objects;

/**
 * Shared tenant extraction and gRPC context propagation.
 *
 * Services provide only the tenant authorization policy.
 */
public final class GrpcTenantInterceptor implements ServerInterceptor {

    public static final Context.Key<String> TENANT_ID_CONTEXT_KEY =
            Context.key("grpc-tenant-id");

    private final TenantPolicy policy;
    private final Metadata.Key<String> tenantHeader;

    public GrpcTenantInterceptor(TenantPolicy policy) {
        this.policy = Objects.requireNonNull(
                policy,
                "policy must not be null"
        );

        String headerName = requireHeaderName(
                policy.tenantHeaderName()
        );

        this.tenantHeader = Metadata.Key.of(
                headerName,
                Metadata.ASCII_STRING_MARSHALLER
        );
    }

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next
    ) {
        String tenantId = normalize(headers.get(tenantHeader));

        if (tenantId.isEmpty()) {
            return reject(
                    call,
                    Status.INVALID_ARGUMENT.withDescription(
                            tenantHeader.name() + " is required"
                    )
            );
        }

        TenantDecision decision = Objects.requireNonNull(
                policy.authorize(tenantId, call, headers),
                "tenant policy decision must not be null"
        );

        if (!decision.allowed()) {
            return reject(
                    call,
                    decision.rejectionStatus()
            );
        }

        Context context = Context.current()
                .withValue(
                        TENANT_ID_CONTEXT_KEY,
                        tenantId
                );

        return Contexts.interceptCall(
                context,
                call,
                headers,
                next
        );
    }

    public static String currentTenantId() {
        String tenantId = currentTenantIdOrNull();

        if (tenantId == null) {
            throw new SecurityException(
                    "Tenant context is unavailable"
            );
        }

        return tenantId;
    }

    public static String currentTenantIdOrNull() {
        String tenantId = TENANT_ID_CONTEXT_KEY.get();

        return tenantId == null || tenantId.isBlank()
                ? null
                : tenantId;
    }

    private static String requireHeaderName(String value) {
        String headerName = normalize(value)
                .toLowerCase(Locale.ROOT);

        if (headerName.isEmpty()) {
            throw new IllegalArgumentException(
                    "tenant header name must not be blank"
            );
        }

        return headerName;
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private static <T> ServerCall.Listener<T> reject(
            ServerCall<?, ?> call,
            Status status
    ) {
        Status rejection = status == null || status.isOk()
                ? Status.PERMISSION_DENIED.withDescription(
                "Tenant access denied"
        )
                : status;

        call.close(rejection, new Metadata());

        return new ServerCall.Listener<>() {
        };
    }

    public interface TenantPolicy {

        String tenantHeaderName();

        TenantDecision authorize(
                String tenantId,
                ServerCall<?, ?> call,
                Metadata headers
        );
    }

    public record TenantDecision(
            boolean allowed,
            Status rejectionStatus
    ) {

        public TenantDecision {
            if (allowed) {
                rejectionStatus = null;
            } else if (rejectionStatus == null
                    || rejectionStatus.isOk()) {
                rejectionStatus =
                        Status.PERMISSION_DENIED.withDescription(
                                "Tenant access denied"
                        );
            }
        }

        public static TenantDecision allow() {
            return new TenantDecision(true, null);
        }

        public static TenantDecision deny(Status status) {
            return new TenantDecision(false, status);
        }
    }
}