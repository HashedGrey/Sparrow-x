# SparrowX Agentic Service

> **Disclaimer:** This README describes the planned replacement service. 
> The current implementation and existing skeleton are outdated, may not 
> match this architecture and should not be treated as authoritative 
> until the service is updated.

## Overview

This Service is the orchestration layer responsible for executing durable, multi-step knowledge missions across internal company data and documents.

It exposes gRPC operations for submitting missions, streaming progress, retrieving results, managing human approvals, and cancelling active missions. Commands and queries use the shared `CommandBus`, `QueryBus`, and `EventBus`.

## Temporal Orchestration

Each accepted mission is persisted and started using a stable Temporal Workflow ID.

Temporal owns:

* Activity scheduling and retries
* Cancellation
* Durable human-gate waiting
* Workflow Updates
* Recovery and replay
* Continue-As-New decisions

PostgreSQL stores business projections, progress events, approval records, and external checkpoints. It does not replace Temporal as the execution authority.

## Embabel Agent Reasoning

Embabel performs bounded reasoning inside Temporal Activities.

It classifies intent, creates or revises mission plans, selects authorized capabilities, reviews observations, and determines whether the mission requires another execution hop or final synthesis.

The Workflow repeatedly performs:

1. Planning or review
2. One Activity execution
3. Observation persistence
4. Replanning or completion

## Service Integrations

The Agentic Service uses Document Service for uploads, ingestion tracking, document search, evidence construction, and evidence verification.

It uses Internal Service for entity resolution, company graph queries, and learning graph queries.

Structured LLM calls are permitted only inside Temporal Activities.

## Governance and Grounding

Policies enforce tenant isolation, budgets, source authorization, tool authorization, redaction, grounding, citation coverage, and human approval requirements.

Final answers are synthesized only after required evidence has been registered and grounding validation succeeds.

## Technology

The service targets Java 21, Spring Boot 3.x, gRPC, Embabel, Temporal and PostgreSQL.

All external mutations must be idempotent, observable, replay-safe and policy-governed.
