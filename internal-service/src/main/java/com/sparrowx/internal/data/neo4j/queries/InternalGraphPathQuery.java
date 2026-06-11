package com.sparrowx.internal.data.neo4j.queries;

import com.sparrowx.internal.features.getinternalgraphcontext.InternalAgentContextMapper;
import com.sparrowx.internal.models.InternalGraphContext;
import com.sparrowx.internal.valueobjects.InternalGraphNodeType;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Record;
import org.neo4j.driver.Value;
import org.neo4j.driver.Values;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class InternalGraphPathQuery {

    private static final int MAX_DEPTH = 5;
    private static final int DEFAULT_DEPTH = 5;

    private final Driver driver;

    public InternalGraphPathQuery(Driver driver) {
        this.driver = driver;
    }

    public InternalGraphContext explainPath(
            String tenantId,
            String sourceEntityId,
            InternalGraphNodeType sourceNodeType,
            String targetEntityId,
            InternalGraphNodeType targetNodeType,
            int maxDepth
    ) {
        int safeDepth = clamp(maxDepth <= 0 ? DEFAULT_DEPTH : maxDepth, 1, MAX_DEPTH);

        String graphId = "path-" + UUID.randomUUID();

        try (var session = driver.session()) {
            Record record = session.executeRead(tx -> {
                var result = tx.run(
                        """
                        MATCH (source:InternalGraphNode {
                          tenant_id: $tenantId,
                          entity_id: $sourceEntityId
                        })
                        WHERE source.node_type = $sourceNodeType

                        MATCH (target:InternalGraphNode {
                          tenant_id: $tenantId,
                          entity_id: $targetEntityId
                        })
                        WHERE target.node_type = $targetNodeType

                        MATCH path = shortestPath((source)-[*1..%d]-(target))

                        RETURN
                          [n IN nodes(path) | {
                            node_id: coalesce(n.node_id, elementId(n)),
                            node_type: coalesce(n.node_type, labels(n)[0]),
                            label: coalesce(n.label, n.name, n.title, n.entity_id),
                            summary: coalesce(n.summary, n.description, ""),
                            entity_id: coalesce(n.entity_id, n.node_id, elementId(n)),
                            graph_type: coalesce(n.graph_type, "")
                          }] AS nodes,
                          [r IN relationships(path) | {
                            relationship_id: coalesce(r.relationship_id, elementId(r)),
                            from_node_id: coalesce(startNode(r).node_id, elementId(startNode(r))),
                            to_node_id: coalesce(endNode(r).node_id, elementId(endNode(r))),
                            relationship_type: coalesce(r.relationship_type, type(r)),
                            summary: coalesce(r.summary, "")
                          }] AS relationships
                        """.formatted(safeDepth),
                        Values.parameters(
                                "tenantId", tenantId,
                                "sourceEntityId", sourceEntityId,
                                "sourceNodeType", sourceNodeType.name(),
                                "targetEntityId", targetEntityId,
                                "targetNodeType", targetNodeType.name()
                        )
                );

                return result.hasNext() ? result.next() : null;
            });

            if (record == null) {
                return InternalAgentContextMapper.toCompanyGraph(
                        graphId,
                        List.of(),
                        List.of()
                );
            }

            List<Map<String, Object>> nodes = record
                    .get("nodes")
                    .asList(value -> value.asMap(Value::asObject));

            List<Map<String, Object>> relationships = record
                    .get("relationships")
                    .asList(value -> value.asMap(Value::asObject));

            return InternalAgentContextMapper.toCompanyGraph(
                    graphId,
                    nodes,
                    relationships
            );
        }
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(value, max));
    }
}