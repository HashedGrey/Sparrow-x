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
public class LearningGraphQuery {

    private static final int MAX_DEPTH = 5;
    private static final int DEFAULT_DEPTH = 2;
    private static final int DEFAULT_LIMIT = 50;
    private static final int MAX_LIMIT = 200;

    private final Driver driver;

    public LearningGraphQuery(Driver driver) {
        this.driver = driver;
    }

    public InternalGraphContext read(
            String tenantId,
            String rootEntityId,
            InternalGraphNodeType rootNodeType,
            int depth,
            int limit
    ) {
        int safeDepth = clamp(depth <= 0 ? DEFAULT_DEPTH : depth, 1, MAX_DEPTH);
        int safeLimit = clamp(limit <= 0 ? DEFAULT_LIMIT : limit, 1, MAX_LIMIT);

        String graphId = "learning-" + UUID.randomUUID();

        try (var session = driver.session()) {
            Record record = session.executeRead(tx -> {
                var result = tx.run(
                        """
                        MATCH (root:InternalGraphNode {
                          tenant_id: $tenantId,
                          entity_id: $rootEntityId
                        })
                        WHERE root.node_type = $rootNodeType
                          AND root.graph_type = "LEARNING"

                        OPTIONAL MATCH path = (root)-[*1..%d]-(connected:InternalGraphNode)
                        WHERE connected.node_id <> root.node_id
                          AND connected.graph_type = "LEARNING"

                        WITH root, connected, relationships(path) AS rels
                        LIMIT $limit

                        WITH collect(DISTINCT root) + collect(DISTINCT connected) AS rawNodes,
                             collect(rels) AS relGroups

                        WITH rawNodes,
                             reduce(acc = [], group IN relGroups | acc + group) AS rawRelationships

                        WITH
                          reduce(uniqueNodes = [], n IN rawNodes |
                            CASE
                              WHEN n IS NOT NULL AND NOT n IN uniqueNodes THEN uniqueNodes + n
                              ELSE uniqueNodes
                            END
                          ) AS ns,
                          reduce(uniqueRels = [], r IN rawRelationships |
                            CASE
                              WHEN r IS NOT NULL AND NOT r IN uniqueRels THEN uniqueRels + r
                              ELSE uniqueRels
                            END
                          ) AS rs

                        RETURN
                          [n IN ns | {
                            node_id: coalesce(n.node_id, elementId(n)),
                            node_type: coalesce(n.node_type, labels(n)[0]),
                            label: coalesce(n.label, n.name, n.title, n.entity_id),
                            summary: coalesce(n.summary, n.description, ""),
                            entity_id: coalesce(n.entity_id, n.node_id, elementId(n)),
                            graph_type: coalesce(n.graph_type, "")
                          }] AS nodes,
                          [r IN rs | {
                            relationship_id: coalesce(r.relationship_id, elementId(r)),
                            from_node_id: coalesce(startNode(r).node_id, elementId(startNode(r))),
                            to_node_id: coalesce(endNode(r).node_id, elementId(endNode(r))),
                            relationship_type: coalesce(r.relationship_type, type(r)),
                            summary: coalesce(r.summary, "")
                          }] AS relationships
                        """.formatted(safeDepth),
                        Values.parameters(
                                "tenantId", tenantId,
                                "rootEntityId", rootEntityId,
                                "rootNodeType", rootNodeType.name(),
                                "limit", safeLimit
                        )
                );

                return result.hasNext() ? result.next() : null;
            });

            if (record == null) {
                return InternalAgentContextMapper.toLearningGraph(
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

            return InternalAgentContextMapper.toLearningGraph(
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