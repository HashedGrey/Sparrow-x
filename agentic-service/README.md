# Agentic RAG Service

This service implements **Agentic, multi-hop Retrieval-Augmented Generation (RAG)** for **SparrowX**.

At present, the repository contains **structure without implementation**.  
This is intentional since the coding approach is top-down

The purpose of this repo is to make the **reasoning architecture explicit before code exists**.

If you want to understand how complex analytical missions are decomposed, routed, verified, and synthesized, start with:

➡ **`agentic-tree.txt`**

That file is an ASCII execution map designed to be fed directly into an LLM.

---

## What This Service Is (and Is Not)

### It **is**
- A **mission-oriented agentic system**
- Designed for **high-stakes, multi-document analysis**
- Deterministic where it must be, probabilistic where it is safe
- Evidence-first, citation-required, provenance-aware

### It **is not**
- A chat wrapper
- A “single-hop RAG search”
- A place where LLMs invent domain truth
- A memory store for user facts or opinions

---

## Example Mission This System Is Built To Execute

> **Given the following PDFs:**
> - GPT-5 System Card
> - Stanford Research PDF — *Routed LLM Systems Under Stress*
> - Preparedness Framework v2
>
> **Extract and dedupe all testable claims**  
> `{metric, benchmark, conditions, value}`
>
> For each claim cluster:
> - Generate targeted retrieval queries
> - Retrieve relevant tweet discussions (full threads + metadata)
> - Classify evidence as `confirm / mismatch / contradict / unknown`
>
> Rank:
> - Claim clusters by `impact × confidence × contradiction-density`
> - Authors by `credibility × insight-velocity × claim-relevance`
>
> Provide citations back to the exact PDF sections.

This README explains **how that mission executes**, step by step.

---

## Start Here: `agentic-tree.txt`

`agentic-tree.txt` is the **authoritative mental model** of the system.

It shows:
- Agent boundaries
- Deterministic vs LLM-driven steps
- Evidence-only memory rules
- Domain-read vs evidence-retrieval separation
- Validation, policy, and budget gates
- Where hallucination is impossible by construction

### How to Use It

Paste the file into an LLM and ask:

```text
Explain how this agentic system executes a multi-hop RAG mission,
including planning, evidence verification, and synthesis.
````
## Core Execution Model

### Every mission follows the same non-negotiable lifecycle:

Intent  
→ Typed Canonical Command  
→ DICE Context Bind  
→ PlanGraph (DAG)  
→ Domain Reads (LIVE sp state)  
→ Policy & Budget Gates  
→ Evidence Retrieval (docs, tweets, PDFs)  
→ Claim Extraction & Cross-checking  
→ Validation (schemas + provenance)  
→ Synthesis (citations required)  

### Execution Flow Across Files

Below is the concrete walkthrough of the repository that implements the lifecycle above.
Think of it as: request enters, plan is built, gates are enforced, evidence is gathered, answer is synthesized.

- grpc/server/AgenticServiceGrpcImpl.java
Entry point for a mission request. Starts tracing/budgets, streams progress events, hands off to the engine.

- engine/AgentEngine.java
Mission lifecycle coordinator:   
Intent → DICE bind → PlanGraph → Execute → Synthesize.

- planning/IntentResolver.java → planning/Planner.java → planning/PlanGraph.java
Turns user mission into a typed intent, then produces a deterministic Plan DAG (optionally via GOAP plugin).

- dice/ContextAssembler.java (+ ConstraintBinder, GuardrailBinder)
Builds versioned mission context from LIVE sp domain reads + evidence summaries; injects budgets and invariants.

- actions/* executed by engine/PlanExecutor.java
Executes nodes in-order (or explicit parallel), enforcing retries/budgets/policy:

- Domain reads: actions/domain/* via gRPC adapters

- Evidence: actions/evidence/* via ES/Milvus/local index

- Enforcement: actions/enforcement/* gates

- engine/EvidenceCollector.java + governance/ProvenanceVerifier.java
Ensures every EvidenceItem has a complete provenance chain; rejects “floating” claims.

- engine/ResultSynthesizer.java
Produces the final response: combines domain facts + cited evidence, emits confidence + rationale.