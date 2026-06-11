// ============================================================
// SparrowX Internal Service
// engineer_context.cypher
// ============================================================
//
// Purpose:
// - Seed engineer/learner context.
// - Connect engineers to teams.
// - Connect engineers to onboarding paths if known.
// - Safe to rerun.
//
// Required param:
// :param tenantId => "default-tenant";
//
// ============================================================


// ============================================================
// Engineer nodes
// ============================================================

MERGE (aggrey:InternalGraphNode {
node_id: $tenantId + ":engineer:aggrey-lelei"
})
SET aggrey.tenant_id = $tenantId,
aggrey.graph_type = "COMPANY",
aggrey.node_type = "ENGINEER",
aggrey.label = "Aggrey Lelei",
aggrey.summary = "Engineer building SparrowX internal, agentic, CRM, and document service learning platform.",
aggrey.entity_id = "aggrey-lelei";

MERGE (mentor:InternalGraphNode {
node_id: $tenantId + ":engineer:platform-mentor"
})
SET mentor.tenant_id = $tenantId,
mentor.graph_type = "COMPANY",
mentor.node_type = "ENGINEER",
mentor.label = "Platform Mentor",
mentor.summary = "Reference mentor persona for platform onboarding and review.",
mentor.entity_id = "platform-mentor";


// ============================================================
// Engineer -> team membership
// ============================================================

MATCH (engineer:InternalGraphNode {node_id: $tenantId + ":engineer:aggrey-lelei"})
MATCH (team:InternalGraphNode {node_id: $tenantId + ":team:platform"})
MERGE (engineer)-[r:MEMBER_OF {
relationship_id: $tenantId + ":rel:aggrey-member-of-platform"
}]->(team)
SET r.relationship_type = "MEMBER_OF",
r.summary = "Aggrey is associated with Platform Team for Internal Service implementation.";

MATCH (engineer:InternalGraphNode {node_id: $tenantId + ":engineer:aggrey-lelei"})
MATCH (team:InternalGraphNode {node_id: $tenantId + ":team:ai-orchestration"})
MERGE (engineer)-[r:MEMBER_OF {
relationship_id: $tenantId + ":rel:aggrey-member-of-ai-orchestration"
}]->(team)
SET r.relationship_type = "MEMBER_OF",
r.summary = "Aggrey is associated with AI Orchestration Team for Agentic Service work.";

MATCH (engineer:InternalGraphNode {node_id: $tenantId + ":engineer:platform-mentor"})
MATCH (team:InternalGraphNode {node_id: $tenantId + ":team:platform"})
MERGE (engineer)-[r:MEMBER_OF {
relationship_id: $tenantId + ":rel:platform-mentor-member-of-platform"
}]->(team)
SET r.relationship_type = "MEMBER_OF",
r.summary = "Platform Mentor belongs to Platform Team.";


// ============================================================
// Engineer -> modules under learning/ownership context
// ============================================================

MATCH (engineer:InternalGraphNode {node_id: $tenantId + ":engineer:aggrey-lelei"})
MATCH (module:InternalGraphNode {node_id: $tenantId + ":module:internal-service"})
MERGE (engineer)-[r:RELATED_TO {
relationship_id: $tenantId + ":rel:aggrey-related-to-internal-service"
}]->(module)
SET r.relationship_type = "RELATED_TO",
r.summary = "Aggrey is currently implementing Internal Service.";

MATCH (engineer:InternalGraphNode {node_id: $tenantId + ":engineer:aggrey-lelei"})
MATCH (module:InternalGraphNode {node_id: $tenantId + ":module:agentic-service"})
MERGE (engineer)-[r:RELATED_TO {
relationship_id: $tenantId + ":rel:aggrey-related-to-agentic-service"
}]->(module)
SET r.relationship_type = "RELATED_TO",
r.summary = "Aggrey is building Agentic Service as the orchestrator over internal, CRM, and document services.";

MATCH (engineer:InternalGraphNode {node_id: $tenantId + ":engineer:aggrey-lelei"})
MATCH (module:InternalGraphNode {node_id: $tenantId + ":module:document-service"})
MERGE (engineer)-[r:RELATED_TO {
relationship_id: $tenantId + ":rel:aggrey-related-to-document-service"
}]->(module)
SET r.relationship_type = "RELATED_TO",
r.summary = "Aggrey has implemented Document Service retrieval, verification, and evidence graph capabilities.";


// ============================================================
// Engineer -> onboarding assignment context
// ============================================================

MERGE (assignment:InternalGraphNode {
node_id: $tenantId + ":assignment:aggrey-internal-service-path"
})
SET assignment.tenant_id = $tenantId,
assignment.graph_type = "COMPANY",
assignment.node_type = "ONBOARDING_ASSIGNMENT",
assignment.label = "Aggrey Internal Service Onboarding Assignment",
assignment.summary = "Assignment connecting Aggrey to Internal Service onboarding path.",
assignment.entity_id = "aggrey-internal-service-path-assignment";

MATCH (engineer:InternalGraphNode {node_id: $tenantId + ":engineer:aggrey-lelei"})
MATCH (assignment:InternalGraphNode {node_id: $tenantId + ":assignment:aggrey-internal-service-path"})
MERGE (assignment)-[r:ASSIGNED_TO {
relationship_id: $tenantId + ":rel:aggrey-assigned-internal-service-path"
}]->(engineer)
SET r.relationship_type = "ASSIGNED_TO",
r.summary = "Internal Service onboarding assignment is assigned to Aggrey.";

MATCH (assignment:InternalGraphNode {node_id: $tenantId + ":assignment:aggrey-internal-service-path"})
MATCH (path:InternalGraphNode {node_id: $tenantId + ":onboarding_path:internal-service-path"})
MERGE (assignment)-[r:RELATED_TO {
relationship_id: $tenantId + ":rel:aggrey-assignment-related-to-internal-path"
}]->(path)
SET r.relationship_type = "RELATED_TO",
r.summary = "Aggrey onboarding assignment is related to Internal Service onboarding path.";