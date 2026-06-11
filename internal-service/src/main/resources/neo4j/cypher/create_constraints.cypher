// ============================================================
// SparrowX Internal Service
// create_constraints.cypher
// ============================================================
//
// Purpose:
// - Create graph constraints once.
// - Runtime graph APIs remain read-only.
// - Safe to rerun.
//
// ============================================================

CREATE CONSTRAINT internal_graph_node_id_unique IF NOT EXISTS
FOR (n:InternalGraphNode)
REQUIRE n.node_id IS UNIQUE;

CREATE CONSTRAINT internal_graph_node_identity_unique IF NOT EXISTS
FOR (n:InternalGraphNode)
REQUIRE (n.tenant_id, n.graph_type, n.node_type, n.entity_id) IS UNIQUE;