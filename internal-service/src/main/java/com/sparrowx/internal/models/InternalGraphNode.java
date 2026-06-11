package com.sparrowx.internal.models;

import com.sparrowx.internal.valueobjects.InternalGraphNodeType;

public record InternalGraphNode(
        String nodeId,
        InternalGraphNodeType nodeType,
        String label,
        String summary,
        String entityId
) {
    public InternalGraphNode {
        if (nodeId == null || nodeId.isBlank()) {
            throw new IllegalArgumentException("nodeId is required");
        }

        if (nodeType == null) {
            throw new IllegalArgumentException("nodeType is required");
        }

        if (label == null || label.isBlank()) {
            throw new IllegalArgumentException("label is required");
        }

        if (summary == null) {
            summary = "";
        }

        if (entityId == null || entityId.isBlank()) {
            throw new IllegalArgumentException("entityId is required");
        }

        nodeId = nodeId.trim();
        label = label.trim();
        summary = summary.trim();
        entityId = entityId.trim();
    }
}