// ============================================================
// SparrowX Internal Service
// module_context.cypher
// ============================================================
//
// Purpose:
// - Seed repository, document, and runbook nodes around modules.
// - Connect module -> repository/document/runbook context.
// - Safe to rerun.
//
// Required param:
// :param tenantId => "default-tenant";
//
// ============================================================


// ============================================================
// Repository nodes
// ============================================================

MERGE (agenticRepo:InternalGraphNode {
node_id: $tenantId + ":repository:agentic-service-repo"
})
SET agenticRepo.tenant_id = $tenantId,
agenticRepo.graph_type = "COMPANY",
agenticRepo.node_type = "REPOSITORY",
agenticRepo.label = "agentic-service",
agenticRepo.summary = "Repository for Agentic Service.",
agenticRepo.entity_id = "agentic-service-repo";

MERGE (crmRepo:InternalGraphNode {
node_id: $tenantId + ":repository:crm-service-repo"
})
SET crmRepo.tenant_id = $tenantId,
crmRepo.graph_type = "COMPANY",
crmRepo.node_type = "REPOSITORY",
crmRepo.label = "crm-service",
crmRepo.summary = "Repository for CRM Service.",
crmRepo.entity_id = "crm-service-repo";

MERGE (documentRepo:InternalGraphNode {
node_id: $tenantId + ":repository:document-service-repo"
})
SET documentRepo.tenant_id = $tenantId,
documentRepo.graph_type = "COMPANY",
documentRepo.node_type = "REPOSITORY",
documentRepo.label = "document-service",
documentRepo.summary = "Repository for Document Service.",
documentRepo.entity_id = "document-service-repo";

MERGE (internalRepo:InternalGraphNode {
node_id: $tenantId + ":repository:internal-service-repo"
})
SET internalRepo.tenant_id = $tenantId,
internalRepo.graph_type = "COMPANY",
internalRepo.node_type = "REPOSITORY",
internalRepo.label = "internal-service",
internalRepo.summary = "Repository for Internal Service.",
internalRepo.entity_id = "internal-service-repo";


// ============================================================
// Repository -> module relationships
// ============================================================

MATCH (repo:InternalGraphNode {node_id: $tenantId + ":repository:agentic-service-repo"})
MATCH (module:InternalGraphNode {node_id: $tenantId + ":module:agentic-service"})
MERGE (repo)-[r:BELONGS_TO {
relationship_id: $tenantId + ":rel:agentic-repo-belongs-to-agentic"
}]->(module)
SET r.relationship_type = "BELONGS_TO",
r.summary = "Agentic Service repository belongs to Agentic Service.";

MATCH (repo:InternalGraphNode {node_id: $tenantId + ":repository:crm-service-repo"})
MATCH (module:InternalGraphNode {node_id: $tenantId + ":module:crm-service"})
MERGE (repo)-[r:BELONGS_TO {
relationship_id: $tenantId + ":rel:crm-repo-belongs-to-crm"
}]->(module)
SET r.relationship_type = "BELONGS_TO",
r.summary = "CRM Service repository belongs to CRM Service.";

MATCH (repo:InternalGraphNode {node_id: $tenantId + ":repository:document-service-repo"})
MATCH (module:InternalGraphNode {node_id: $tenantId + ":module:document-service"})
MERGE (repo)-[r:BELONGS_TO {
relationship_id: $tenantId + ":rel:document-repo-belongs-to-document"
}]->(module)
SET r.relationship_type = "BELONGS_TO",
r.summary = "Document Service repository belongs to Document Service.";

MATCH (repo:InternalGraphNode {node_id: $tenantId + ":repository:internal-service-repo"})
MATCH (module:InternalGraphNode {node_id: $tenantId + ":module:internal-service"})
MERGE (repo)-[r:BELONGS_TO {
relationship_id: $tenantId + ":rel:internal-repo-belongs-to-internal"
}]->(module)
SET r.relationship_type = "BELONGS_TO",
r.summary = "Internal Service repository belongs to Internal Service.";


// ============================================================
// Internal Service documents and runbooks
// ============================================================

MERGE (internalProtoDoc:InternalGraphNode {
node_id: $tenantId + ":document:internal-proto"
})
SET internalProtoDoc.tenant_id = $tenantId,
internalProtoDoc.graph_type = "COMPANY",
internalProtoDoc.node_type = "DOCUMENT",
internalProtoDoc.label = "Internal Service Proto",
internalProtoDoc.summary = "gRPC contract for Internal Service.",
internalProtoDoc.entity_id = "internal-proto";

MERGE (internalReadmeDoc:InternalGraphNode {
node_id: $tenantId + ":document:internal-readme"
})
SET internalReadmeDoc.tenant_id = $tenantId,
internalReadmeDoc.graph_type = "COMPANY",
internalReadmeDoc.node_type = "DOCUMENT",
internalReadmeDoc.label = "Internal Service README",
internalReadmeDoc.summary = "Internal Service architecture, runtime rules, and graph read contract.",
internalReadmeDoc.entity_id = "internal-readme";

MERGE (internalRunbook:InternalGraphNode {
node_id: $tenantId + ":runbook:internal-service-onboarding"
})
SET internalRunbook.tenant_id = $tenantId,
internalRunbook.graph_type = "COMPANY",
internalRunbook.node_type = "RUNBOOK",
internalRunbook.label = "Internal Service Onboarding Runbook",
internalRunbook.summary = "Runbook for understanding and operating Internal Service.",
internalRunbook.entity_id = "internal-service-onboarding-runbook";


// ============================================================
// Module -> document/runbook relationships
// ============================================================

MATCH (doc:InternalGraphNode {node_id: $tenantId + ":document:internal-proto"})
MATCH (module:InternalGraphNode {node_id: $tenantId + ":module:internal-service"})
MERGE (doc)-[r:DOCUMENTS {
relationship_id: $tenantId + ":rel:internal-proto-documents-internal-service"
}]->(module)
SET r.relationship_type = "DOCUMENTS",
r.summary = "Internal proto documents Internal Service.";

MATCH (doc:InternalGraphNode {node_id: $tenantId + ":document:internal-readme"})
MATCH (module:InternalGraphNode {node_id: $tenantId + ":module:internal-service"})
MERGE (doc)-[r:DOCUMENTS {
relationship_id: $tenantId + ":rel:internal-readme-documents-internal-service"
}]->(module)
SET r.relationship_type = "DOCUMENTS",
r.summary = "Internal README documents Internal Service.";

MATCH (module:InternalGraphNode {node_id: $tenantId + ":module:internal-service"})
MATCH (runbook:InternalGraphNode {node_id: $tenantId + ":runbook:internal-service-onboarding"})
MERGE (module)-[r:HAS_RUNBOOK {
relationship_id: $tenantId + ":rel:internal-service-has-onboarding-runbook"
}]->(runbook)
SET r.relationship_type = "HAS_RUNBOOK",
r.summary = "Internal Service has an onboarding runbook.";

MATCH (runbook:InternalGraphNode {node_id: $tenantId + ":runbook:internal-service-onboarding"})
MATCH (doc:InternalGraphNode {node_id: $tenantId + ":document:internal-readme"})
MERGE (runbook)-[r:DOCUMENTS {
relationship_id: $tenantId + ":rel:internal-runbook-documents-readme"
}]->(doc)
SET r.relationship_type = "DOCUMENTS",
r.summary = "Internal Service onboarding runbook points to README.";