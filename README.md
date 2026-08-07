
# Sparrow-X

## 🔁 Runtime Traffic Flow

![Sparrowx Data Flow](resources/sp-architecture-flow.gif)

*Animated service-to-service data flow inside the Sparrowx mesh.*



SparrowX is an internal knowledge and agentic search system for engineering organizations. 
It connects agentic-service code and system design graph relations, company documents, 
service ownership, onboarding workflows, repositories and internal domain data into 
one searchable, explainable assistant.

## Core Services

SparrowX is built around three core services:

* **agentic-service** - The orchestration layer that receives user missions, parses intent, plans tool calls, invokes internal services, coordinates agent execution, and produces grounded answers.

* **internal-service** - The structured internal system that stores the agentic-service file paths and their graph relations, teams, engineers, services, onboarding paths, tasks, ownership, service metadata, and internal business/domain entities.

* **document-service** - The document intelligence layer that handles document upload, extraction, chunking, hybrid retrieval, vector search, keyword search, citation verification, and evidence graph construction.

* **Building-blocks** - The shared building-blocks foundation. It provides reusable infrastructure used 
across SparrowX services, including command/query handling, proto definitions, validation, observability,
tracing, metrics, exception handling, context propagation, resilience patterns and 
common domain primitives.

## What SparrowX Can Do
### Agentic Guide / Self-Referential System Understanding


By deployment, SparrowX has indexed the `agenticsvc` codebase, architecture, runtime flows,
service relationships, repositories, configuration, runbooks and supporting engineering 
documentation into `intsvc` and `docsvc`.

This makes Agentic Service both the **reasoning system** and the **system being explored**. 
Engineers can ask questions about SparrowX itself and receive explanations grounded in its 
current implementation rather than a fixed tutorial.

- **`agentic-service`** interprets the engineer's question, decides which structured relationships
  and documents are needed, performs multi-hop reasoning across them, and produces a grounded
  explanation with citations.
- **`internal-service`** provides the structured system graph: services, modules, repositories, teams, 
ownership, dependencies, onboarding paths, components, and relationships between 
architectural entities.
- **`document-service`** provides source-backed evidence: indexed code, architecture documents,- READMEs, design notes, runbooks, configuration documentation, and other technical material.


### 1. End-to-End Architecture Walkthrough

**Example query:**

> “Explain what happens after I submit a mission to Agentic Service. Walk me through 
> mission persistence, Temporal initialization, Embabel reasoning, LLM calls, Internal Service and Document Service execution, grounding, and the final response.”

SparrowX reconstructs the runtime flow from its indexed implementation and explains the major execution stages in order:

1. **SubmitMission** - the client submits the natural-language request, constraints, and execution context to `agenticsvc`.
2. **Validate + persist mission** - Agentic Service validates the request and stores the initial mission state.
3. **Start Temporal workflow** - Temporal establishes the durable outer execution boundary that manages the mission lifecycle.
4. **Initialize Embabel mission state** - Embabel agent receives the mission context, available actions, policies, budgets, and working state.
5. **Understand intent** - Agent determines what the mission means and invokes an LLM where semantic interpretation is required.
6. **Plan the mission** - Agent determines goals, candidate actions, dependencies, and what should happen next.
7. **Choose + execute the next action** - Embabel selects an LLM call, `intsvc`, `docsvc`, human approval, or another capability; Temporal durably executes the corresponding outer workflow step.
8. **Observe + replan** - Embabel consumes the returned observation, updates its agent state, and determines the next action.
9. **Synthesize + verify** - once sufficient evidence exists, Embabel drives answer synthesis while SparrowX performs grounding, citation, policy, and output validation.
10. **Persist + return result** - Agentic Service stores the completed mission result and exposes the grounded response to the client.

At this level, the guide teaches the distinction between the two orchestration layers:

> **Embabel decides what the agent should do next. Temporal makes that execution durable.**

---

### 2. Component and Responsibility Exploration

The guide can also answer architectural questions that do not require a complete end-to-end 
walkthrough.

**Example query:**

> “Why does the enterprise system use both Embabel and Temporal? Which responsibilities belong to each, 
> and what happens if Agentic Service crashes halfway through a multi-hop mission?”

For this query:

- **`internal-service`** resolves the relevant architectural components and their relationships.
- **`document-service`** retrieves implementation evidence covering the Embabel
agent, Temporal workflow, Activities, checkpoints, mission persistence, and recovery behavior.
- **`agentic-service`** compares the responsibilities of those components and constructs the 
- explanation.

The resulting guide can explain relationships such as:

```text
Temporal
   │
   │ durable mission lifecycle
   ▼
Embabel
   │
   │ decides next action
   ▼
LLM / intsvc / docsvc
   │
   │ observation
   ▼
Embabel
   │
   │ re-evaluates mission
   ▼
next action
```

This allows engineers to explore queries such as:


> What does the Embabel blackboard contain during a mission, and how is context 
> from internal-service, evidence from document-service, LLM outputs, and intermediate 
> observations represented there?

> What information belongs in Temporal history versus PostgreSQL mission projections and 
> checkpoints?

> Why are LLM calls executed inside Temporal Activities while Embabel remains responsible 
> for deciding when they are needed?

> How does Embabel’s goal-and-action planning resemble GOAP when a mission must combine 
> internal-service relationships with document-service evidence before producing a grounded
> answer?

> What gets reconstructed after an agentic-service pod failure, and which parts come 
> from Temporal, PostgreSQL, and Embabel state?



The guide therefore instructs not only **what components exist**, but also 
**why architectural boundaries exist**.

---

### 3. Code-to-Runtime Investigation

The deepest guide mode connects a runtime concept directly back to its implementation.

**Example query:**

> “Show me how `SubmitMission` eventually reaches Embabel planning, identify the classes 
> responsible for each transition, and explain where durable execution begins.”

Instead of returning only a conceptual architecture description, SparrowX can trace the 
concept through indexed source code and supporting documentation.

A response may correlate:

```text
Public gRPC API
      ↓
SubmitMission
      ↓
Command / application layer
      ↓
Mission persistence
      ↓
Temporal client
      ↓
Mission Workflow
      ↓
Mission Activity
      ↓
EmbabelMissionRunner
      ↓
MissionAgent
      ↓
intent / planning / actions
```

For each transition, the system can retrieve the corresponding code or architecture evidence 
and explain:

- which class owns the transition
- what state enters and leaves it
- whether the operation is deterministic or side-effecting
- whether it executes inside or outside a Temporal Activity
- what is persisted
- what happens on retry or failure
- how the result becomes available to the next Embabel reasoning cycle

Eventually the system becomes an interactive **agentic-system tutorial backed by the 
implementation itself**.

An engineer can progressively drill down:

> “Explain `MissionActivitiesImpl`.”

> “Now show which Embabel component it invokes.”

> “Where is the observation persisted?”

> “Which interfaces isolate the agent framework from Temporal?”

Because of this, the guide can move continuously between
**concept → runtime flow → implementation → failure behavior → design rationale**.

---

## Enterprise Simulation & LLMOps - Langfuse Integration

Out of the box, SparrowX includes a **production-grade seed-data pipeline** that simulates 
an operating engineering organization.

The environment provisions an **Agentic Service Team** alongside additional engineering 
teams and tenants, generates synthetic company data and workloads, and provides realistic 
activity for exercising SparrowX's retrieval and orchestration paths.

This simulation makes the self-referential guide observable at runtime. An engineer can 
first ask it how a mission is supposed to work and then inspect the corresponding 
execution through the integrated **Langfuse** environment.

The integration demonstrates several LLMOps concerns relevant to production agentic systems:

- **Visualize Nested Agent Traces:** Inspect hierarchical execution traces showing how 
a mission branches into LLM generations, retrieval operations, tool calls, and downstream 
service interactions.

- **Monitor Multi-Tenant Cost & Latency:** Analyze token usage, model cost, execution 
latency, and other operational characteristics across engineering teams, tenants, 
service domains, and model configurations.

- **Manage Prompts & Iteration Loops:** Centralize prompt versions and experiment with 
prompt changes independently of the core orchestration implementation, allowing different
agent pipelines to consume controlled prompt revisions.

- **Track Operational Quality with Evals:** Attach automated evaluation signals to agent 
executions to measure properties such as grounding quality, answer correctness, 
hallucination rates, and execution performance over time.

Together, the Guide and LLMOps environment create a useful feedback loop:

```text
Ask SparrowX how the system works
              ↓
SparrowX explains its indexed implementation
              ↓
Execute a real mission
              ↓
Observe its agent / tool / LLM traces in Langfuse
              ↓
Compare architecture with runtime behavior
              ↓
Investigate individual components through SparrowX
```

## Roadmap

| Feature          | Dormant | In Progress | Completed |
|------------------|---------|-------------|-----------|
| API Gateway      |   ✅     |             |           |
| Agentic Service  |        |      ✅       |           |
| Building Blocks  |         |            |     ✅       |
| Document Service |        |             |       ✅    |
| Internal Service |        |             |      ✅     |


