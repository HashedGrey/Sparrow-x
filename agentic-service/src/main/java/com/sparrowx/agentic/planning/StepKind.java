package com.sparrowx.agentic.planning;

public enum StepKind {
    PREPARE_INPUT_ARTIFACTS(
            "agentic.artifacts.prepare",
            CapabilityGroup.AGENTIC),
    UPLOAD_DOCUMENT(
            "document.upload",
            CapabilityGroup.DOCUMENT),
    GET_INGESTION_JOB(
            "document.ingestion.get",
            CapabilityGroup.DOCUMENT),
    SEARCH_DOCUMENT_SPANS(
            "document.spans.search",
            CapabilityGroup.DOCUMENT),
    BUILD_DOCUMENT_EVIDENCE(
            "document.evidence.build",
            CapabilityGroup.DOCUMENT),
    VERIFY_DOCUMENT_EVIDENCE(
            "document.evidence.verify",
            CapabilityGroup.DOCUMENT),
    SEARCH_INTERNAL_ENTITIES(
            "internal.entities.search",
            CapabilityGroup.INTERNAL),
    READ_INTERNAL_COMPANY_GRAPH(
            "internal.company-graph.read",
            CapabilityGroup.INTERNAL),
    READ_LEARNING_GRAPH(
            "internal.learning-graph.read",
            CapabilityGroup.INTERNAL),
    APPLY_REDACTION(
            "governance.redaction.apply",
            CapabilityGroup.GOVERNANCE),
    CHECK_GROUNDING(
            "governance.grounding.check",
            CapabilityGroup.GOVERNANCE),
    REQUEST_HUMAN_APPROVAL(
            "governance.human-approval.request",
            CapabilityGroup.GOVERNANCE),
    BUILD_CITATIONS(
            "synthesis.citations.build",
            CapabilityGroup.SYNTHESIS),
    COMPOSE_ANSWER(
            "synthesis.answer.compose",
            CapabilityGroup.SYNTHESIS);

    private final String capability;
    private final CapabilityGroup group;

    StepKind(
            String capability,
            CapabilityGroup group) {

        this.capability = capability;
        this.group = group;
    }

    public String capability() {
        return capability;
    }

    public CapabilityGroup group() {
        return group;
    }

    public boolean isDocumentOperation() {
        return group == CapabilityGroup.DOCUMENT;
    }

    public boolean isInternalOperation() {
        return group == CapabilityGroup.INTERNAL;
    }

    public enum CapabilityGroup {
        AGENTIC,
        DOCUMENT,
        INTERNAL,
        GOVERNANCE,
        SYNTHESIS
    }
}