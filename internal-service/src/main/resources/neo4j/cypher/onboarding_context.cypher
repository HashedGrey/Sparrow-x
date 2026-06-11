// ============================================================
// SparrowX Internal Service
// onboarding_context.cypher
// ============================================================
//
// Purpose:
// - Seed onboarding paths and tasks.
// - Connect paths to modules.
// - Connect tasks to documents/runbooks.
// - Connect learning graph topics/objectives.
// - Safe to rerun.
//
// Required param:
// :param tenantId => "default-tenant";
//
// ============================================================


// ============================================================
// Company onboarding path
// ============================================================

MERGE (internalPath:InternalGraphNode {
node_id: $tenantId + ":onboarding_path:internal-service-path"
})
SET internalPath.tenant_id = $tenantId,
internalPath.graph_type = "COMPANY",
internalPath.node_type = "ONBOARDING_PATH",
internalPath.label = "Internal Service Onboarding Path",
internalPath.summary = "Learning path for understanding Internal Service verticals, persistence, gRPC, onboarding, permissions, and graph reads.",
internalPath.entity_id = "internal-service-path";

MATCH (path:InternalGraphNode {node_id: $tenantId + ":onboarding_path:internal-service-path"})
MATCH (module:InternalGraphNode {node_id: $tenantId + ":module:internal-service"})
MERGE (path)-[r:BELONGS_TO {
relationship_id: $tenantId + ":rel:internal-path-belongs-to-internal-service"
}]->(module)
SET r.relationship_type = "BELONGS_TO",
r.summary = "Internal Service onboarding path belongs to Internal Service.";


// ============================================================
// Company onboarding task nodes
// ============================================================

MERGE (taskProto:InternalGraphNode {
node_id: $tenantId + ":onboarding_task:read-internal-proto"
})
SET taskProto.tenant_id = $tenantId,
taskProto.graph_type = "COMPANY",
taskProto.node_type = "ONBOARDING_TASK",
taskProto.label = "Read Internal Proto",
taskProto.summary = "Understand Internal Service public gRPC contract.",
taskProto.entity_id = "read-internal-proto";

MERGE (taskVerticals:InternalGraphNode {
node_id: $tenantId + ":onboarding_task:understand-verticals"
})
SET taskVerticals.tenant_id = $tenantId,
taskVerticals.graph_type = "COMPANY",
taskVerticals.node_type = "ONBOARDING_TASK",
taskVerticals.label = "Understand Vertical Slices",
taskVerticals.summary = "Understand command/query/handler/repository/mapper/model flow per internal entity.",
taskVerticals.entity_id = "understand-verticals";

MERGE (taskGraphReads:InternalGraphNode {
node_id: $tenantId + ":onboarding_task:understand-graph-reads"
})
SET taskGraphReads.tenant_id = $tenantId,
taskGraphReads.graph_type = "COMPANY",
taskGraphReads.node_type = "ONBOARDING_TASK",
taskGraphReads.label = "Understand Graph Reads",
taskGraphReads.summary = "Understand read-only company graph and learning graph access.",
taskGraphReads.entity_id = "understand-graph-reads";

MERGE (taskPolicies:InternalGraphNode {
node_id: $tenantId + ":onboarding_task:understand-policies"
})
SET taskPolicies.tenant_id = $tenantId,
taskPolicies.graph_type = "COMPANY",
taskPolicies.node_type = "ONBOARDING_TASK",
taskPolicies.label = "Understand Policies",
taskPolicies.summary = "Understand access, visibility, permission, cache, and resilience policy shells.",
taskPolicies.entity_id = "understand-policies";


// ============================================================
// Path -> task relationships
// ============================================================

MATCH (path:InternalGraphNode {node_id: $tenantId + ":onboarding_path:internal-service-path"})
MATCH (task:InternalGraphNode {node_id: $tenantId + ":onboarding_task:read-internal-proto"})
MERGE (path)-[r:HAS_TASK {
relationship_id: $tenantId + ":rel:internal-path-has-read-proto-task"
}]->(task)
SET r.relationship_type = "HAS_TASK",
r.summary = "Internal Service onboarding path includes reading the proto.",
r.sort_order = 1;

MATCH (path:InternalGraphNode {node_id: $tenantId + ":onboarding_path:internal-service-path"})
MATCH (task:InternalGraphNode {node_id: $tenantId + ":onboarding_task:understand-verticals"})
MERGE (path)-[r:HAS_TASK {
relationship_id: $tenantId + ":rel:internal-path-has-verticals-task"
}]->(task)
SET r.relationship_type = "HAS_TASK",
r.summary = "Internal Service onboarding path includes understanding vertical slices.",
r.sort_order = 2;

MATCH (path:InternalGraphNode {node_id: $tenantId + ":onboarding_path:internal-service-path"})
MATCH (task:InternalGraphNode {node_id: $tenantId + ":onboarding_task:understand-graph-reads"})
MERGE (path)-[r:HAS_TASK {
relationship_id: $tenantId + ":rel:internal-path-has-graph-reads-task"
}]->(task)
SET r.relationship_type = "HAS_TASK",
r.summary = "Internal Service onboarding path includes understanding graph reads.",
r.sort_order = 3;

MATCH (path:InternalGraphNode {node_id: $tenantId + ":onboarding_path:internal-service-path"})
MATCH (task:InternalGraphNode {node_id: $tenantId + ":onboarding_task:understand-policies"})
MERGE (path)-[r:HAS_TASK {
relationship_id: $tenantId + ":rel:internal-path-has-policies-task"
}]->(task)
SET r.relationship_type = "HAS_TASK",
r.summary = "Internal Service onboarding path includes understanding policy shells.",
r.sort_order = 4;


// ============================================================
// Task -> document/runbook relationships
// ============================================================

MATCH (task:InternalGraphNode {node_id: $tenantId + ":onboarding_task:read-internal-proto"})
MATCH (doc:InternalGraphNode {node_id: $tenantId + ":document:internal-proto"})
MERGE (task)-[r:DOCUMENTS {
relationship_id: $tenantId + ":rel:read-proto-task-documents-proto"
}]->(doc)
SET r.relationship_type = "DOCUMENTS",
r.summary = "Read Internal Proto task points to the Internal Service proto.";

MATCH (task:InternalGraphNode {node_id: $tenantId + ":onboarding_task:understand-verticals"})
MATCH (doc:InternalGraphNode {node_id: $tenantId + ":document:internal-readme"})
MERGE (task)-[r:DOCUMENTS {
relationship_id: $tenantId + ":rel:verticals-task-documents-readme"
}]->(doc)
SET r.relationship_type = "DOCUMENTS",
r.summary = "Understand Vertical Slices task points to Internal Service README.";

MATCH (task:InternalGraphNode {node_id: $tenantId + ":onboarding_task:understand-graph-reads"})
MATCH (runbook:InternalGraphNode {node_id: $tenantId + ":runbook:internal-service-onboarding"})
MERGE (task)-[r:HAS_RUNBOOK {
relationship_id: $tenantId + ":rel:graph-reads-task-has-runbook"
}]->(runbook)
SET r.relationship_type = "HAS_RUNBOOK",
r.summary = "Understand Graph Reads task uses the Internal Service onboarding runbook.";

MATCH (task:InternalGraphNode {node_id: $tenantId + ":onboarding_task:understand-policies"})
MATCH (runbook:InternalGraphNode {node_id: $tenantId + ":runbook:internal-service-onboarding"})
MERGE (task)-[r:HAS_RUNBOOK {
relationship_id: $tenantId + ":rel:policies-task-has-runbook"
}]->(runbook)
SET r.relationship_type = "HAS_RUNBOOK",
r.summary = "Understand Policies task uses the Internal Service onboarding runbook.";


// ============================================================
// Learning graph topics
// ============================================================

MERGE (topicDistributedSystems:InternalGraphNode {
node_id: $tenantId + ":learning_topic:distributed-systems"
})
SET topicDistributedSystems.tenant_id = $tenantId,
topicDistributedSystems.graph_type = "LEARNING",
topicDistributedSystems.node_type = "LEARNING_TOPIC",
topicDistributedSystems.label = "Distributed Systems",
topicDistributedSystems.summary = "Core distributed systems concepts used across SparrowX services.",
topicDistributedSystems.entity_id = "distributed-systems";

MERGE (topicGrpc:InternalGraphNode {
node_id: $tenantId + ":learning_topic:grpc"
})
SET topicGrpc.tenant_id = $tenantId,
topicGrpc.graph_type = "LEARNING",
topicGrpc.node_type = "LEARNING_TOPIC",
topicGrpc.label = "gRPC",
topicGrpc.summary = "Contract-first service communication using Protocol Buffers and gRPC.",
topicGrpc.entity_id = "grpc";

MERGE (topicCqrs:InternalGraphNode {
node_id: $tenantId + ":learning_topic:cqrs"
})
SET topicCqrs.tenant_id = $tenantId,
topicCqrs.graph_type = "LEARNING",
topicCqrs.node_type = "LEARNING_TOPIC",
topicCqrs.label = "CQRS",
topicCqrs.summary = "Command/query separation using CommandBus and QueryBus.",
topicCqrs.entity_id = "cqrs";

MERGE (topicKnowledgeGraph:InternalGraphNode {
node_id: $tenantId + ":learning_topic:knowledge-graph"
})
SET topicKnowledgeGraph.tenant_id = $tenantId,
topicKnowledgeGraph.graph_type = "LEARNING",
topicKnowledgeGraph.node_type = "LEARNING_TOPIC",
topicKnowledgeGraph.label = "Knowledge Graph",
topicKnowledgeGraph.summary = "Graph representation of company and learning relationships.",
topicKnowledgeGraph.entity_id = "knowledge-graph";

MERGE (topicUnitOfWork:InternalGraphNode {
node_id: $tenantId + ":learning_topic:unit-of-work"
})
SET topicUnitOfWork.tenant_id = $tenantId,
topicUnitOfWork.graph_type = "LEARNING",
topicUnitOfWork.node_type = "LEARNING_TOPIC",
topicUnitOfWork.label = "Unit of Work",
topicUnitOfWork.summary = "Transaction boundary and domain event publishing pattern used by command bus execution.",
topicUnitOfWork.entity_id = "unit-of-work";


// ============================================================
// Learning objectives
// ============================================================

MERGE (objectiveReadGraph:InternalGraphNode {
node_id: $tenantId + ":learning_objective:read-graph-context"
})
SET objectiveReadGraph.tenant_id = $tenantId,
objectiveReadGraph.graph_type = "LEARNING",
objectiveReadGraph.node_type = "LEARNING_OBJECTIVE",
objectiveReadGraph.label = "Read graph context",
objectiveReadGraph.summary = "Learn how Agentic Service reads graph context from Internal Service.",
objectiveReadGraph.entity_id = "read-graph-context";

MERGE (objectiveBuildVerticals:InternalGraphNode {
node_id: $tenantId + ":learning_objective:build-service-verticals"
})
SET objectiveBuildVerticals.tenant_id = $tenantId,
objectiveBuildVerticals.graph_type = "LEARNING",
objectiveBuildVerticals.node_type = "LEARNING_OBJECTIVE",
objectiveBuildVerticals.label = "Build service verticals",
objectiveBuildVerticals.summary = "Learn how commands, queries, handlers, repositories, mappers, and gRPC adapters form a vertical.",
objectiveBuildVerticals.entity_id = "build-service-verticals";

MERGE (objectiveUseUnitOfWork:InternalGraphNode {
node_id: $tenantId + ":learning_objective:use-unit-of-work"
})
SET objectiveUseUnitOfWork.tenant_id = $tenantId,
objectiveUseUnitOfWork.graph_type = "LEARNING",
objectiveUseUnitOfWork.node_type = "LEARNING_OBJECTIVE",
objectiveUseUnitOfWork.label = "Use Unit of Work",
objectiveUseUnitOfWork.summary = "Learn why handlers avoid transaction annotations and let buses/unit-of-work own boundaries.",
objectiveUseUnitOfWork.entity_id = "use-unit-of-work";


// ============================================================
// Learning relationships
// ============================================================

MATCH (grpc:InternalGraphNode {node_id: $tenantId + ":learning_topic:grpc"})
MATCH (ds:InternalGraphNode {node_id: $tenantId + ":learning_topic:distributed-systems"})
MERGE (grpc)-[r:DEPENDS_ON {
relationship_id: $tenantId + ":rel:grpc-depends-on-distributed-systems"
}]->(ds)
SET r.relationship_type = "DEPENDS_ON",
r.summary = "gRPC builds on distributed systems concepts.";

MATCH (cqrs:InternalGraphNode {node_id: $tenantId + ":learning_topic:cqrs"})
MATCH (ds:InternalGraphNode {node_id: $tenantId + ":learning_topic:distributed-systems"})
MERGE (cqrs)-[r:RELATED_TO {
relationship_id: $tenantId + ":rel:cqrs-related-to-distributed-systems"
}]->(ds)
SET r.relationship_type = "RELATED_TO",
r.summary = "CQRS is related to distributed service architecture.";

MATCH (uow:InternalGraphNode {node_id: $tenantId + ":learning_topic:unit-of-work"})
MATCH (cqrs:InternalGraphNode {node_id: $tenantId + ":learning_topic:cqrs"})
MERGE (uow)-[r:RELATED_TO {
relationship_id: $tenantId + ":rel:uow-related-to-cqrs"
}]->(cqrs)
SET r.relationship_type = "RELATED_TO",
r.summary = "Unit of Work is used by command bus execution in the CQRS pattern.";

MATCH (kg:InternalGraphNode {node_id: $tenantId + ":learning_topic:knowledge-graph"})
MATCH (objective:InternalGraphNode {node_id: $tenantId + ":learning_objective:read-graph-context"})
MERGE (kg)-[r:HAS_TASK {
relationship_id: $tenantId + ":rel:kg-has-read-graph-objective"
}]->(objective)
SET r.relationship_type = "HAS_TASK",
r.summary = "Knowledge graph topic includes reading graph context.";

MATCH (cqrs:InternalGraphNode {node_id: $tenantId + ":learning_topic:cqrs"})
MATCH (objective:InternalGraphNode {node_id: $tenantId + ":learning_objective:build-service-verticals"})
MERGE (cqrs)-[r:HAS_TASK {
relationship_id: $tenantId + ":rel:cqrs-has-build-verticals-objective"
}]->(objective)
SET r.relationship_type = "HAS_TASK",
r.summary = "CQRS topic includes building service verticals.";

MATCH (uow:InternalGraphNode {node_id: $tenantId + ":learning_topic:unit-of-work"})
MATCH (objective:InternalGraphNode {node_id: $tenantId + ":learning_objective:use-unit-of-work"})
MERGE (uow)-[r:HAS_TASK {
relationship_id: $tenantId + ":rel:uow-has-use-unit-of-work-objective"
}]->(objective)
SET r.relationship_type = "HAS_TASK",
r.summary = "Unit of Work topic includes learning command-bus transaction boundaries.";

MATCH (objective:InternalGraphNode {node_id: $tenantId + ":learning_objective:read-graph-context"})
MATCH (internal:InternalGraphNode {node_id: $tenantId + ":module:internal-service"})
MERGE (objective)-[r:RELATED_TO {
relationship_id: $tenantId + ":rel:read-graph-objective-related-to-internal-service"
}]->(internal)
SET r.relationship_type = "RELATED_TO",
r.summary = "Reading graph context is practiced through Internal Service.";

MATCH (objective:InternalGraphNode {node_id: $tenantId + ":learning_objective:build-service-verticals"})
MATCH (internal:InternalGraphNode {node_id: $tenantId + ":module:internal-service"})
MERGE (objective)-[r:RELATED_TO {
relationship_id: $tenantId + ":rel:build-verticals-objective-related-to-internal-service"
}]->(internal)
SET r.relationship_type = "RELATED_TO",
r.summary = "Building service verticals is practiced through Internal Service.";

MATCH (objective:InternalGraphNode {node_id: $tenantId + ":learning_objective:use-unit-of-work"})
MATCH (internal:InternalGraphNode {node_id: $tenantId + ":module:internal-service"})
MERGE (objective)-[r:RELATED_TO {
relationship_id: $tenantId + ":rel:unit-of-work-objective-related-to-internal-service"
}]->(internal)
SET r.relationship_type = "RELATED_TO",
r.summary = "Unit of Work is practiced by refactoring Internal Service handlers.";