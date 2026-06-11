// ============================================================
// SparrowX Internal Service
// build_internal_graph.cypher
// ============================================================
//
// Purpose:
// - Seed the top-level SparrowX internal company graph.
// - Create root company node.
// - Create core service/module nodes.
// - Create high-level service dependency relationships.
//
// Required param:
// :param tenantId => "default-tenant";
//
// ============================================================


// ============================================================
// Company root
// ============================================================

MERGE (company:InternalGraphNode {
node_id: $tenantId + ":company:sparrowx"
})
SET company.tenant_id = $tenantId,
company.graph_type = "COMPANY",
company.node_type = "COMPANY",
company.label = "SparrowX",
company.summary = "Internal company graph root for SparrowX.",
company.entity_id = "sparrowx";


// ============================================================
// Core services / modules
// ============================================================

MERGE (agentic:InternalGraphNode {
node_id: $tenantId + ":module:agentic-service"
})
SET agentic.tenant_id = $tenantId,
agentic.graph_type = "COMPANY",
agentic.node_type = "MODULE",
agentic.label = "Agentic Service",
agentic.summary = "Mission intake, planning, orchestration, tool routing, service calls, and synthesis.",
agentic.entity_id = "agentic-service";

MERGE (crm:InternalGraphNode {
node_id: $tenantId + ":module:crm-service"
})
SET crm.tenant_id = $tenantId,
crm.graph_type = "COMPANY",
crm.node_type = "MODULE",
crm.label = "CRM Service",
crm.summary = "Customer, account, subscription, usage, risk, support, incident, feature, bug, activity, and contract context service.",
crm.entity_id = "crm-service";

MERGE (document:InternalGraphNode {
node_id: $tenantId + ":module:document-service"
})
SET document.tenant_id = $tenantId,
document.graph_type = "COMPANY",
document.node_type = "MODULE",
document.label = "Document Service",
document.summary = "Document upload, extraction, chunking, indexing, hybrid retrieval, citation verification, and evidence graph building.",
document.entity_id = "document-service";

MERGE (internal:InternalGraphNode {
node_id: $tenantId + ":module:internal-service"
})
SET internal.tenant_id = $tenantId,
internal.graph_type = "COMPANY",
internal.node_type = "MODULE",
internal.label = "Internal Service",
internal.summary = "Engineers, teams, modules, repositories, internal document references, runbooks, onboarding, permissions, and read-only graph context.",
internal.entity_id = "internal-service";


// ============================================================
// Company owns core modules
// ============================================================

MATCH (company:InternalGraphNode {node_id: $tenantId + ":company:sparrowx"})
MATCH (agentic:InternalGraphNode {node_id: $tenantId + ":module:agentic-service"})
MERGE (company)-[r:OWNS {
relationship_id: $tenantId + ":rel:company-owns-agentic-service"
}]->(agentic)
SET r.relationship_type = "OWNS",
r.summary = "SparrowX owns Agentic Service.";

MATCH (company:InternalGraphNode {node_id: $tenantId + ":company:sparrowx"})
MATCH (crm:InternalGraphNode {node_id: $tenantId + ":module:crm-service"})
MERGE (company)-[r:OWNS {
relationship_id: $tenantId + ":rel:company-owns-crm-service"
}]->(crm)
SET r.relationship_type = "OWNS",
r.summary = "SparrowX owns CRM Service.";

MATCH (company:InternalGraphNode {node_id: $tenantId + ":company:sparrowx"})
MATCH (document:InternalGraphNode {node_id: $tenantId + ":module:document-service"})
MERGE (company)-[r:OWNS {
relationship_id: $tenantId + ":rel:company-owns-document-service"
}]->(document)
SET r.relationship_type = "OWNS",
r.summary = "SparrowX owns Document Service.";

MATCH (company:InternalGraphNode {node_id: $tenantId + ":company:sparrowx"})
MATCH (internal:InternalGraphNode {node_id: $tenantId + ":module:internal-service"})
MERGE (company)-[r:OWNS {
relationship_id: $tenantId + ":rel:company-owns-internal-service"
}]->(internal)
SET r.relationship_type = "OWNS",
r.summary = "SparrowX owns Internal Service.";


// ============================================================
// Core service dependencies
// ============================================================

MATCH (agentic:InternalGraphNode {node_id: $tenantId + ":module:agentic-service"})
MATCH (document:InternalGraphNode {node_id: $tenantId + ":module:document-service"})
MERGE (agentic)-[r:DEPENDS_ON {
relationship_id: $tenantId + ":rel:agentic-depends-on-document"
}]->(document)
SET r.relationship_type = "DEPENDS_ON",
r.summary = "Agentic Service calls Document Service for document search, evidence, and verification.";

MATCH (agentic:InternalGraphNode {node_id: $tenantId + ":module:agentic-service"})
MATCH (crm:InternalGraphNode {node_id: $tenantId + ":module:crm-service"})
MERGE (agentic)-[r:DEPENDS_ON {
relationship_id: $tenantId + ":rel:agentic-depends-on-crm"
}]->(crm)
SET r.relationship_type = "DEPENDS_ON",
r.summary = "Agentic Service calls CRM Service for customer, account, risk, and contract context.";

MATCH (agentic:InternalGraphNode {node_id: $tenantId + ":module:agentic-service"})
MATCH (internal:InternalGraphNode {node_id: $tenantId + ":module:internal-service"})
MERGE (agentic)-[r:DEPENDS_ON {
relationship_id: $tenantId + ":rel:agentic-depends-on-internal"
}]->(internal)
SET r.relationship_type = "DEPENDS_ON",
r.summary = "Agentic Service calls Internal Service for company graph and learning graph context.";

MATCH (internal:InternalGraphNode {node_id: $tenantId + ":module:internal-service"})
MATCH (document:InternalGraphNode {node_id: $tenantId + ":module:document-service"})
MERGE (internal)-[r:RELATED_TO {
relationship_id: $tenantId + ":rel:internal-related-to-document"
}]->(document)
SET r.relationship_type = "RELATED_TO",
r.summary = "Internal Service stores references to documents that may live in Document Service.";