package com.sparrowx.internal.features.getinternalgraphcontext;

import com.sparrowx.internal.models.InternalGraphContext;
import com.sparrowx.internal.models.InternalGraphNode;
import com.sparrowx.internal.models.InternalGraphRelationship;
import com.sparrowx.internal.valueobjects.InternalGraphNodeType;
import com.sparrowx.internal.valueobjects.InternalGraphRelationshipType;
import com.sparrowx.internal.valueobjects.InternalGraphType;

import java.util.List;
import java.util.Map;

public final class InternalAgentContextMapper {

    private InternalAgentContextMapper() {
    }

    public static InternalGraphContext toCompanyGraph(
            String graphId,
            List<Map<String, Object>> nodes,
            List<Map<String, Object>> relationships
    ) {
        return InternalGraphContext.of(
                graphId,
                InternalGraphType.COMPANY,
                toNodes(nodes),
                toRelationships(relationships)
        );
    }

    public static InternalGraphContext toLearningGraph(
            String graphId,
            List<Map<String, Object>> nodes,
            List<Map<String, Object>> relationships
    ) {
        return InternalGraphContext.of(
                graphId,
                InternalGraphType.LEARNING,
                toNodes(nodes),
                toRelationships(relationships)
        );
    }

    private static List<InternalGraphNode> toNodes(
            List<Map<String, Object>> rows
    ) {
        if (rows == null) {
            return List.of();
        }

        return rows.stream()
                .map(row -> new InternalGraphNode(
                        string(row, "node_id"),
                        InternalGraphNodeType.from(string(row, "node_type")),
                        string(row, "label"),
                        string(row, "summary"),
                        string(row, "entity_id")
                ))
                .toList();
    }

    private static List<InternalGraphRelationship> toRelationships(
            List<Map<String, Object>> rows
    ) {
        if (rows == null) {
            return List.of();
        }

        return rows.stream()
                .map(row -> new InternalGraphRelationship(
                        string(row, "relationship_id"),
                        string(row, "from_node_id"),
                        string(row, "to_node_id"),
                        InternalGraphRelationshipType.from(string(row, "relationship_type")),
                        string(row, "summary")
                ))
                .toList();
    }

    private static String string(
            Map<String, Object> row,
            String key
    ) {
        if (row == null || !row.containsKey(key) || row.get(key) == null) {
            return "";
        }

        return row.get(key).toString();
    }
}