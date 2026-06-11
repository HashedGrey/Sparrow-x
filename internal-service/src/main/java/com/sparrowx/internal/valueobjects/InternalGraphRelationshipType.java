package com.sparrowx.internal.valueobjects;

public enum InternalGraphRelationshipType {
    MEMBER_OF,
    OWNS,
    BELONGS_TO,
    DOCUMENTS,
    HAS_RUNBOOK,
    HAS_TASK,
    ASSIGNED_TO,
    REQUIRES_PERMISSION,
    DEPENDS_ON,
    RELATED_TO;

    public static InternalGraphRelationshipType from(String value) {
        if (value == null || value.isBlank()) {
            return RELATED_TO;
        }

        return switch (value.trim().toUpperCase()) {
            case "INTERNAL_GRAPH_RELATIONSHIP_TYPE_MEMBER_OF", "MEMBER_OF" -> MEMBER_OF;
            case "INTERNAL_GRAPH_RELATIONSHIP_TYPE_OWNS", "OWNS" -> OWNS;
            case "INTERNAL_GRAPH_RELATIONSHIP_TYPE_BELONGS_TO", "BELONGS_TO" -> BELONGS_TO;
            case "INTERNAL_GRAPH_RELATIONSHIP_TYPE_DOCUMENTS", "DOCUMENTS" -> DOCUMENTS;
            case "INTERNAL_GRAPH_RELATIONSHIP_TYPE_HAS_RUNBOOK", "HAS_RUNBOOK" -> HAS_RUNBOOK;
            case "INTERNAL_GRAPH_RELATIONSHIP_TYPE_HAS_TASK", "HAS_TASK" -> HAS_TASK;
            case "INTERNAL_GRAPH_RELATIONSHIP_TYPE_ASSIGNED_TO", "ASSIGNED_TO" -> ASSIGNED_TO;
            case "INTERNAL_GRAPH_RELATIONSHIP_TYPE_REQUIRES_PERMISSION", "REQUIRES_PERMISSION" -> REQUIRES_PERMISSION;
            case "INTERNAL_GRAPH_RELATIONSHIP_TYPE_DEPENDS_ON", "DEPENDS_ON" -> DEPENDS_ON;
            case "INTERNAL_GRAPH_RELATIONSHIP_TYPE_RELATED_TO", "RELATED_TO" -> RELATED_TO;
            default -> RELATED_TO;
        };
    }
}