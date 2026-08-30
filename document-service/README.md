# SparrowX Document Service

## Overview

The Document Service provides document ingestion, storage, retrieval, evidence construction, and citation verification for SparrowX.

It owns the unstructured knowledge pipeline used by Agentic Service to retrieve grounded evidence from company documents.

The service supports document upload, asynchronous ingestion, hybrid search, evidence graph construction, evidence verification, and source-level citations.

## Implementation Progress

🟩 **Document upload and persistence** ██████████ **100%**

🟩 **Document extraction and chunking** █████████░ **98%**

🟩 **Embedding and indexing pipeline** █████████░ **98%**

🟩 **Hybrid keyword/vector retrieval** █████████░ **98%**

🟩 **Access and permission filtering** █████████░ **98%**

🟩 **Evidence graph construction** █████████░ **85%**

🟩 **Citation and evidence verification** █████████░ **98%**

🟩 **Ingestion recovery and workers** █████████░ **98%**

🟩 **gRPC API and resilience policies** █████████░ **98%**

🟩 **Observability** █████████░ **98%**

```text
Overall                         🟩 █████████░ 98%
```

## Document Ingestion

Uploaded documents are persisted and processed through the ingestion pipeline.

Supported processing includes:

* document validation
* object storage
* text extraction
* token-aware chunking
* embedding generation
* keyword indexing
* vector indexing
* ingestion status tracking
* failure recovery

Supported extractors include:

* PDF
* DOCX
* XLSX
* Apache Tika-compatible formats
* OCR-backed documents

The ingestion pipeline separates processing into explicit stages so failures can be recovered without rebuilding unrelated document state.

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

Indexing supports dual publication to keyword and vector search infrastructure.

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
                 Retrieval Evidence
```

## Evidence Graph

Retrieved document evidence can be transformed into a structured evidence graph.

The evidence layer contains:

* evidence normalization
* schema validation
* relation linking
* graph construction
* policy enforcement
* response compaction
* projection adapters

Evidence graphs provide structured provenance between claims, source spans, and supporting document content.

## Evidence Verification

The service verifies evidence before it is consumed as grounded context.

Verification includes:

* source-span validation
* citation verification
* evidence graph validation
* verification status tracking

This allows Agentic Service to synthesize answers from registered, traceable evidence rather than raw retrieval results.

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

Strong value objects are used for document IDs, tenant IDs, user IDs, hashes, titles, MIME types, retrieval modes, and verification state.

## Security and Isolation

Retrieval applies contextual access and permission checks before evidence is returned.

The service maintains tenant-aware document boundaries and propagates caller identity through the request pipeline.

gRPC policy enforcement provides an additional transport-level governance boundary.

## Observability

Dedicated lifecycle logging exists for:

* document lifecycle
* ingestion
* retrieval
* evidence construction
* evidence verification
* citation verification

This makes ingestion and retrieval failures traceable independently from Agentic Service orchestration.

## Technology

The service uses:

* Java 24+
* Spring Boot
* gRPC
* PostgreSQL
* MinIO
* Elasticsearch
* Qdrant
* Gemini embeddings
* Embabel-compatible evidence projection
* OpenTelemetry-compatible observability

## Current State

The heavy document intelligence core is implemented.

```text
Ingestion Pipeline              🟩 █████████░ 98%
Storage / Persistence           🟩 █████████░ 98%
Hybrid Retrieval                🟩 █████████░ 98%
Evidence Construction           🟩 █████████░ 98%
Evidence Verification           🟩 █████████░ 98%
Service API                     🟩 █████████░ 98%

Overall                         🟩 █████████░ 98%
```

Remaining work should primarily focus on production hardening, integration validation, failure-path testing, tuning retrieval quality, and removing obsolete or experimental implementations where appropriate.
