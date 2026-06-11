package com.sparrowx.internal.models;

import com.sparrowx.internal.valueobjects.InternalGraphType;

import java.time.Instant;
import java.util.List;

public record InternalGraphContext(
        String graphId,
        InternalGraphType graphType,
        List<InternalGraphNode> nodes,
        List<InternalGraphRelationship> relationships,
        Instant readAt
) {
    public InternalGraphContext {
        if (graphId == null || graphId.isBlank()) {
            throw new IllegalArgumentException("graphId is required");
        }

        if (graphType == null) {
            throw new IllegalArgumentException("graphType is required");
        }

        nodes = nodes == null ? List.of() : List.copyOf(nodes);
        relationships = relationships == null ? List.of() : List.copyOf(relationships);

        if (readAt == null) {
            readAt = Instant.now();
        }

        graphId = graphId.trim();
    }

    public static InternalGraphContext of(
            String graphId,
            InternalGraphType graphType,
            List<InternalGraphNode> nodes,
            List<InternalGraphRelationship> relationships
    ) {
        return new InternalGraphContext(
                graphId,
                graphType,
                nodes,
                relationships,
                Instant.now()
        );
    }
}