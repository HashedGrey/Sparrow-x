// ============================================================
// SparrowX Internal Service
// permission_context.cypher
// ============================================================
//
// Purpose:
// - Seed permission nodes.
// - Connect modules/API contexts to required permissions.
// - Safe to rerun.
//
// Required param:
// :param tenantId => "default-tenant";
//
// ============================================================


// ============================================================
// Permission nodes
// ============================================================

MERGE (permGraphRead:InternalGraphNode {
    node_id: $tenantId + ":permission:INTERNAL_GRAPH_READ"
})
SET permGraphRead.tenant_id = $tenantId,
    permGraphRead.graph_type = "COMPANY",
    permGraphRead.node_type = "PERMISSION",
    permGraphRead.label = "INTERNAL_GRAPH_READ",
    permGraphRead.summary = "Permission to read internal company graph and learning graph context.",
    permGraphRead.entity_id = "INTERNAL_GRAPH_READ";

MERGE (permEngineerRead:InternalGraphNode {
    node_id: $tenantId + ":permission:INTERNAL_ENGINEER_READ"
})
SET permEngineerRead.tenant_id = $tenantId,
    permEngineerRead.graph_type = "COMPANY",
    permEngineerRead.node_type = "PERMISSION",
    permEngineerRead.label = "INTERNAL_ENGINEER_READ",
    permEngineerRead.summary = "Permission to read engineer records.",
    permEngineerRead.entity_id = "INTERNAL_ENGINEER_READ";

MERGE (permEngineerManage:InternalGraphNode {
    node_id: $tenantId + ":permission:INTERNAL_ENGINEER_MANAGE"
})
SET permEngineerManage.tenant_id = $tenantId,
    permEngineerManage.graph_type = "COMPANY",
    permEngineerManage.node_type = "PERMISSION",
    permEngineerManage.label = "INTERNAL_ENGINEER_MANAGE",
    permEngineerManage.summary = "Permission to create or manage engineer records.",
    permEngineerManage.entity_id = "INTERNAL_ENGINEER_MANAGE";

MERGE (permStructureRead:InternalGraphNode {
    node_id: $tenantId + ":permission:INTERNAL_STRUCTURE_READ"
})
SET permStructureRead.tenant_id = $tenantId,
    permStructureRead.graph_type = "COMPANY",
    permStructureRead.node_type = "PERMISSION",
    permStructureRead.label = "INTERNAL_STRUCTURE_READ",
    permStructureRead.summary = "Permission to read teams, modules, repositories, documents, and runbooks.",
    permStructureRead.entity_id = "INTERNAL_STRUCTURE_READ";

MERGE (permStructureManage:InternalGraphNode {
    node_id: $tenantId + ":permission:INTERNAL_STRUCTURE_MANAGE"
})
SET permStructureManage.tenant_id = $tenantId,
    permStructureManage.graph_type = "COMPANY",
    permStructureManage.node_type = "PERMISSION",
    permStructureManage.label = "INTERNAL_STRUCTURE_MANAGE",
    permStructureManage.summary = "Permission to create or manage teams, modules, repositories, documents, and runbooks.",
    permStructureManage.entity_id = "INTERNAL_STRUCTURE_MANAGE";

MERGE (permOnboardingRead:InternalGraphNode {
    node_id: $tenantId + ":permission:INTERNAL_ONBOARDING_READ"
})
SET permOnboardingRead.tenant_id = $tenantId,
    permOnboardingRead.graph_type = "COMPANY",
    permOnboardingRead.node_type = "PERMISSION",
    permOnboardingRead.label = "INTERNAL_ONBOARDING_READ",
    permOnboardingRead.summary = "Permission to read onboarding paths, tasks, assignments, and progress.",
    permOnboardingRead.entity_id = "INTERNAL_ONBOARDING_READ";

MERGE (permOnboardingManage:InternalGraphNode {
    node_id: $tenantId + ":permission:INTERNAL_ONBOARDING_MANAGE"
})
SET permOnboardingManage.tenant_id = $tenantId,
    permOnboardingManage.graph_type = "COMPANY",
    permOnboardingManage.node_type = "PERMISSION",
    permOnboardingManage.label = "INTERNAL_ONBOARDING_MANAGE",
    permOnboardingManage.summary = "Permission to create onboarding paths/tasks and assign/complete onboarding.",
    permOnboardingManage.entity_id = "INTERNAL_ONBOARDING_MANAGE";

MERGE (permPermissionRead:InternalGraphNode {
    node_id: $tenantId + ":permission:INTERNAL_PERMISSION_READ"
})
SET permPermissionRead.tenant_id = $tenantId,
    permPermissionRead.graph_type = "COMPANY",
    permPermissionRead.node_type = "PERMISSION",
    permPermissionRead.label = "INTERNAL_PERMISSION_READ",
    permPermissionRead.summary = "Permission to read internal permission definitions.",
    permPermissionRead.entity_id = "INTERNAL_PERMISSION_READ";

MERGE (permPermissionManage:InternalGraphNode {
    node_id: $tenantId + ":permission:INTERNAL_PERMISSION_MANAGE"
})
SET permPermissionManage.tenant_id = $tenantId,
    permPermissionManage.graph_type = "COMPANY",
    permPermissionManage.node_type = "PERMISSION",
    permPermissionManage.label = "INTERNAL_PERMISSION_MANAGE",
    permPermissionManage.summary = "Permission to create or manage internal permission definitions.",
    permPermissionManage.entity_id = "INTERNAL_PERMISSION_MANAGE";


// ============================================================
// Internal Service -> required permission relationships
// ============================================================

MATCH (internal:InternalGraphNode {node_id: $tenantId + ":module:internal-service"})
MATCH (permission:InternalGraphNode {node_id: $tenantId + ":permission:INTERNAL_GRAPH_READ"})
MERGE (internal)-[r:REQUIRES_PERMISSION {
    relationship_id: $tenantId + ":rel:internal-requires-graph-read"
}]->(permission)
SET r.relationship_type = "REQUIRES_PERMISSION",
    r.summary = "Internal Service graph read APIs require graph read permission.";

MATCH (internal:InternalGraphNode {node_id: $tenantId + ":module:internal-service"})
MATCH (permission:InternalGraphNode {node_id: $tenantId + ":permission:INTERNAL_ENGINEER_READ"})
MERGE (internal)-[r:REQUIRES_PERMISSION {
    relationship_id: $tenantId + ":rel:internal-requires-engineer-read"
}]->(permission)
SET r.relationship_type = "REQUIRES_PERMISSION",
    r.summary = "Internal Service engineer read APIs require engineer read permission.";

MATCH (internal:InternalGraphNode {node_id: $tenantId + ":module:internal-service"})
MATCH (permission:InternalGraphNode {node_id: $tenantId + ":permission:INTERNAL_ENGINEER_MANAGE"})
MERGE (internal)-[r:REQUIRES_PERMISSION {
    relationship_id: $tenantId + ":rel:internal-requires-engineer-manage"
}]->(permission)
SET r.relationship_type = "REQUIRES_PERMISSION",
    r.summary = "Internal Service engineer mutation APIs require engineer management permission.";

MATCH (internal:InternalGraphNode {node_id: $tenantId + ":module:internal-service"})
MATCH (permission:InternalGraphNode {node_id: $tenantId + ":permission:INTERNAL_STRUCTURE_READ"})
MERGE (internal)-[r:REQUIRES_PERMISSION {
    relationship_id: $tenantId + ":rel:internal-requires-structure-read"
}]->(permission)
SET r.relationship_type = "REQUIRES_PERMISSION",
    r.summary = "Internal Service structure read APIs require structure read permission.";

MATCH (internal:InternalGraphNode {node_id: $tenantId + ":module:internal-service"})
MATCH (permission:InternalGraphNode {node_id: $tenantId + ":permission:INTERNAL_STRUCTURE_MANAGE"})
MERGE (internal)-[r:REQUIRES_PERMISSION {
    relationship_id: $tenantId + ":rel:internal-requires-structure-manage"
}]->(permission)
SET r.relationship_type = "REQUIRES_PERMISSION",
    r.summary = "Internal Service structure mutation APIs require structure management permission.";

MATCH (internal:InternalGraphNode {node_id: $tenantId + ":module:internal-service"})
MATCH (permission:InternalGraphNode {node_id: $tenantId + ":permission:INTERNAL_ONBOARDING_READ"})
MERGE (internal)-[r:REQUIRES_PERMISSION {
    relationship_id: $tenantId + ":rel:internal-requires-onboarding-read"
}]->(permission)
SET r.relationship_type = "REQUIRES_PERMISSION",
    r.summary = "Internal Service onboarding read APIs require onboarding read permission.";

MATCH (internal:InternalGraphNode {node_id: $tenantId + ":module:internal-service"})
MATCH (permission:InternalGraphNode {node_id: $tenantId + ":permission:INTERNAL_ONBOARDING_MANAGE"})
MERGE (internal)-[r:REQUIRES_PERMISSION {
    relationship_id: $tenantId + ":rel:internal-requires-onboarding-manage"
}]->(permission)
SET r.relationship_type = "REQUIRES_PERMISSION",
    r.summary = "Internal Service onboarding mutation APIs require onboarding management permission.";

MATCH (internal:InternalGraphNode {node_id: $tenantId + ":module:internal-service"})
MATCH (permission:InternalGraphNode {node_id: $tenantId + ":permission:INTERNAL_PERMISSION_READ"})
MERGE (internal)-[r:REQUIRES_PERMISSION {
    relationship_id: $tenantId + ":rel:internal-requires-permission-read"
}]->(permission)
SET r.relationship_type = "REQUIRES_PERMISSION",
    r.summary = "Internal Service permission read APIs require permission read permission.";

MATCH (internal:InternalGraphNode {node_id: $tenantId + ":module:internal-service"})
MATCH (permission:InternalGraphNode {node_id: $tenantId + ":permission:INTERNAL_PERMISSION_MANAGE"})
MERGE (internal)-[r:REQUIRES_PERMISSION {
    relationship_id: $tenantId + ":rel:internal-requires-permission-manage"
}]->(permission)
SET r.relationship_type = "REQUIRES_PERMISSION",
    r.summary = "Internal Service permission mutation APIs require permission management permission.";


// ============================================================
// Permission hierarchy / grouping
// ============================================================

MATCH (manage:InternalGraphNode {node_id: $tenantId + ":permission:INTERNAL_ENGINEER_MANAGE"})
MATCH (read:InternalGraphNode {node_id: $tenantId + ":permission:INTERNAL_ENGINEER_READ"})
MERGE (manage)-[r:DEPENDS_ON {
    relationship_id: $tenantId + ":rel:engineer-manage-depends-on-engineer-read"
}]->(read)
SET r.relationship_type = "DEPENDS_ON",
    r.summary = "Engineer management implies engineer reading.";

MATCH (manage:InternalGraphNode {node_id: $tenantId + ":permission:INTERNAL_STRUCTURE_MANAGE"})
MATCH (read:InternalGraphNode {node_id: $tenantId + ":permission:INTERNAL_STRUCTURE_READ"})
MERGE (manage)-[r:DEPENDS_ON {
    relationship_id: $tenantId + ":rel:structure-manage-depends-on-structure-read"
}]->(read)
SET r.relationship_type = "DEPENDS_ON",
    r.summary = "Structure management implies structure reading.";

MATCH (manage:InternalGraphNode {node_id: $tenantId + ":permission:INTERNAL_ONBOARDING_MANAGE"})
MATCH (read:InternalGraphNode {node_id: $tenantId + ":permission:INTERNAL_ONBOARDING_READ"})
MERGE (manage)-[r:DEPENDS_ON {
    relationship_id: $tenantId + ":rel:onboarding-manage-depends-on-onboarding-read"
}]->(read)
SET r.relationship_type = "DEPENDS_ON",
    r.summary = "Onboarding management implies onboarding reading.";

MATCH (manage:InternalGraphNode {node_id: $tenantId + ":permission:INTERNAL_PERMISSION_MANAGE"})
MATCH (read:InternalGraphNode {node_id: $tenantId + ":permission:INTERNAL_PERMISSION_READ"})
MERGE (manage)-[r:DEPENDS_ON {
    relationship_id: $tenantId + ":rel:permission-manage-depends-on-permission-read"
}]->(read)
SET r.relationship_type = "DEPENDS_ON",
    r.summary = "Permission management implies permission reading.";