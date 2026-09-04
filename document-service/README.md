# SparrowX Document Service

## Overview

The Document Service provides document ingestion, storage, retrieval, evidence construction, grounding verification, and citation verification for SparrowX.

It owns the unstructured knowledge pipeline used by Agentic Service to ingest company documents, retrieve grounded source spans, construct provenance-aware evidence, and verify citations.

Document Service owns document grounding and retrieval mechanics. Agentic Service owns mission-level semantic reasoning over that evidence, including interpretation, support/contradiction judgments, and final synthesis.

## Implementation Progress

🟩 **Document upload and persistence** ██████████ **100%**

🟩 **Document ingestion and chunking** █████████░ **98%**

🟩 **Embedding and indexing pipeline** █████████░ **98%**

🟩 **Hybrid keyword/vector retrieval** █████████░ **98%**

🟩 **Access and permission filtering** █████████░ **98%**

🟩 **Evidence graph construction** █████████░ **98%**

🟩 **Citation and grounding verification** █████████░ **98%**

🟩 **Ingestion recovery and workers** █████████░ **98%**

🟩 **gRPC API and resilience policies** █████████░ **98%**

🟩 **Observability** █████████░ **98%**

```text
Overall                         🟩 █████████░ 98%
```

## Document Ingestion

Uploaded documents are persisted and processed through the ingestion pipeline.

Document parsing and chunking are handled through Embabel Agent RAG.

The ingestion layer provides:

* document validation
* object storage
* Embabel hierarchical document parsing
* Apache Tika-backed content extraction
* page-aware PDF ingestion
* overlapping content chunking
* embedding generation
* keyword indexing
* vector indexing
* ingestion status tracking
* failure recovery

The Embabel content chunker currently uses:

```text
Maximum chunk size    1500
Chunk overlap          200
Embedding batch size   100
```

The ingestion pipeline retains explicit lifecycle stages for status tracking, persistence, metrics, indexing, and recovery.

Document parsing and chunking are performed together through the Embabel RAG ingestion adapter while the existing pipeline lifecycle remains visible to callers and observability infrastructure.

```text
Object Storage
     ↓
Embabel RAG Ingestion
     ↓
Hierarchical Parsing
     ↓
Content Chunking
     ↓
Persist Chunks
     ↓
Keyword + Vector Indexing
```

## Storage and Indexing

The service uses multiple storage systems for different responsibilities:

```text
PostgreSQL
  → document metadata
  → chunks
  → ingestion jobs

MinIO
  → original document objects

Elasticsearch
  → keyword retrieval

Qdrant
  → vector retrieval
```

Indexing supports publication to both keyword and vector search infrastructure.

Qdrant indexing uses request-local payload state so concurrent indexing operations remain isolated.

## Retrieval

Document retrieval combines keyword and vector search.

The retrieval pipeline provides:

* hybrid retrieval
* score merging
* reranking
* deduplication
* metadata lookup
* tenant/context access filtering
* permission filtering
* source-span construction

```text
Query
  ↓
Keyword Search ──┐
                 ├─→ Score Merge
Vector Search ───┘
                       ↓
                   Deduplicate
                       ↓
                     Rerank
                       ↓
                Permission Filter
                       ↓
                  Source Spans
                       ↓
              Retrieval Evidence
```

Retrieval remains responsible for locating relevant document content and returning grounded source material. It does not perform mission-level reasoning over that content.

## Evidence Graph

Retrieved document evidence can be organized into a grounded evidence graph.

The evidence layer contains:

* grounded source-span normalization
* schema validation
* grounded relation linking
* graph construction
* policy enforcement
* response compaction

Document Service keeps evidence construction grounded in retrieved document content.

Evidence nodes are derived from registered source spans and preserve provenance back to originating chunks and documents.

Document Service does not infer semantic evidence types merely to satisfy requested node classifications. When semantic types cannot be established directly from grounded document evidence, nodes may remain semantically unspecified.

Likewise, mission-level judgments such as whether evidence supports or contradicts a user's proposition belong to Agentic Service.

```text
Source Spans
     ↓
Grounded Normalization
     ↓
Evidence Nodes
     ↓
Grounded Relation Linking
     ↓
Schema Validation
     ↓
Policy Enforcement
     ↓
Document Evidence Graph
```

Evidence graphs provide structured provenance between:

* evidence nodes
* evidence relations
* source spans
* document chunks
* originating documents

## Evidence Verification

The service verifies that evidence is grounded in registered document sources before it is consumed by Agentic Service.

Verification includes:

* source-span validation
* citation verification
* evidence graph structural validation
* grounding/support verification
* verification status tracking

Verification establishes that returned evidence is traceable to document content.

It does not perform mission-level entailment reasoning or determine whether a user's proposition is ultimately supported or contradicted.

That responsibility belongs to Agentic Service.

```text
Document Evidence
      ↓
Source Validation
      ↓
Citation Validation
      ↓
Graph Validation
      ↓
Grounding Verification
      ↓
Verified Evidence
      ↓
Agentic Service Reasoning
```

## Service Boundary

The Document Service intentionally stops at grounded evidence.

```text
Document Service

ingest
  ↓
parse
  ↓
chunk
  ↓
index
  ↓
retrieve
  ↓
ground
  ↓
construct provenance
  ↓
verify sources and citations
```

Agentic Service consumes that evidence and performs higher-level reasoning:

```text
Agentic Service

interpret evidence
  ↓
classify semantics
  ↓
compare evidence
  ↓
judge support / contradiction
  ↓
reason across sources
  ↓
synthesize grounded answer
```

This boundary keeps document retrieval deterministic and provenance-focused while allowing Agentic Service to perform mission-specific reasoning.

## Service API

Primary features include:

```text
UploadDocument
GetDocument
GetIngestionJob
SearchDocumentSpans
BuildDocumentEvidence
VerifyEvidenceGraph
```

Commands and queries are implemented as feature-oriented handlers and exposed through the gRPC service.

## Domain Model

The document domain includes:

* `Document`
* `IngestionJob`
* `RetrievalEvidence`
* `SourceSpan`
* `DocumentEvidenceGraph`
* `DocumentEvidenceNode`
* `DocumentEvidenceEdge`

Strong value objects are used for:

* document IDs
* tenant IDs
* user IDs
* project IDs
* team IDs
* content hashes
* titles
* MIME types
* retrieval modes
* verification state

## Security and Isolation

Retrieval applies contextual access and permission checks before evidence is returned.

The service maintains tenant-aware document boundaries and propagates caller identity through the request pipeline.

Security boundaries include:

* tenant-scoped document access
* contextual retrieval filtering
* permission filtering
* caller identity propagation
* gRPC transport-level policy enforcement

## Observability

Dedicated lifecycle logging exists for:

* document lifecycle
* ingestion
* retrieval
* evidence construction
* evidence verification
* citation verification

This makes ingestion, indexing, retrieval, and grounding failures independently traceable from Agentic Service orchestration.

## Technology

The service uses:

* Java 25
* Spring Boot
* Spring AI
* gRPC
* PostgreSQL
* MinIO
* Elasticsearch
* Qdrant
* Gemini embeddings
* Embabel Agent RAG
* Apache Tika
* OpenTelemetry-compatible observability

Embabel Agent RAG is used for hierarchical document ingestion and chunking.

## Current State

The heavy document intelligence and retrieval core is implemented.

```text
Ingestion Pipeline              🟩 █████████░ 98%
Storage / Persistence           🟩 █████████░ 98%
Hybrid Retrieval                🟩 █████████░ 98%
Evidence Construction           🟩 █████████░ 98%
Evidence Verification           🟩 █████████░ 98%
Service API                     🟩 █████████░ 98%

Overall                         🟩 █████████░ 98%
```

Remaining work should primarily focus on:

* production hardening
* integration validation
* failure-path testing
* retrieval quality tuning
* concurrency and load testing
* ingestion format coverage
* observability validation
* removing obsolete or experimental dependencies
* validating Agentic Service integration against the simplified evidence boundary
