// ============================================================
// SparrowX Internal Service
// create_indexes.cypher
// ============================================================
//
// Purpose:
// - Speed up read-only graph context lookups.
// - Support rootEntityId + rootNodeType graph reads.
// - Safe to rerun.
//
// ============================================================

CREATE INDEX internal_graph_tenant_entity IF NOT EXISTS
FOR (n:InternalGraphNode)
ON (n.tenant_id, n.entity_id);

CREATE INDEX internal_graph_tenant_node_type IF NOT EXISTS
FOR (n:InternalGraphNode)
ON (n.tenant_id, n.node_type);

CREATE INDEX internal_graph_graph_type IF NOT EXISTS
FOR (n:InternalGraphNode)
ON (n.graph_type);

CREATE INDEX internal_graph_tenant_graph_type IF NOT EXISTS
FOR (n:InternalGraphNode)
ON (n.tenant_id, n.graph_type);

CREATE INDEX internal_graph_label IF NOT EXISTS
FOR (n:InternalGraphNode)
ON (n.label);