package com.sparrowx.internal.grpc.policies;

import com.sparrowx.internal.exceptions.InternalPermissionDeniedException;
import io.grpc.Metadata;
import org.springframework.stereotype.Component;

@Component
public class InternalAccessPolicy {

    private static final Metadata.Key<String> TENANT_ID_HEADER =
            Metadata.Key.of("x-tenant-id", Metadata.ASCII_STRING_MARSHALLER);

    private static final Metadata.Key<String> ACTOR_ID_HEADER =
            Metadata.Key.of("x-actor-id", Metadata.ASCII_STRING_MARSHALLER);

    public void assertAllowed(
            String methodName,
            Metadata headers
    ) {
        if (methodName == null || methodName.isBlank()) {
            throw new InternalPermissionDeniedException("gRPC method is required");
        }

        /*
         * Current policy:
         * - public service methods require tenant + actor context
         * - detailed role/permission resolution can later use PermissionResolutionPolicy
         * - graph reads stay read-only by design
         */
        var tenantId = headers.get(TENANT_ID_HEADER);
        var actorId = headers.get(ACTOR_ID_HEADER);

        if (tenantId == null || tenantId.isBlank()) {
            throw new InternalPermissionDeniedException("tenant context is required");
        }

        if (actorId == null || actorId.isBlank()) {
            throw new InternalPermissionDeniedException("actor context is required");
        }
    }
}