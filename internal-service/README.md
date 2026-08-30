# SparrowX Internal Service

## Overview

The Internal Service owns SparrowX structured company knowledge.

It manages engineers, teams, repositories, modules, runbooks, internal documents, permissions, onboarding paths, onboarding tasks, onboarding progress, and internal knowledge graphs.

The service exposes these capabilities over gRPC for use by other SparrowX services, especially Agentic Service.

## Implementation Progress

🟩 **Domain models and value objects** ██████████ **100%**

🟩 **PostgreSQL persistence** ██████████ **100%**

🟩 **Neo4j graph queries** ██████████ **100%**

🟩 **Engineer management** ██████████ **100%**

🟩 **Team management** ██████████ **100%**

🟩 **Repository management** ██████████ **100%**

🟩 **Module management** ██████████ **100%**

🟩 **Runbook management** ██████████ **100%**

🟩 **Internal document management** ██████████ **100%**

🟩 **Permission management** ██████████ **100%**

🟩 **Onboarding paths and tasks** ██████████ **100%**

🟩 **Onboarding progress tracking** ██████████ **100%**

🟩 **Internal entity search** ██████████ **100%**

🟩 **Internal graph context** ██████████ **100%**

🟩 **gRPC API and policies** ██████████ **100%**

```text
Overall                         🟩 ██████████ 100%
```

## Structured Knowledge Model

The service models internal engineering knowledge including:

* engineers
* teams
* repositories
* modules
* runbooks
* internal documents
* permissions
* onboarding paths
* onboarding tasks
* onboarding assignments
* onboarding task progress

Strong domain value objects are used for identifiers, roles, statuses, permission scopes, repository providers, graph types, and timestamps.

## Persistence

PostgreSQL stores the authoritative structured business state.

Persistence includes dedicated entities, repositories, and mappers for:

* engineers
* onboarding assignments
* onboarding progress
* internal documents
* modules
* onboarding paths
* onboarding tasks
* permissions
* repositories
* runbooks
* teams

## Internal Graph

Neo4j provides graph-oriented access to company knowledge.

Supported graph operations include:

```text
InternalAgentGraphQuery
InternalGraphPathQuery
LearningGraphQuery
```

The graph layer supports navigation across internal entities and relationships without forcing graph semantics into the relational persistence model.

## Internal Graph Context

Agent-facing graph context is exposed through:

```text
GetInternalGraphContext
```

This converts relevant internal entities and graph relationships into a structured context suitable for downstream agent reasoning.

## Internal Entity Search

The service exposes structured entity discovery through:

```text
SearchInternalEntities
```

Search supports resolution across internal entity types and returns normalized results containing:

* entity identity
* node type
* label
* slug
* summary
* relevance score
* match reason
* parent relationships
* attributes

This provides Agentic Service with a general-purpose structured retrieval capability.

## Engineers

Engineer features support:

```text
CreateEngineer
GetEngineer
```

Engineer data can participate in onboarding, team relationships, permissions, and internal graph queries.

## Teams

Team features support:

```text
CreateTeam
GetTeam
```

Teams act as core ownership boundaries for services, repositories, modules, onboarding structures, and access control.

## Repositories

Repository features support:

```text
CreateRepository
GetRepository
```

Repositories represent source-control assets and their relationship to teams and internal engineering systems.

## Modules

Module features support:

```text
CreateModule
GetModule
```

Modules provide a structured representation of service or system components and their ownership and criticality.

## Runbooks

Runbook features support:

```text
CreateRunbook
GetRunbook
```

Runbooks provide operational knowledge that can be referenced directly or associated with onboarding tasks and internal graph relationships.

## Internal Documents

Internal metadata records for documents are managed through:

```text
CreateDocument
GetDocument
```

Document content processing remains owned by Document Service, while Internal Service stores structured company relationships involving those documents.

## Permissions

Permission features support:

```text
CreatePermission
GetPermission
```

Permission models include:

* actions
* scopes
* target entities
* tenant boundaries

Permission resolution participates in internal access-policy enforcement.

## Onboarding

The onboarding domain supports:

```text
CreateOnboardingPath
CreateOnboardingTask
AssignEngineerToOnboardingPath
CompleteOnboardingTask
GetEngineerOnboardingProgress
```

This allows SparrowX to represent developer onboarding as structured, queryable company knowledge.

Progress is tracked independently for engineers and tasks.

## gRPC API

The service exposes its application features through `InternalServiceGrpcImpl`.

Transport-level infrastructure includes:

* tenant context propagation
* policy enforcement
* resilience policies
* access policies
* caching policies
* onboarding visibility rules
* permission resolution

## Security and Tenant Isolation

Internal Service enforces tenant-aware access at the service boundary.

Policy components include:

```text
InternalAccessPolicy
InternalCachePolicy
InternalResiliencePolicy
OnboardingVisibilityPolicy
PermissionResolutionPolicy
```

Tenant context is propagated through dedicated gRPC interception.

## Architecture

```text
Internal Service
│
├── Features
│   ├── Engineers
│   ├── Teams
│   ├── Repositories
│   ├── Modules
│   ├── Runbooks
│   ├── Documents
│   ├── Permissions
│   ├── Onboarding
│   ├── Entity Search
│   └── Graph Context
│
├── PostgreSQL
│   └── Structured business state
│
├── Neo4j
│   └── Graph relationships and traversal
│
└── gRPC
    └── Service integration boundary
```

## Technology

The service uses:

* Java 24+
* Spring Boot
* gRPC
* PostgreSQL
* JPA / Hibernate
* Neo4j
* SparrowX Building Blocks

## Current State

Internal Service is feature-complete.

```text
Domain Model                     🟩 ██████████ 100%
Persistence                      🟩 ██████████ 100%
Graph Queries                    🟩 ██████████ 100%
Entity Search                    🟩 ██████████ 100%
Onboarding                       🟩 ██████████ 100%
Permissions                      🟩 ██████████ 100%
gRPC API                         🟩 ██████████ 100%
Agent-facing Context             🟩 ██████████ 100%

Overall                          🟩 ██████████ 100%
```

Future changes should primarily extend the structured company knowledge model rather than complete missing architectural foundations.
