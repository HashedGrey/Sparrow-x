package com.sparrowx.internal.models;

import com.sparrowx.internal.valueobjects.InternalGraphRelationshipType;

public record InternalGraphRelationship(
        String relationshipId,
        String fromNodeId,
        String toNodeId,
        InternalGraphRelationshipType relationshipType,
        String summary
) {
    public InternalGraphRelationship {
        if (relationshipId == null || relationshipId.isBlank()) {
            throw new IllegalArgumentException("relationshipId is required");
        }

        if (fromNodeId == null || fromNodeId.isBlank()) {
            throw new IllegalArgumentException("fromNodeId is required");
        }

        if (toNodeId == null || toNodeId.isBlank()) {
            throw new IllegalArgumentException("toNodeId is required");
        }

        if (relationshipType == null) {
            throw new IllegalArgumentException("relationshipType is required");
        }

        if (summary == null) {
            summary = "";
        }

        relationshipId = relationshipId.trim();
        fromNodeId = fromNodeId.trim();
        toNodeId = toNodeId.trim();
        summary = summary.trim();
    }
}