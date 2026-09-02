package com.sparrowx.agentic.adapters.document;

import com.sparrowx.agentic.mission.model.MissionContext;
import com.sparrowx.document.proto.RequestContext;
import io.grpc.Metadata;

import java.util.Objects;

public final class DocumentClientMapper {

    private static final String AGENTIC_SERVICE = "agentic-service";

    private static final Metadata.Key<String> INTERNAL_CALL_HEADER =
            Metadata.Key.of("x-internal-call", Metadata.ASCII_STRING_MARSHALLER);

    private static final Metadata.Key<String> USER_ID_HEADER =
            Metadata.Key.of("x-user-id", Metadata.ASCII_STRING_MARSHALLER);

    private static final Metadata.Key<String> TENANT_ID_HEADER =
            Metadata.Key.of("x-tenant-id", Metadata.ASCII_STRING_MARSHALLER);

    private static final Metadata.Key<String> ROLES_HEADER =
            Metadata.Key.of("x-roles", Metadata.ASCII_STRING_MARSHALLER);
    private static final Metadata.Key<String> REQUEST_ID_HEADER =
            Metadata.Key.of("x-sparrowx-request-id", Metadata.ASCII_STRING_MARSHALLER);

    private static final Metadata.Key<String> PROJECT_ID_HEADER =
            Metadata.Key.of("x-sparrowx-project-id", Metadata.ASCII_STRING_MARSHALLER);
    private static final Metadata.Key<String> TEAM_ID_HEADER =
            Metadata.Key.of("x-sparrowx-team-id", Metadata.ASCII_STRING_MARSHALLER);
    private static final Metadata.Key<String> TRACE_ID_HEADER =
            Metadata.Key.of("x-sparrowx-trace-id", Metadata.ASCII_STRING_MARSHALLER);
    private static final Metadata.Key<String> CALLER_SERVICE_HEADER =
            Metadata.Key.of("x-sparrowx-caller-service", Metadata.ASCII_STRING_MARSHALLER);

    public RequestContext toRequestContext(
            MissionContext context,
            String requestId
    ) {
        Objects.requireNonNull(context, "context must not be null");
        requireTenant(context.tenantId());
        requestId = requireText(requestId, "requestId");

        return RequestContext.newBuilder()
                .setRequestId(requestId)
                .setTenantId(context.tenantId())
                .setUserId(context.userId())
                .setProjectId(context.projectId())
                .setTeamId(context.teamId())
                .setTraceId(context.traceId())
                .setCallerService(callerService(context))
                .build();
    }

    public RequestContext toRequestContext(MissionContext context) {
        Objects.requireNonNull(context, "context must not be null");
        return toRequestContext(context, context.requestId());
    }

    public Metadata toMetadata(MissionContext context, String requestId) {
        Objects.requireNonNull(context, "context must not be null");
        requireTenant(context.tenantId());
        requestId = requireText(requestId, "requestId");

        Metadata metadata = new Metadata();

        metadata.put(INTERNAL_CALL_HEADER, "true");
        metadata.put(TENANT_ID_HEADER, context.tenantId());

        putIfPresent(metadata, USER_ID_HEADER, context.userId());
        metadata.put(ROLES_HEADER, "USER");

        putIfPresent(metadata, REQUEST_ID_HEADER, requestId);
        putIfPresent(metadata, PROJECT_ID_HEADER, context.projectId());
        putIfPresent(metadata, TEAM_ID_HEADER, context.teamId());
        putIfPresent(metadata, TRACE_ID_HEADER, context.traceId());
        putIfPresent(metadata, CALLER_SERVICE_HEADER, AGENTIC_SERVICE);

        return metadata;
    }

    public Metadata toMetadata(MissionContext context) {
        return toMetadata(context, context.requestId());
    }


    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value;
    }



    private static String callerService(MissionContext context) {
        return  AGENTIC_SERVICE;
    }

    private static void requireTenant(String tenantId) {
        if (tenantId.isBlank()) {
            throw new IllegalArgumentException("tenantId must not be blank");
        }
    }

    private static void putIfPresent(Metadata metadata, Metadata.Key<String> key, String value) {
        if (!value.isBlank()) {
            metadata.put(key, value);
        }
    }
}
