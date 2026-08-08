package com.sparrowx.agentic.steps;

import com.sparrowx.agentic.actions.governance.CheckGroundingAction;
import com.sparrowx.agentic.mission.evidence.EvidenceRegistry;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public final class VerifyGroundingStep {

    private final CheckGroundingAction checkGroundingAction;

    public VerifyGroundingStep(
            CheckGroundingAction checkGroundingAction
    ) {
        this.checkGroundingAction = Objects.requireNonNull(
                checkGroundingAction,
                "checkGroundingAction must not be null"
        );
    }

    public CheckGroundingAction.Result execute(
            CheckGroundingAction.CheckSpec spec,
            EvidenceRegistry evidenceRegistry
    ) {
        Objects.requireNonNull(spec, "spec must not be null");
        Objects.requireNonNull(
                evidenceRegistry,
                "evidenceRegistry must not be null"
        );

        return checkGroundingAction.execute(spec, evidenceRegistry);
    }
}