# SparrowX Internal Service

Internal Service is the read/write source for SparrowX internal company context and learning/onboarding context.

It exposes gRPC APIs for:

- Engineers / learners
- Teams
- Modules / services
- Repositories
- Internal document references
- Runbooks
- Onboarding paths
- Onboarding tasks
- Engineer onboarding assignment/progress
- Permissions
- Read-only company graph reads
- Read-only learning graph reads

## Core rule

The knowledge graph is built or seeded outside the public runtime path.

Runtime APIs only:

1. create/read relational internal entities
2. read existing graph relationships for Agentic Service context


## Runtime shape

```text
Agentic Service
  -> InternalService gRPC
      -> CommandBus / QueryBus
          -> feature handlers
              -> Postgres repositories
              -> Neo4j read client