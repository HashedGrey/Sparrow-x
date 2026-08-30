package com.sparrowx.agentic.tools.internal;

import com.sparrowx.agentic.adapters.internal.InternalClientMapper;
import com.sparrowx.agentic.mission.model.MissionContext;
import com.sparrowx.internal.grpc.InternalGraphNodeType;
import com.sparrowx.internal.grpc.SearchInternalEntitiesRequest;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class InternalEntitySearchRequestBuilder {

    private final InternalClientMapper clientMapper;

    public InternalEntitySearchRequestBuilder(
            InternalClientMapper clientMapper) {

        this.clientMapper = Objects.requireNonNull(
                clientMapper,
                "clientMapper must not be null");
    }

    public SearchInternalEntitiesRequest build(
            MissionContext context,
            SearchSpec spec) {

        Objects.requireNonNull(context, "context must not be null");
        Objects.requireNonNull(spec, "spec must not be null");

        return SearchInternalEntitiesRequest.newBuilder()
                .setContext(clientMapper.toRequestContext(
                        context,
                        spec.requestId()))
                .setQuery(spec.query())
                .addAllAllowedNodeTypes(spec.allowedNodeTypes())
                .setRootEntityId(spec.rootEntityId())
                .setRootNodeType(spec.rootNodeType())
                .setDepth(spec.depth())
                .setLimit(spec.limit())
                .setIncludeFuzzyMatches(spec.includeFuzzyMatches())
                .putAllFilters(spec.filters())
                .build();
    }

    public record SearchSpec(
            String requestId,
            String query,
            List<InternalGraphNodeType> allowedNodeTypes,
            String rootEntityId,
            InternalGraphNodeType rootNodeType,
            int depth,
            int limit,
            boolean includeFuzzyMatches,
            Map<String, String> filters) {

        public SearchSpec {
            requestId = requireText(requestId, "requestId");
            query = requireText(query, "query");

            allowedNodeTypes = allowedNodeTypes == null
                    ? List.of()
                    : List.copyOf(allowedNodeTypes);

            rootEntityId = rootEntityId == null
                    ? ""
                    : rootEntityId;

            rootNodeType = rootNodeType == null
                    ? InternalGraphNodeType
                    .INTERNAL_GRAPH_NODE_TYPE_UNSPECIFIED
                    : rootNodeType;

            if (!rootEntityId.isBlank()
                    && (rootNodeType == InternalGraphNodeType
                    .INTERNAL_GRAPH_NODE_TYPE_UNSPECIFIED
                    || rootNodeType
                    == InternalGraphNodeType.UNRECOGNIZED)) {

                throw new IllegalArgumentException(
                        "rootNodeType is required with rootEntityId");
            }

            if (depth < 0) {
                throw new IllegalArgumentException(
                        "depth must not be negative");
            }

            if (limit <= 0) {
                throw new IllegalArgumentException(
                        "limit must be positive");
            }

            filters = filters == null
                    ? Map.of()
                    : Map.copyOf(filters);
        }
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    field + " must not be blank");
        }

        return value;
    }
}