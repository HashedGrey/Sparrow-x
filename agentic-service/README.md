# Agentic RAG Service

This service implements **high-fidelity, multi-hop Retrieval-Augmented Generation (RAG)** for **SparrowX**.  
It is built on the **Embabel agentic framework** to execute complex analytical missions that require:

- Cross-document reasoning
- Social evidence synthesis
- Strict provenance guarantees
---
## Core Philosophy
Unlike standard *"chat-over-PDF"* wrappers, this system treats RAG as a **structured pipeline of discrete, verifiable actions**.

### Mission-Oriented
Optimized for **missions** (long-running research or execution tasks) rather than simple Q&A.

### DICE Architecture
Uses a **Domain-Integrated Context Engineering** model to ensure the LLM only interacts with **projected, normalized metadata**.

### Evidence-First
Hallucinations are mitigated **by construction**. Agents are restricted to tools that operate on **verified evidence stores**.

### Deterministic Governance
Budgets, safety policies, and provenance checks are **enforced by the platform**, not the LLM.

---

## Technical Stack

| Layer | Technology                                                                                    |
|---|-----------------------------------------------------------------------------------------------|
| Framework | Spring Boot + Embabel Agent Runtime                                                           |
| Communication | gRPC (Mission API) + HTTP (Admin/Eval)                                                        |
| Storage | Elasticsearch (Lexical), CassandraDb(Hydration), Milvus (Vector), Neo4j (Graph), MinIO (Blob) |
| Observability | OpenTelemetry (OTEL) + Prometheus                                                             |

---

# The Execution Pipeline
At present, the repository contains **structure without implementation**, which is intentional since the coding approach is top-down.
The purpose of this is to make the **reasoning architecture explicit before code exists**.

If you want to understand how complex analytical missions are decomposed, routed, verified and synthesized:

## Start Here: `agentic-tree.txt`
The file is an ASCII File Directory tree that functions as an execution map designed to be fed directly into an LLM.

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
---

---
Every mission follows a canonical lifecycle managed by the **MissionFlowCoordinator**.

### 1. Ingest & Project
Raw **PDFs / Tweets** are parsed and chunked.

The **DICE projection layer** normalizes metadata such as:

- { titles ,authors ,dates ,sources }

This ensures the **LLM never sees raw document headers or messy metadata**.

---

### 2. Claim Extraction

The **ClaimMiningAgent** extracts structured, testable claims:
- {
  metric,
  benchmark,
  conditions,
  value
  }

This converts narrative text into **machine-analyzable claims**.

---

### 3. Deduplication

Claims across multiple documents are merged into **ClaimClusters**.

Each cluster:

- groups semantically equivalent claims
- retains **full provenance** to original sources.

---

### 4. Social Discovery

The **SocialEvidenceAgent** generates targeted queries to retrieve real-world discussion signals:

- Tweets
- Threads
- Public commentary

This adds **external social validation or contradiction** to academic or formal claims.

---

### 5. Analysis & Stance

The **StanceAndRankingAgent** labels evidence as:

- **confirm**
- **mismatch**
- **contradict**

Claim clusters are ranked based on **contradiction density**, surfacing the most controversial or debated claims first.

---

### 6. Synthesis

The **ReportSynthesisAgent** generates the final analytical report.

Outputs include:

- mandatory citations
- caveats
- claim-level provenance
- structured reasoning summaries

---

# Repository Map

## 1. Mission Entry Point

**api/grpc/AgenticRagGrpcImpl.java**

Primary interface for:

- submitting missions
- streaming mission progress events

**mission/MissionFlowCoordinator.java**

Orchestrates transitions between **MissionStages** in the pipeline.

---

## 2. The DICE Layer (Guardrails)

**dice/projection/EvidenceProjection.java**

Canonicalizes metadata so the **LLM never sees raw, messy document headers**.

**dice/GuardrailBinder.java**

Ensures every generated output maps back to a **valid SourceRef**.

---

## 3. Agent & Action Definitions

**agents/**  
Contains Embabel `@Agent` components such as:

- `ClaimMiningAgent`
- `SocialEvidenceAgent`
- `StanceAndRankingAgent`
- `ReportSynthesisAgent`

**actions/**  
Contains atomic, reusable work units executed by agents:

- `ExtractClaimsAction`
- `LabelStanceAction`
- other deterministic actions

---

## 4. Governance & Policy

**policy/**

Defines deterministic platform rules:

- `BudgetPolicy`
- `SafetyPolicy`
- `ToolAuthz`

These policies enforce **execution constraints independent of the LLM**.

---

**governance/ProvenanceVerifier.java**

Rejects any **floating citations** that cannot be traced to a specific **PDF chunk or document fragment**.

This guarantees **verifiable provenance for every claim in the final report**.