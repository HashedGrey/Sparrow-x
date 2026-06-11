package com.sparrowx.internal.mappers;

import com.sparrowx.internal.features.getinternalgraphcontext.GetInternalGraphContextQuery;
import com.sparrowx.internal.features.getinternalgraphcontext.GetInternalGraphContextResult;
import com.sparrowx.internal.grpc.InternalGraph;
import com.sparrowx.internal.grpc.InternalGraphNode;
import com.sparrowx.internal.grpc.InternalGraphNodeType;
import com.sparrowx.internal.grpc.InternalGraphRelationship;
import com.sparrowx.internal.grpc.InternalGraphRelationshipType;
import com.sparrowx.internal.grpc.InternalGraphType;
import com.sparrowx.internal.grpc.ReadInternalCompanyGraphRequest;
import com.sparrowx.internal.grpc.ReadInternalCompanyGraphResponse;
import com.sparrowx.internal.grpc.ReadLearningGraphRequest;
import com.sparrowx.internal.grpc.ReadLearningGraphResponse;

public final class InternalGraphMapper {

    private InternalGraphMapper() {
    }

    public static GetInternalGraphContextQuery toReadInternalCompanyGraphQuery(
            ReadInternalCompanyGraphRequest request
    ) {
        return new GetInternalGraphContextQuery(
                InternalMapper.tenantId(request.getContext()),
                InternalMapper.actorId(request.getContext()),
                InternalMapper.requestId(request.getContext()),
                InternalGraphType.INTERNAL_GRAPH_TYPE_COMPANY.name(),
                request.getRootEntityId(),
                request.getRootNodeType().name(),
                request.getDepth(),
                request.getLimit()
        );
    }

    public static ReadInternalCompanyGraphResponse toReadInternalCompanyGraphResponse(
            GetInternalGraphContextResult result
    ) {
        return ReadInternalCompanyGraphResponse.newBuilder()
                .setGraph(toProto(result.graph()))
                .build();
    }

    public static GetInternalGraphContextQuery toReadLearningGraphQuery(
            ReadLearningGraphRequest request
    ) {
        return new GetInternalGraphContextQuery(
                InternalMapper.tenantId(request.getContext()),
                InternalMapper.actorId(request.getContext()),
                InternalMapper.requestId(request.getContext()),
                InternalGraphType.INTERNAL_GRAPH_TYPE_LEARNING.name(),
                request.getRootEntityId(),
                request.getRootNodeType().name(),
                request.getDepth(),
                request.getLimit()
        );
    }

    public static ReadLearningGraphResponse toReadLearningGraphResponse(
            GetInternalGraphContextResult result
    ) {
        return ReadLearningGraphResponse.newBuilder()
                .setGraph(toProto(result.graph()))
                .build();
    }

    public static InternalGraph toProto(
            com.sparrowx.internal.models.InternalGraphContext graph
    ) {
        var builder = InternalGraph.newBuilder()
                .setGraphId(graph.graphId())
                .setGraphType(toProtoGraphType(graph.graphType()))
                .setReadAt(InternalMapper.toTimestamp(graph.readAt()));

        graph.nodes().forEach(node ->
                builder.addNodes(toProto(node))
        );

        graph.relationships().forEach(relationship ->
                builder.addRelationships(toProto(relationship))
        );

        return builder.build();
    }

    private static InternalGraphNode toProto(
            com.sparrowx.internal.models.InternalGraphNode node
    ) {
        return InternalGraphNode.newBuilder()
                .setNodeId(node.nodeId())
                .setNodeType(toProtoNodeType(node.nodeType()))
                .setLabel(node.label())
                .setSummary(node.summary())
                .setEntityId(node.entityId())
                .build();
    }

    private static InternalGraphRelationship toProto(
            com.sparrowx.internal.models.InternalGraphRelationship relationship
    ) {
        return InternalGraphRelationship.newBuilder()
                .setRelationshipId(relationship.relationshipId())
                .setFromNodeId(relationship.fromNodeId())
                .setToNodeId(relationship.toNodeId())
                .setRelationshipType(toProtoRelationshipType(relationship.relationshipType()))
                .setSummary(relationship.summary())
                .build();
    }

    private static InternalGraphType toProtoGraphType(Object graphType) {
        if (graphType == null) {
            return InternalGraphType.INTERNAL_GRAPH_TYPE_UNSPECIFIED;
        }

        return switch (graphType.toString()) {
            case "COMPANY", "INTERNAL_GRAPH_TYPE_COMPANY" ->
                    InternalGraphType.INTERNAL_GRAPH_TYPE_COMPANY;
            case "LEARNING", "INTERNAL_GRAPH_TYPE_LEARNING" ->
                    InternalGraphType.INTERNAL_GRAPH_TYPE_LEARNING;
            default -> InternalGraphType.INTERNAL_GRAPH_TYPE_UNSPECIFIED;
        };
    }

    private static InternalGraphNodeType toProtoNodeType(Object nodeType) {
        if (nodeType == null) {
            return InternalGraphNodeType.INTERNAL_GRAPH_NODE_TYPE_UNSPECIFIED;
        }

        return switch (nodeType.toString()) {
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
    private static InternalGraphRelationshipType toProtoRelationshipType(
            Object relationshipType
    ) {
        if (relationshipType == null) {
            return InternalGraphRelationshipType.INTERNAL_GRAPH_RELATIONSHIP_TYPE_UNSPECIFIED;
        }

        return switch (relationshipType.toString()) {
            case "MEMBER_OF", "INTERNAL_GRAPH_RELATIONSHIP_TYPE_MEMBER_OF" ->
                    InternalGraphRelationshipType.INTERNAL_GRAPH_RELATIONSHIP_TYPE_MEMBER_OF;
            case "OWNS", "INTERNAL_GRAPH_RELATIONSHIP_TYPE_OWNS" ->
                    InternalGraphRelationshipType.INTERNAL_GRAPH_RELATIONSHIP_TYPE_OWNS;
            case "BELONGS_TO", "INTERNAL_GRAPH_RELATIONSHIP_TYPE_BELONGS_TO" ->
                    InternalGraphRelationshipType.INTERNAL_GRAPH_RELATIONSHIP_TYPE_BELONGS_TO;
            case "DOCUMENTS", "INTERNAL_GRAPH_RELATIONSHIP_TYPE_DOCUMENTS" ->
                    InternalGraphRelationshipType.INTERNAL_GRAPH_RELATIONSHIP_TYPE_DOCUMENTS;
            case "HAS_RUNBOOK", "INTERNAL_GRAPH_RELATIONSHIP_TYPE_HAS_RUNBOOK" ->
                    InternalGraphRelationshipType.INTERNAL_GRAPH_RELATIONSHIP_TYPE_HAS_RUNBOOK;
            case "HAS_TASK", "INTERNAL_GRAPH_RELATIONSHIP_TYPE_HAS_TASK" ->
                    InternalGraphRelationshipType.INTERNAL_GRAPH_RELATIONSHIP_TYPE_HAS_TASK;
            case "ASSIGNED_TO", "INTERNAL_GRAPH_RELATIONSHIP_TYPE_ASSIGNED_TO" ->
                    InternalGraphRelationshipType.INTERNAL_GRAPH_RELATIONSHIP_TYPE_ASSIGNED_TO;
            case "REQUIRES_PERMISSION", "INTERNAL_GRAPH_RELATIONSHIP_TYPE_REQUIRES_PERMISSION" ->
                    InternalGraphRelationshipType.INTERNAL_GRAPH_RELATIONSHIP_TYPE_REQUIRES_PERMISSION;
            case "DEPENDS_ON", "INTERNAL_GRAPH_RELATIONSHIP_TYPE_DEPENDS_ON" ->
                    InternalGraphRelationshipType.INTERNAL_GRAPH_RELATIONSHIP_TYPE_DEPENDS_ON;
            case "RELATED_TO", "INTERNAL_GRAPH_RELATIONSHIP_TYPE_RELATED_TO" ->
                    InternalGraphRelationshipType.INTERNAL_GRAPH_RELATIONSHIP_TYPE_RELATED_TO;
            default -> InternalGraphRelationshipType.INTERNAL_GRAPH_RELATIONSHIP_TYPE_UNSPECIFIED;
        };
    }
}