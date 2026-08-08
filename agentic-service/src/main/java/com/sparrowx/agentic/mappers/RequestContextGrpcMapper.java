package com.sparrowx.agentic.mappers;

import buildingblocks.shared.context.AuthContext;
import com.sparrowx.agentic.grpc.interceptors.GrpcTenantContextInterceptor;
import com.sparrowx.agentic.proto.RequestContext;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

@Component
public final class RequestContextGrpcMapper {

    public MissionContext toDomain(RequestContext context) {
        RequestContextView view = toView(context);

        return new MissionContext(
                view.requestId(),
                view.tenantId(),
                view.userId(),
                view.username(),
                view.projectId(),
                view.teamId(),
                view.traceId(),
                view.callerService(),
                view.sessionId(),
                view.conversationId(),
                view.clientChannel(),
                view.metadata()
        );
    }

    public RequestContextView toView(RequestContext context) {
        Objects.requireNonNull(
                context,
                "context must not be null"
        );

        String requestTenantId =
                normalize(context.getTenantId());

        validateTransportTenant(requestTenantId);
        //validateCallerTenant(requestTenantId);

        return new RequestContextView(
                context.getRequestId(),
                requestTenantId,
                context.getUserId(),
                context.getUsername(),
                context.getProjectId(),
                context.getTeamId(),
                context.getTraceId(),
                context.getCallerService(),
                context.getSessionId(),
                context.getConversationId(),
                context.getClientChannel(),
                context.getMetadataMap()
        );
    }

    private static void validateTransportTenant(
            String requestTenantId
    ) {
        String transportTenantId =
                GrpcTenantContextInterceptor
                        .currentTenantIdOrNull();

        if (transportTenantId != null
                && !transportTenantId.equals(requestTenantId)) {
            throw new SecurityException(
                    "RequestContext tenant does not match "
                            + "the authenticated transport tenant"
            );
        }
    }

    private static void validateCallerTenant(
            String requestTenantId
    ) {
        AuthContext authContext =
                buildingblocks.infrastructure.grpc.interceptors
                        .GrpcAuthInterceptor.REQUEST_CTX_KEY.get();

        if (authContext == null) {
            throw new SecurityException(
                    "Authenticated caller context is unavailable"
            );
        }

        String callerTenantId = normalize(
                authContext.getTenantId()
        );

        String requestedTenantId = normalize(
                requestTenantId
        );

        if (callerTenantId.isEmpty()) {
            throw new SecurityException(
                    "Authenticated caller tenant is unavailable"
            );
        }

        if (!callerTenantId.equals(requestedTenantId)) {
            throw new SecurityException(
                    "Caller is not authorized for tenant "
                            + requestedTenantId
            );
        }
    }



    private static String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    public record RequestContextView(
            String requestId,
            String tenantId,
            String userId,
            String username,
            String projectId,
            String teamId,
            String traceId,
            String callerService,
            String sessionId,
            String conversationId,
            String clientChannel,
            Map<String, String> metadata
    ) {

        public RequestContextView {
            requestId = normalize(requestId);
            tenantId = normalize(tenantId);
            userId = normalize(userId);
            username = normalize(username);
            projectId = normalize(projectId);
            teamId = normalize(teamId);
            traceId = normalize(traceId);
            callerService = normalize(callerService);
            sessionId = normalize(sessionId);
            conversationId = normalize(conversationId);
            clientChannel = normalize(clientChannel);
            metadata = immutableMetadata(metadata);
        }

        private static Map<String, String> immutableMetadata(
                Map<String, String> source
        ) {
            if (source == null || source.isEmpty()) {
                return Map.of();
            }

            Map<String, String> copy =
                    new LinkedHashMap<>();

            source.forEach((key, value) -> {
                if (key != null && value != null) {
                    copy.put(key, value);
                }
            });

            return Map.copyOf(copy);
        }
    }
}