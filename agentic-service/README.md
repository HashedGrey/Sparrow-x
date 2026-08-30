# SparrowX Agentic Service

## Overview

The Agentic Service is the orchestration layer responsible for executing durable, multi-step knowledge missions across internal company data and documents.

It exposes gRPC operations for submitting missions, streaming progress, retrieving results, managing human approvals, and cancelling active missions.

Commands and queries use the shared `CommandBus`, `QueryBus`, and `EventBus`.

## Implementation Progress

🟩 **Mission API, lifecycle and persistence** ██████████ **100%**

🟩 **Request idempotency and deterministic mission identity** ██████████ **100%**

🟩 **Temporal workflow orchestration foundation** █████████░ **95%**

🟩 **Embabel intent classification and planning** ██████████ **100%**

🟩 **Internal Service capability integration** ██████████ **100%**

🟩 **Document Service capability architecture** █████████░ **95%**

🟩 **Evidence collection and mission evidence model** █████████░ **95%**

🟩 **Grounded synthesis and citation pipeline** █████████░ **90%**

🟩 **Policy, budget and execution guardrails** █████████░ **90%**

🟩 **FAST mission execution path** █████████░ **90%**

⬛ **Fine-grained RESEARCH execution path** ███░░░░░░░ **30%**

⬛ **Research checkpoint/replanning loop** ██░░░░░░░░ **20%**

⬛ **Durable human-in-the-loop research gates** ██░░░░░░░░ **20%**

⬛ **Long-running multi-hop research execution** █░░░░░░░░░ **10%**

The heavy shared core required by both FAST and RESEARCH execution is implemented. The primary remaining work is converting the existing reasoning and capability layer into the fine-grained, durable RESEARCH execution model.

## Temporal Orchestration

Each accepted mission is persisted and started using a stable Temporal Workflow ID.

Temporal owns:

* Activity scheduling and infrastructure retries
* Cancellation
* Durable human-gate waiting
* Workflow Updates
* Recovery and replay
* Continue-As-New decisions

PostgreSQL stores business projections, progress events, approval records, mission state, and external checkpoints.

### FAST Mode

FAST missions currently execute Embabel reasoning inside a coarse-grained Temporal Activity.

The Activity performs the mission reasoning pipeline:

```text
MissionRunInput
  → IntentState
  → PlanState
  → MissionEvidence
  → MissionResult
```

This provides durable mission-level execution while keeping short and medium-complexity requests inexpensive.

A failed Activity can be retried by Temporal, but intermediate Embabel reasoning steps are not individually persisted into Temporal history.

**FAST mode:** 🟩 █████████░ **90%**

### RESEARCH Mode

RESEARCH will use finer-grained Temporal durability for long-running, multi-hop missions.

Instead of wrapping the complete agent run in one Activity, the Workflow will persist meaningful execution boundaries between planning, capability execution, observations, replanning, human gates, and synthesis.

Conceptually:

```text
Plan / Review
     ↓
Execute Capability
     ↓
Persist Observation
     ↓
Replan
     ↓
Execute Capability
     ↓
...
     ↓
Synthesize
```

The shared reasoning, evidence, capability, persistence, and policy foundations already exist. The remaining work is primarily orchestration and checkpoint granularity.

**RESEARCH mode:** ⬛ ███░░░░░░░ **30%**

## Embabel Agent Reasoning

Embabel performs bounded reasoning inside Temporal Activities.

It:

* interprets mission intent
* creates mission plans
* selects authorized capabilities
* collects mission evidence
* reasons over observations
* determines whether sufficient evidence exists
* synthesizes grounded mission results

Current Embabel graph:

```text
MissionRunInput
  → IntentState
  → PlanState
  → MissionEvidence
  → MissionResult
```

Intent and planning execute through Embabel `OperationContext` using the configured LLM.

Structured LLM calls are permitted only inside Temporal Activities.

**Embabel reasoning core:** 🟩 █████████░ **95%**

## Service Integrations

### Internal Service

Internal Service provides structured company knowledge including:

* entity resolution
* teams and engineers
* services and repositories
* ownership relationships
* onboarding structures
* company graph queries
* learning graph queries

The agent decides when structured internal context is required and invokes the appropriate capability.

**Internal Service integration:** 🟩 ██████████ **100%**

### Document Service

Document Service provides unstructured knowledge capabilities including:

* document uploads
* ingestion tracking
* hybrid document retrieval
* document span search
* evidence construction
* evidence verification
* citations

Document evidence and Internal Service evidence are normalized into mission evidence before final synthesis.

**Document Service integration:** 🟩 █████████░ **95%**

## Evidence and Grounding

Mission execution separates retrieval from final synthesis.

Evidence is collected first and represented explicitly as mission evidence.

Final synthesis operates over registered evidence rather than unrestricted model context.

Grounding policies enforce:

* tenant isolation
* source authorization
* tool authorization
* evidence provenance
* citation coverage
* budget limits
* redaction
* grounding validation

Final answers are produced only from evidence available to the mission.

**Evidence and grounding:** 🟩 █████████░ **95%**

## Human Approval

The mission lifecycle supports a durable waiting state for approval:

```text
RUNNING
   ↓
WAITING_APPROVAL
   ↓
RUNNING
```

Approval and rejection commands are part of the mission architecture.

The remaining work is primarily integrating approval decisions into the future fine-grained RESEARCH planning loop so the agent can intentionally request human intervention during long-running missions.

**Human approval foundation:** 🟩 ████████░░ **80%**

**Research HITL integration:** ⬛ ██░░░░░░░░ **20%**

## Persistence and Idempotency

Mission persistence supports:

* deterministic mission IDs
* request fingerprint validation
* idempotent submission
* mission lifecycle projections
* external checkpoint persistence
* checkpoint integrity hashes
* replay-safe state restoration

Temporal provides orchestration durability while PostgreSQL stores application-owned business state and externally inspectable checkpoints.

**Persistence foundation:** 🟩 ██████████ **100%**

## Governance

Execution is constrained by mission policies and budgets.

Mission budgets can bound:

* LLM calls
* tool calls
* retrieval queries
* hydrated items
* input tokens
* output tokens
* cost

Capabilities execute only after authorization and policy checks.

All external mutations must remain idempotent, observable, replay-safe, and policy-governed.

**Governance foundation:** 🟩 █████████░ **90%**

## Technology

The service targets:

* Java 24+
* Spring Boot 3.x
* gRPC
* Embabel
* Temporal
* PostgreSQL
* OpenTelemetry
* Langfuse-compatible observability

## Current State

The core Agentic Service architecture is substantially implemented.

```text
Shared Agent Core        🟩 █████████░  95%
FAST Execution           🟩 █████████░  90%
RESEARCH Execution       ⬛ ███░░░░░░░  30%
Overall Service          🟩 ████████░░  80%
```

The major remaining architectural milestone is the RESEARCH path: converting long-running missions from one coarse-grained agent execution into a durable sequence of Temporal-managed reasoning, execution, observation, replanning, and human-intervention boundaries.
