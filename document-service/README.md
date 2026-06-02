# SparrowX Document Service

The **SparrowX Document Service** is the document intelligence layer for SparrowX. It handles document upload, metadata persistence, object storage, ingestion, text extraction, chunking, keyword indexing, vector indexing, hybrid retrieval, citation verification, and DICE evidence graph generation.

This service is designed to support enterprise agentic workflows where the Agentic Service needs reliable document evidence, citations, source spans, and structured evidence graphs instead of raw document blobs.

---

## Service Status

✅ **Core document service features are complete.**

The service now supports the full document lifecycle:

1. Upload document
2. Store binary object
3. Persist document metadata
4. Create ingestion job
5. Extract document text
6. Chunk extracted text
7. Index chunks into Elasticsearch
8. Embed chunks with Gemini
9. Index vectors into Qdrant
10. Run keyword, vector, and hybrid retrieval
11. Build source spans and retrieval evidence
12. Verify citations and evidence graphs
13. Build DICE evidence graphs
14. Expose document operations through gRPC
15. Emit lifecycle logs, traces, and metrics

---

## Architecture

The service is organized around clear backend boundaries:

```text
document-service
├── config
├── data
│   ├── postgres
│   ├── minio
│   ├── elasticsearch
│   └── qdrant
├── domain
├── features
├── grpc
├── ingestion
├── retrieval
├── verification
├── dice
├── mappers
├── observability
└── exceptions
```

---

## Core Capabilities

### ✅ Document Upload

The upload flow accepts document metadata and binary content, validates the request, stores the document object, persists metadata, and creates an ingestion job.

Implemented through:

```text
features/uploaddocument
data/minio
data/postgres
domain/events/DocumentUploadedDomainEvent.java
```

---

### ✅ Object Storage

Uploaded files are stored in MinIO using a tenant-aware object key structure.

Implemented through:

```text
data/minio/DocumentStorage.java
data/minio/MinioDocumentStorage.java
data/minio/StoredDocumentObject.java
data/minio/MinioObjectMetadata.java
```

---

### ✅ Postgres Metadata Persistence

Document metadata, chunks, and ingestion jobs are persisted in Postgres.

Implemented through:

```text
data/postgres/entities/DocumentEntity.java
data/postgres/entities/DocumentChunkEntity.java
data/postgres/entities/IngestionJobEntity.java
data/postgres/repositories/DocumentRepository.java
data/postgres/repositories/DocumentChunkRepository.java
data/postgres/repositories/IngestionJobRepository.java
```

---

### ✅ Async Ingestion

Documents are processed through an ingestion worker and queue. Uploading a document creates an ingestion job, and the worker processes it asynchronously.

Implemented through:

```text
ingestion/IngestionWorker.java
ingestion/IngestionJobRunner.java
ingestion/IngestionJobRecoveryService.java
ingestion/queue/IngestionQueue.java
ingestion/queue/InMemoryInestionQueue.java
features/processingestionjob
```

---

### ✅ Text Extraction

The service supports multiple document extraction paths, including PDF, DOCX, XLSX, OCR, and Tika-based extraction.

Implemented through:

```text
ingestion/extraction/DocumentTextExtractor.java
ingestion/extraction/TikaDocumentTextExtractor.java
ingestion/extraction/PdfDocumentTextExtractor.java
ingestion/extraction/DocxDocumentTextExtractor.java
ingestion/extraction/XlsxDocumentTextExtractor.java
ingestion/extraction/OcrDocumentTextExtractor.java
```

---

### ✅ Document Chunking

Extracted text is split into searchable chunks with metadata and boundaries.

Implemented through:

```text
ingestion/chunking/DocumentChunker.java
ingestion/chunking/TokenDocumentChunker.java
ingestion/chunking/DocumentChunkDraft.java
ingestion/chunking/ChunkBoundaryDetector.java
ingestion/chunking/ChunkMetadataBuilder.java
```

---

### ✅ Elasticsearch Keyword Indexing

Document chunks are indexed into Elasticsearch for keyword retrieval.

Implemented through:

```text
data/elasticsearch/ElasticsearchChunkIndexer.java
data/elasticsearch/ElasticsearchDocumentKeywordRepository.java
ingestion/indexing/ElasticsearchDocumentChunkIndexer.java
retrieval/KeywordDocumentSearcher.java
```

---

### ✅ Gemini Embeddings

Document chunks are embedded using Gemini embeddings for semantic retrieval.

Implemented through:

```text
ingestion/indexing/EmbeddingService.java
ingestion/indexing/GeminiEmbeddingService.java
ingestion/indexing/GeminiEmbeddingRequest.java
ingestion/indexing/GeminiEmbeddingResponse.java
```

---

### ✅ Qdrant Vector Indexing

Embedded chunks are stored in Qdrant for vector search.

Implemented through:

```text
data/qdrant/QdrantCollectionInitializer.java
data/qdrant/QdrantChunkIndexer.java
data/qdrant/QdrantDocumentVectorRepository.java
ingestion/indexing/QdrantDocumentChunkIndexer.java
retrieval/VectorDocumentSearcher.java
```

---

### ✅ Hybrid Retrieval

The retrieval layer combines keyword search, vector search, score merging, deduplication, reranking, permission filtering, and source span construction.

Implemented through:

```text
retrieval/HybridDocumentRetriever.java
retrieval/KeywordDocumentSearcher.java
retrieval/VectorDocumentSearcher.java
retrieval/RetrievalScoreMerger.java
retrieval/RetrievalDeduplicator.java
retrieval/DocumentReranker.java
retrieval/RetrievalEvidenceBuilder.java
retrieval/SourceSpanBuilder.java
retrieval/RetrievalPermissionFilter.java
retrieval/ContextAccessFilter.java
retrieval/RetrievalPolicy.java
```

---

### ✅ Citation Verification

The verification layer checks whether claims and evidence are supported by retrieved document spans.

Implemented through:

```text
verification/CitationVerifier.java
verification/EvidenceGraphVerifier.java
features/verifyevidencegraph
```

---

### ✅ DICE Evidence Graphs

The DICE layer converts retrieved document evidence into structured evidence graphs made of nodes, edges, facts, source spans, and verification status.

Implemented through:

```text
dice/DocumentDiceRuntime.java
dice/EvidenceBuildOrchestrator.java
dice/EvidenceGraphBuilder.java
dice/EvidenceGraphPolicy.java
dice/EvidenceGraphResponseCompactor.java
dice/EvidenceNormalizer.java
dice/EvidenceRelationLinker.java
dice/EvidenceSchemaValidator.java
```

---

### ✅ Gemini DICE Projection

Gemini is used to project retrieved evidence into structured DICE graph objects.

Implemented through:

```text
dice/gemini/GeminiDocumentDiceProjectionAdapter.java
dice/gemini/GeminiJsonExtractor.java
dice/gemini/GeminiLlmConfiguration.java
dice/gemini/GeminiLlmProperties.java
dice/gemini/GeminiProjectionPromptBuilder.java
dice/gemini/GeminiProjectionResponseValidator.java
```

---

### ✅ Embabel DICE Integration

The service includes an Embabel-compatible DICE projection path for evidence graph objects, facts, and actors.

Implemented through:

```text
dice/embabel/DocumentEvidenceActor.java
dice/embabel/DocumentEvidenceFact.java
dice/embabel/DocumentEvidenceObject.java
dice/embabel/EmbabelDiceConfiguration.java
dice/embabel/EmbabelDiceDocumentProjectionAdapter.java
```

---

### ✅ gRPC API

The service exposes document operations through gRPC.

Implemented through:

```text
grpc/DocumentServiceGrpcImpl.java
grpc/GrpcServerConfig.java
grpc/health/DocumentServiceStartupCheck.java
grpc/interceptors/GrpcPolicyEnforcementInterceptor.java
grpc/policies/DocumentResiliencePolicy.java
```

Supported operations include:

```text
UploadDocument
GetDocument
GetIngestionJob
SearchDocumentSpans
VerifyEvidenceGraph
BuildDocumentEvidence
```

---

### ✅ Observability

The service includes lifecycle logging for document upload, ingestion, retrieval, citation verification, and evidence graph generation.

Implemented through:

```text
observability/DocumentLifecycleLogger.java
observability/IngestionLifecycleLogger.java
observability/RetrievalLifecycleLogger.java
observability/CitationVerificationLogger.java
observability/EvidenceGraphVerificationLogger.java
observability/EvidenceBuildLogger.java
```

---

## Completed Feature Checklist

```text
✅ Document upload
✅ MinIO object storage
✅ Postgres metadata persistence
✅ Ingestion job tracking
✅ Async ingestion worker
✅ Text extraction
✅ PDF extraction
✅ DOCX extraction
✅ XLSX extraction
✅ OCR extraction path
✅ Document chunking
✅ Elasticsearch keyword indexing
✅ Gemini embeddings
✅ Qdrant vector indexing
✅ Dual keyword/vector indexing
✅ Keyword retrieval
✅ Vector retrieval
✅ Hybrid retrieval
✅ Retrieval filtering
✅ Retrieval deduplication
✅ Retrieval reranking
✅ Source span building
✅ Citation verification
✅ Evidence graph verification
✅ DICE evidence graph building
✅ Gemini DICE projection
✅ Embabel DICE projection
✅ gRPC API implementation
✅ Domain-specific exception model
✅ Lifecycle observability
✅ Multi-tenant request context
```

---

## Role in SparrowX

The Document Service is one of the three core SparrowX services:

```text
SparrowX
├── Agentic Service
├── Knowledge-Base Service
└── Document Service
```

The Agentic Service uses this service when it needs to answer questions grounded in uploaded documents. Instead of returning raw blobs, this service returns structured retrieval evidence, source spans, citations, verification results, and DICE evidence graphs.

This makes the Document Service the evidence layer for SparrowX.
