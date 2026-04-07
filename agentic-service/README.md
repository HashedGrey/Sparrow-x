# Agentic RAG Service

The Agentic RAG Service is SparrowX’s goal-driven retrieval and reasoning layer for complex analytical queries over social and document evidence.

It is built on Embabel and designed to execute structured RAG missions that require:

- multi-step retrieval
- evidence comparison
- controlled tool use
- domain-aware hydration
- strict provenance in final outputs

This service is still under active development, but the repository already defines the architecture, boundaries, and execution model that the implementation will follow.

## Core Idea

This is not a generic “chat over data” wrapper. The service treats retrieval and reasoning as a mission pipeline made of explicit stages:

- interpret the user’s request
- retrieve the right evidence
- enrich only where necessary
- compare and analyze
- synthesize a grounded result

The LLM is used as a reasoning component inside a governed pipeline, not as an unconstrained source of truth.  
Determinism go brrr...
## Core Philosophy

### Mission-Oriented

The service is optimized for analytical missions, not simple one-shot chat. 
A query may require multiple retrieval hops, ranking passes, comparison steps, and evidence enrichment before a result is produced.

### Evidence-First

The system is designed so that outputs are grounded in retrieved evidence, not invented by the model.  
The LLM helps interpret, route, compare, and summarize, but the evidence comes from SparrowX stores and services.

### Deterministic Governance

Budgets, tool permissions, validation rules, and provenance checks are enforced by the platform.  
The LLM operates within those boundaries.

### Clear Domain Boundaries

The agentic layer does not own social-domain truth.  
It retrieves and reasons across evidence, while canonical tweet and profile truth remain in SparrowX domain services.

## What the Service Actually Does

For social RAG queries, the runtime flow is:

1. **Parse the user query**  
   The service sends the query to an LLM to determine intent, task shape, retrieval targets, and comparison objectives.

2. **Build retrieval vectors**  
   BGE Large is used to embed the query for semantic retrieval.

3. **Retrieve candidate tweet IDs from Qdrant**  
   Qdrant already contains tweet vectors produced upstream when Tweet Service writes occur.  
   The agentic service does not embed and store tweets at query time.

4. **Fetch searchable tweet/thread evidence from Search Service**  
   The returned IDs are used to retrieve tweet documents, thread context, and searchable metadata from Search Service / Elasticsearch.

5. **Hydrate where required**  
   When richer domain truth is needed, the service hydrates selectively from:
  - **Tweet Service** for canonical tweet/thread details
  - **Profile Service** for author/profile context

   Hydration is not always required; it is done only when the mission needs it.

6. **Compare, cluster, and analyze**  
   Embabel engine then groups the retrieved evidence into coherent narratives, clusters or analytical units.  
   The system can compare signals, contradictions, momentum, stance, or relevance depending on the mission.

7. **Synthesize the result**  
   The final answer is produced with citations, caveats, and structured reasoning grounded in retrieved evidence.

## Important Retrieval Boundary

Tweets are already stored in Qdrant outside the agent workflow, triggered by Tweet Service writes.  
That means the agentic service should be understood as a query-time orchestration and reasoning layer, not a tweet-ingestion pipeline.

In short:

- **Qdrant** = semantic lookup surface for tweet IDs
- **Search Service / Elasticsearch** = searchable retrieval surface for tweet/thread documents
- **Tweet Service / Profile Service** = hydration and canonical domain truth
- **Agentic RAG Service** = orchestration, comparison, reasoning, and synthesis

## Technical Stack

| Layer | Technology                                                  |
|---|-------------------------------------------------------------|
| Framework | Spring Boot + Embabel Agent Runtime                         |
| Communication | gRPC (Mission API) + HTTP (Admin/Eval)                      |
| Retrieval | Qdrant (vector retrieval), Elasticsearch via Search Service |
| Hydration | Tweet Service, Profile Service                              |
| Evidence Storage | MinIO for document artifacts where applicable               |
| Observability | Loki + Tempo + Alloy + Prometheus == Grafana                |


## How to Read the Skeleton

For now the repository contains **structure without implementation**, which is intentional since the coding approach is top-down.  
So if you want to understand how the system is meant to work, this skeleton ↓ can be read as an architectural execution map.

### Start Here: `agentic-tree.txt`
The file is an ASCII File Directory tree that functions as an execution map designed to be fed directly into an LLM to give a high level view of the engine.

This is the **authoritative mental model** of the system which shows:
- Agent boundaries
- Deterministic vs LLM-driven steps
- Evidence-only memory rules
- Domain-read vs evidence-retrieval separation
- Validation, policy and budget gates
- Where hallucination is impossible by construction

### How to Use It

Paste the file into an LLM and ask:
```text
Explain how this agentic system executes a complex multi-hop RAG mission,
including planning, evidence verification, and synthesis.
````



It shows:

- service boundaries
- where retrieval happens
- where hydration happens
- where Embabel agents operate
- where deterministic enforcement lives
- how the final result is assembled

---

## Execution Model

The current repository is intentionally top-down.  
It defines the execution shape, interfaces, policies, and system boundaries before all runtime details are fully implemented.

This makes the intended reasoning model explicit:

- where LLM-driven steps exist
- where deterministic validation exists
- where domain reads happen
- where evidence retrieval happens
- where governance gates sit
- where provenance is enforced

## Canonical Mission Flow

Every mission follows a governed execution lifecycle managed by the mission coordinator.

### 1. Query Interpretation
The user submits a mission or RAG query.  
The service determines:

- what kind of task this is
- what evidence sources are needed
- whether retrieval is semantic, lexical, or hybrid
- whether enrichment or hydration will be necessary
- what constraints or budgets apply

### 2. Retrieval

The service performs retrieval in layers. For social evidence:

- generate retrieval embedding with BGE Large
- search Qdrant for nearest tweet vectors
- collect candidate tweet IDs
- fetch tweet/thread evidence from Search Service

For document evidence, the service may also retrieve indexed chunks and metadata from evidence stores if the mission includes PDFs or other artifacts.

### 3. Hydration

The service enriches retrieved results only when needed.

Examples:

- canonical tweet fields from Tweet Service
- author or segment context from Profile Service
- thread expansion where clustering or comparison requires it

This keeps retrieval fast while preserving access to domain truth when deeper analysis is needed.

### 4. Analysis

The service organizes and evaluates evidence. Depending on the mission, this can include:

- clustering related tweets or threads into narratives
- comparing signals across sources
- identifying contradictions or alignment
- separating weak chatter from durable momentum
- ranking narratives, authors, or intervention opportunities

### 5. Synthesis

The service produces a grounded output that may include:

- ranked narratives
- comparisons
- intervention recommendations
- citations
- caveats
- confidence notes

## Repository Map

### 1. Mission Entry Point

`api/grpc/AgenticRagGrpcImpl.java`

Primary API for:

- submitting missions
- streaming progress
- returning final outputs

`mission/MissionFlowCoordinator.java`

Coordinates mission stages, tool calls, retrieval passes, and synthesis.

### 2. Mission Stages

`mission/MissionStage.java`

Defines the canonical execution path.

At a high level this looks like:

- interpret
- retrieve
- fetch
- hydrate
- analyze
- rank
- synthesize

### 3. DICE / Guardrails Layer

`dice/`

This layer ensures the model interacts with controlled, typed, projected context rather than arbitrary raw payloads.

Here, **DICE** means **Domain Injected Context Engineering**.

Key responsibilities include:

- context assembly
- metadata normalization
- structured binding
- invariant enforcement
- output repair and rejection where needed

### 4. Agents

`agents/`

Embabel agents define the high-level reasoning roles, such as:

- query interpretation
- social evidence orchestration
- stance / ranking analysis
- final synthesis

### 5. Actions

`actions/`

Actions are smaller, reusable execution units for tasks such as:

- targeted query building
- tweet retrieval
- clustering
- stance labeling
- ranking
- provenance verification
- redaction

### 6. Governance and Policy

`policy/` and `governance/`

This layer enforces:

- budgets
- safety constraints
- tool authorization
- compliance boundaries
- provenance guarantees

These controls belong to the platform, not to the model.

## Provenance Guarantees

A key design principle is that the service should not emit “floating conclusions.”

Final outputs should be traceable to retrieved evidence.  
That means:

- evidence must come from approved retrieval surfaces
- enrichments must come from valid domain services
- synthesis should preserve links back to underlying support
- invalid or ungrounded outputs can be rejected by governance checks
