package com.sparrowx.agentic.steps;

import com.sparrowx.agentic.runtime.gate.ApprovalService;
import com.sparrowx.agentic.runtime.gate.HumanGate;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public final class RequestHumanApprovalStep {

    private final ApprovalService approvalService;

    public RequestHumanApprovalStep(ApprovalService approvalService) {
        this.approvalService = Objects.requireNonNull(
                approvalService,
                "approvalService must not be null"
        );
    }

    public HumanGate execute(ApprovalService.OpenRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return approvalService.open(request);
    }
}