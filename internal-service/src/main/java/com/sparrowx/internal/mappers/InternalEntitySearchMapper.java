package com.sparrowx.internal.mappers;

import com.sparrowx.internal.features.searchinternalentities.SearchInternalEntitiesQuery;
import com.sparrowx.internal.features.searchinternalentities.SearchInternalEntitiesResult;
import com.sparrowx.internal.grpc.InternalEntitySearchResult;
import com.sparrowx.internal.grpc.InternalGraphNodeType;
import com.sparrowx.internal.grpc.SearchInternalEntitiesRequest;
import com.sparrowx.internal.grpc.SearchInternalEntitiesResponse;

import java.util.List;

public final class InternalEntitySearchMapper {

    private InternalEntitySearchMapper() {
    }

    public static SearchInternalEntitiesQuery toSearchInternalEntitiesQuery(
            SearchInternalEntitiesRequest request
    ) {
        return new SearchInternalEntitiesQuery(
                InternalMapper.tenantId(request.getContext()),
                InternalMapper.actorId(request.getContext()),
                InternalMapper.requestId(request.getContext()),
                request.getQuery(),
                request.getAllowedNodeTypesList()
                        .stream()
                        .map(Enum::name)
                        .toList(),
                request.getRootEntityId(),
                request.getRootNodeType().name(),
                request.getDepth(),
                request.getLimit(),
                request.getIncludeFuzzyMatches(),
                request.getFiltersMap()
        );
    }

    public static SearchInternalEntitiesResponse toSearchInternalEntitiesResponse(
            SearchInternalEntitiesResult result
    ) {
        if (result == null) {
            return SearchInternalEntitiesResponse.newBuilder()
                    .addWarnings("Search internal entities returned no result.")
                    .build();
        }

        var builder = SearchInternalEntitiesResponse.newBuilder()
                .setAmbiguous(result.ambiguous());

        nullSafe(result.results()).forEach(searchResult ->
                builder.addResults(toProto(searchResult))
        );

        builder.addAllWarnings(nullSafe(result.warnings()));

        return builder.build();
    }

    private static InternalEntitySearchResult toProto(
            com.sparrowx.internal.features.searchinternalentities.InternalEntitySearchResult result
    ) {
        var builder = InternalEntitySearchResult.newBuilder()
                .setEntityId(nullSafe(result.entityId()))
                .setNodeType(toProtoNodeType(result.nodeType()))
                .setLabel(nullSafe(result.label()))
                .setSlug(nullSafe(result.slug()))
                .setSummary(nullSafe(result.summary()))
                .setScore(result.score())
                .setMatchReason(nullSafe(result.matchReason()))
                .setParentEntityId(nullSafe(result.parentEntityId()))
                .setParentNodeType(toProtoNodeType(result.parentNodeType()));

        if (result.attributes() != null) {
            builder.putAllAttributes(result.attributes());
        }

        return builder.build();
    }

    private static InternalGraphNodeType toProtoNodeType(String nodeType) {
        if (nodeType == null || nodeType.isBlank()) {
            return InternalGraphNodeType.INTERNAL_GRAPH_NODE_TYPE_UNSPECIFIED;
        }

        return switch (nodeType) {
            case "COMPANY", "INTERNAL_GRAPH_NODE_TYPE_COMPANY" ->
                    InternalGraphNodeType.INTERNAL_GRAPH_NODE_TYPE_COMPANY;
            case "ENGINEER", "INTERNAL_GRAPH_NODE_TYPE_ENGINEER" ->
                    InternalGraphNodeType.INTERNAL_GRAPH_NODE_TYPE_ENGINEER;
            case "TEAM", "INTERNAL_GRAPH_NODE_TYPE_TEAM" ->
                    InternalGraphNodeType.INTERNAL_GRAPH_NODE_TYPE_TEAM;
            case "MODULE", "INTERNAL_GRAPH_NODE_TYPE_MODULE" ->
                    InternalGraphNodeType.INTERNAL_GRAPH_NODE_TYPE_MODULE;
            case "REPOSITORY", "INTERNAL_GRAPH_NODE_TYPE_REPOSITORY" ->
                    InternalGraphNodeType.INTERNAL_GRAPH_NODE_TYPE_REPOSITORY;
            case "DOCUMENT", "INTERNAL_GRAPH_NODE_TYPE_DOCUMENT" ->
                    InternalGraphNodeType.INTERNAL_GRAPH_NODE_TYPE_DOCUMENT;
            case "RUNBOOK", "INTERNAL_GRAPH_NODE_TYPE_RUNBOOK" ->
                    InternalGraphNodeType.INTERNAL_GRAPH_NODE_TYPE_RUNBOOK;
            case "ONBOARDING_PATH", "INTERNAL_GRAPH_NODE_TYPE_ONBOARDING_PATH" ->
                    InternalGraphNodeType.INTERNAL_GRAPH_NODE_TYPE_ONBOARDING_PATH;
            case "ONBOARDING_TASK", "INTERNAL_GRAPH_NODE_TYPE_ONBOARDING_TASK" ->
                    InternalGraphNodeType.INTERNAL_GRAPH_NODE_TYPE_ONBOARDING_TASK;
            case "ONBOARDING_ASSIGNMENT", "INTERNAL_GRAPH_NODE_TYPE_ONBOARDING_ASSIGNMENT" ->
                    InternalGraphNodeType.INTERNAL_GRAPH_NODE_TYPE_ONBOARDING_ASSIGNMENT;
            case "PERMISSION", "INTERNAL_GRAPH_NODE_TYPE_PERMISSION" ->
                    InternalGraphNodeType.INTERNAL_GRAPH_NODE_TYPE_PERMISSION;
            case "LEARNING_TOPIC", "INTERNAL_GRAPH_NODE_TYPE_LEARNING_TOPIC" ->
                    InternalGraphNodeType.INTERNAL_GRAPH_NODE_TYPE_LEARNING_TOPIC;
            case "LEARNING_OBJECTIVE", "INTERNAL_GRAPH_NODE_TYPE_LEARNING_OBJECTIVE" ->
                    InternalGraphNodeType.INTERNAL_GRAPH_NODE_TYPE_LEARNING_OBJECTIVE;
            default -> InternalGraphNodeType.INTERNAL_GRAPH_NODE_TYPE_UNSPECIFIED;
        };
    }

    private static <T> List<T> nullSafe(List<T> values) {
        return values == null ? List.of() : values;
    }

    private static String nullSafe(String value) {
        return value == null ? "" : value;
    }
}