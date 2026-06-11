// ============================================================
// SparrowX Internal Service
// team_context.cypher
// ============================================================
//
// Purpose:
// - Seed team nodes.
// - Connect teams to modules they own.
// - Safe to rerun.
//
// Required param:
// :param tenantId => "default-tenant";
//
// ============================================================


// ============================================================
// Team nodes
// ============================================================

MERGE (platformTeam:InternalGraphNode {
node_id: $tenantId + ":team:platform"
})
SET platformTeam.tenant_id = $tenantId,
platformTeam.graph_type = "COMPANY",
platformTeam.node_type = "TEAM",
platformTeam.label = "Platform Team",
platformTeam.summary = "Owns core backend services, service runtime conventions, infrastructure-facing integrations, and internal platform reliability.",
platformTeam.entity_id = "platform";

MERGE (aiTeam:InternalGraphNode {
node_id: $tenantId + ":team:ai-orchestration"
})
SET aiTeam.tenant_id = $tenantId,
aiTeam.graph_type = "COMPANY",
aiTeam.node_type = "TEAM",
aiTeam.label = "AI Orchestration Team",
aiTeam.summary = "Owns Agentic Service, orchestration, planning, tool routing, evidence-aware synthesis, and AI runtime behavior.",
aiTeam.entity_id = "ai-orchestration";

MERGE (dataTeam:InternalGraphNode {
node_id: $tenantId + ":team:data-context"
})
SET dataTeam.tenant_id = $tenantId,
dataTeam.graph_type = "COMPANY",
dataTeam.node_type = "TEAM",
dataTeam.label = "Data Context Team",
dataTeam.summary = "Owns CRM, document, and enterprise context services used by Agentic Service.",
dataTeam.entity_id = "data-context";


// ============================================================
// Team ownership relationships
// ============================================================

MATCH (aiTeam:InternalGraphNode {node_id: $tenantId + ":team:ai-orchestration"})
MATCH (agentic:InternalGraphNode {node_id: $tenantId + ":module:agentic-service"})
MERGE (aiTeam)-[r:OWNS {
relationship_id: $tenantId + ":rel:ai-team-owns-agentic-service"
}]->(agentic)
SET r.relationship_type = "OWNS",
r.summary = "AI Orchestration Team owns Agentic Service.";

MATCH (dataTeam:InternalGraphNode {node_id: $tenantId + ":team:data-context"})
MATCH (crm:InternalGraphNode {node_id: $tenantId + ":module:crm-service"})
MERGE (dataTeam)-[r:OWNS {
relationship_id: $tenantId + ":rel:data-team-owns-crm-service"
}]->(crm)
SET r.relationship_type = "OWNS",
r.summary = "Data Context Team owns CRM Service.";

MATCH (dataTeam:InternalGraphNode {node_id: $tenantId + ":team:data-context"})
MATCH (document:InternalGraphNode {node_id: $tenantId + ":module:document-service"})
MERGE (dataTeam)-[r:OWNS {
relationship_id: $tenantId + ":rel:data-team-owns-document-service"
}]->(document)
SET r.relationship_type = "OWNS",
r.summary = "Data Context Team owns Document Service.";

MATCH (platformTeam:InternalGraphNode {node_id: $tenantId + ":team:platform"})
MATCH (internal:InternalGraphNode {node_id: $tenantId + ":module:internal-service"})
MERGE (platformTeam)-[r:OWNS {
relationship_id: $tenantId + ":rel:platform-team-owns-internal-service"
}]->(internal)
SET r.relationship_type = "OWNS",
r.summary = "Platform Team owns Internal Service.";