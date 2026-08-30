# SparrowX Building Blocks

## Overview

`buildingblocks` provides the shared architectural primitives used across SparrowX services.

It centralizes command/query/event dispatch, domain foundations, caching, persistence, messaging, observability, gRPC infrastructure, security, request context, validation, and common exception handling.

The module is intentionally service-agnostic. Business-specific behavior belongs in individual services rather than in building blocks.

## Core Application Buses

### Commands

The command layer provides synchronous and asynchronous command execution through:

* `Command`
* `CommandHandler`
* `CommandBus`
* `AsyncCommandBus`
* execution chains
* command interceptors

Command interceptors provide cross-cutting concerns including:

* authentication
* observability

### Queries

The query layer provides request/response dispatch through:

* `Query`
* `QueryHandler`
* `QueryBus`
* `AsyncQueryBus`
* query execution chains
* query interceptors

Supported cross-cutting query behavior includes:

* authentication
* caching
* logging
* metrics
* tracing

### Events

The event layer supports domain and integration event dispatch through:

* `DomainEvent`
* `IntegrationEvent`
* `EventHandler`
* `EventBus`
* `AsyncEventBus`
* `EventMapper`
* event execution chains

It also provides observability interception and support for internally dispatched commands.

## Domain Foundation

The domain package contains reusable DDD primitives.

### Model

* `AggregateRoot`
* `BaseEntity`

### Domain Events

* `DomainEventBase`

### Domain Exceptions

Common domain failures include:

* aggregate not found
* business rule violations
* optimistic concurrency conflicts
* domain conflicts
* domain not found

These provide a consistent domain error model without coupling domain code to transport concerns.

## Persistence

The persistence layer provides reusable transaction and JPA infrastructure.

### Transactions

* `TransactionManager`
* `UnitOfWork`
* `UnitOfWorkImpl`

### JPA

Shared JPA infrastructure includes:

* auditing
* Hibernate global filtering
* common JPA configuration

## Transactional Outbox

The outbox implementation supports reliable publication of events after business state has been committed.

Components include:

* `OutboxWriter`
* `OutboxRepository`
* `OutboxProcessor`
* `OutboxBackgroundJob`
* `OutboxEventHandler`

Messages track delivery type and lifecycle status.

This allows service state changes and outgoing integration events to participate in the same transactional boundary.

## Inbox and Idempotency

The messaging inbox provides consumer-side deduplication for externally delivered messages.

It includes:

* `InboxMessage`
* `InboxRepository`
* `InboxDeduplicator`

Together with the outbox pattern, this provides the foundation for idempotent and reliable asynchronous integration.

## Messaging

Kafka integration publishing is provided through:

```text
KafkaIntegrationPublisher
```

Internal asynchronous commands can be executed through:

```text
AsyncCommandExecutor
```

Business services remain responsible for defining their own events and handlers.

## Caching

The cache abstraction supports multiple backing implementations:

* Caffeine
* Redis
* hybrid local/distributed caching

Shared components include:

* `CacheProvider`
* `CacheKeyBuilder`
* `CaffeineCacheProvider`
* `RedisCacheProvider`
* `HybridCacheProvider`

This allows application code to depend on a common cache interface rather than a specific cache technology.

## gRPC Infrastructure

The shared gRPC layer provides transport-level cross-cutting behavior.

Interceptors include:

* authentication
* caching
* debugging
* exception translation
* logging
* metrics
* resilience
* tracing

These concerns are applied at the transport boundary rather than duplicated inside individual service handlers.

## Health Checks

Reusable health infrastructure supports:

* PostgreSQL
* Cassandra
* gRPC health reporting
* HTTP liveness endpoints

The gRPC health adapter exposes service health through the standard gRPC health mechanism.

## Observability

Building blocks centralizes common observability primitives.

### Application Observability

* `BaseLogger`
* `BaseMetricsCollector`
* `BaseTracer`
* `BaseObservabilityInterceptor`

### Infrastructure Observability

* OpenTelemetry configuration
* gRPC trace propagation
* logger utilities
* metric registry utilities

Command, query, event, and gRPC pipelines can therefore share consistent telemetry conventions.

## Security

Shared security integration provides:

* Keycloak configuration
* role conversion
* scope conversion
* authentication entry-point handling

Transport authentication is propagated into the shared application request context.

## Request Context

Shared context objects provide consistent propagation of request identity and tracing information:

* `AuthContext`
* `CorrelationContext`
* `RequestContext`

These allow downstream application code to consume authenticated caller and correlation information independently of the transport layer.

## Exception Handling

The shared exception hierarchy defines application-level failures such as:

* bad requests
* validation failures
* authentication failures
* authorization failures
* conflicts
* missing resources
* unavailable services
* internal server failures

Transport adapters are responsible for translating these exceptions into the appropriate protocol response.

## Validation and Utilities

Common utilities include:

* validation helpers
* ID generation
* JSON utilities
* gRPC log formatting

These should contain only behavior that is genuinely shared across multiple services.

## Database Migrations

Building blocks contains shared Flyway configuration and infrastructure migrations for:

```text
inbox
outbox
```

Service-specific schemas and migrations remain owned by their respective services.

## Architecture

The module is organized into five primary layers:

```text
buildingblocks
├── core            # Commands, queries, events and observability pipelines
├── domain          # Shared DDD primitives
├── infrastructure  # Cache, persistence, messaging, gRPC and telemetry
├── security        # Authentication and authorization integration
└── shared          # Context, exceptions, utilities and validation
```

The dependency direction should remain approximately:

```text
Service Business Logic
        │
        ▼
      core
        │
        ├────────► domain
        │
        ▼
 infrastructure
        │
        ▼
 External Systems
```

`domain` and application abstractions should not depend on service-specific infrastructure.

## Design Principles

Building blocks should remain:

* reusable across services
* independent of service-specific business logic
* transport-aware only where explicitly infrastructure-related
* observable by default
* idempotency-friendly
* transaction-safe
* compatible with asynchronous execution
* suitable for multi-tenant service boundaries

New functionality should only be added here when multiple services genuinely require the same abstraction.

## Technology

The module provides shared support for technologies used throughout SparrowX, including:

* Java
* Spring Boot
* gRPC
* JPA / Hibernate
* PostgreSQL
* Redis
* Caffeine
* Kafka
* Flyway
* OpenTelemetry
* Keycloak

## Current State

The core shared infrastructure is substantially implemented.

```text
Command / Query / Event buses   🟩 ██████████ 100%
Domain foundations              🟩 ██████████ 100%
Persistence / Unit of Work      🟩 █████████░  95%
Outbox / Inbox                  🟩 █████████░  95%
Caching                         🟩 █████████░  95%
gRPC infrastructure             🟩 ██████████ 100%
Observability                   🟩 ██████████ 100%
Security                        🟩 ██████████ 100%
Shared context / exceptions     🟩 ██████████ 100%

Overall                         🟩 █████████░  95%
```

The building blocks module should now evolve conservatively. New abstractions should be introduced only when repeated service implementations demonstrate that the concern is genuinely cross-cutting.
