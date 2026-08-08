package com.sparrowx.agentic.agents;

import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.sparrowx.agentic.components.IntentComponent;
import com.sparrowx.agentic.components.IntentComponent.IntentRequest;
import com.sparrowx.agentic.components.PlanningComponent;
import com.sparrowx.agentic.components.PlanningComponent.PlanningRequest;
import com.sparrowx.agentic.components.ReviewComponent;
import com.sparrowx.agentic.components.ReviewComponent.DecisionType;
import com.sparrowx.agentic.components.ReviewComponent.ReviewDecision;
import com.sparrowx.agentic.components.ReviewComponent.ReviewRequest;
import com.sparrowx.agentic.components.SynthesisComponent;
import com.sparrowx.agentic.components.SynthesisComponent.SynthesisDraft;
import com.sparrowx.agentic.components.SynthesisComponent.SynthesisRequest;
import com.sparrowx.agentic.components.ToolSelectionComponent;
import com.sparrowx.agentic.components.ToolSelectionComponent.SelectionRequest;
import com.sparrowx.agentic.planning.MissionIntent;
import com.sparrowx.agentic.planning.MissionPlan;
import com.sparrowx.agentic.planning.PlannedStep;

import java.util.Objects;

@Agent(description = "Selects one authorized SparrowX mission action per invocation")
public final class MissionAgent {

    private final IntentComponent intentComponent;
    private final PlanningComponent planningComponent;
    private final ReviewComponent reviewComponent;
    private final SynthesisComponent synthesisComponent;
    private final ToolSelectionComponent toolSelectionComponent;

    public MissionAgent(
            IntentComponent intentComponent,
            PlanningComponent planningComponent,
            ReviewComponent reviewComponent,
            SynthesisComponent synthesisComponent,
            ToolSelectionComponent toolSelectionComponent) {

        this.intentComponent = Objects.requireNonNull(
                intentComponent,
                "intentComponent must not be null");

        this.planningComponent = Objects.requireNonNull(
                planningComponent,
                "planningComponent must not be null");

        this.reviewComponent = Objects.requireNonNull(
                reviewComponent,
                "reviewComponent must not be null");

        this.synthesisComponent = Objects.requireNonNull(
                synthesisComponent,
                "synthesisComponent must not be null");

        this.toolSelectionComponent = Objects.requireNonNull(
                toolSelectionComponent,
                "toolSelectionComponent must not be null");
    }

    @Action
    public MissionIntent interpret(IntentRequest request) {
        return intentComponent.interpret(request);
    }

    @Action
    public TurnDecision decide(TurnInput input) {
        Objects.requireNonNull(input, "input must not be null");

        MissionPlan plan = input.planningRequest().currentPlan();

        if (plan == null && input.reviewRequest() != null) {
            plan = input.reviewRequest().plan();
        }

        ReviewDecision reviewDecision = null;

        if (input.reviewRequest() != null) {
            reviewDecision =
                    reviewComponent.review(input.reviewRequest());

            if (reviewDecision.type() == DecisionType.COMPLETE) {
                return TurnDecision.complete(
                        plan,
                        reviewDecision.reason());
            }

            if (reviewDecision.type()
                    == DecisionType.WAIT_FOR_APPROVAL) {
                return TurnDecision.waitForApproval(
                        plan,
                        reviewDecision.reason());
            }

            if (reviewDecision.type() == DecisionType.FAIL) {
                return TurnDecision.fail(
                        plan,
                        reviewDecision.reason());
            }
        }

        if (plan == null
                || reviewDecision != null
                && reviewDecision.type() == DecisionType.REPLAN) {

            plan = planningComponent.plan(input.planningRequest());
        }

        SelectionRequest selectionRequest =
                input.selectionRequest().withPlan(plan);

        PlannedStep nextStep =
                toolSelectionComponent.select(selectionRequest);

        String reason = reviewDecision == null
                ? "Initial authorized step selected"
                : reviewDecision.reason();

        return TurnDecision.execute(plan, nextStep, reason);
    }

    @Action
    public SynthesisDraft synthesize(SynthesisRequest request) {
        return synthesisComponent.synthesize(request);
    }

    public record TurnInput(
            PlanningRequest planningRequest,
            ReviewRequest reviewRequest,
            SelectionRequest selectionRequest) {

        public TurnInput {
            planningRequest = Objects.requireNonNull(
                    planningRequest,
                    "planningRequest must not be null");

            selectionRequest = Objects.requireNonNull(
                    selectionRequest,
                    "selectionRequest must not be null");
        }
    }

    public record TurnDecision(
            TurnDecisionType type,
            MissionPlan plan,
            PlannedStep nextStep,
            String reason) {

        public TurnDecision {
            type = Objects.requireNonNull(
                    type,
                    "type must not be null");

            reason = reason == null ? "" : reason;

            if (type == TurnDecisionType.EXECUTE_STEP
                    && nextStep == null) {
                throw new IllegalArgumentException(
                        "nextStep is required for EXECUTE_STEP");
            }
        }

        public static TurnDecision execute(
                MissionPlan plan,
                PlannedStep nextStep,
                String reason) {

            return new TurnDecision(
                    TurnDecisionType.EXECUTE_STEP,
                    Objects.requireNonNull(
                            plan,
                            "plan must not be null"),
                    Objects.requireNonNull(
                            nextStep,
                            "nextStep must not be null"),
                    reason);
        }

        public static TurnDecision complete(
                MissionPlan plan,
                String reason) {

            return new TurnDecision(
                    TurnDecisionType.COMPLETE,
                    plan,
                    null,
                    reason);
        }

        public static TurnDecision waitForApproval(
                MissionPlan plan,
                String reason) {

            return new TurnDecision(
                    TurnDecisionType.WAIT_FOR_APPROVAL,
                    plan,
                    null,
                    reason);
        }

        public static TurnDecision fail(
                MissionPlan plan,
                String reason) {

            return new TurnDecision(
                    TurnDecisionType.FAIL,
                    plan,
                    null,
                    reason);
        }
    }

    public enum TurnDecisionType {
        EXECUTE_STEP,
        COMPLETE,
        WAIT_FOR_APPROVAL,
        FAIL
    }
}