package com.sparrowx.agentic.temporal.workflow;

import com.sparrowx.agentic.temporal.model.MissionWorkflowCommand;
import com.sparrowx.agentic.temporal.model.MissionWorkflowInput;
import com.sparrowx.agentic.temporal.model.MissionWorkflowOutcome;
import com.sparrowx.agentic.temporal.model.MissionWorkflowState;
import io.temporal.workflow.QueryMethod;
import io.temporal.workflow.UpdateMethod;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface MissionWorkflow {

    String APPROVE_UPDATE = "ApproveMission";
    String REJECT_UPDATE = "RejectMission";
    String CANCEL_UPDATE = "CancelMission";
    String STATE_QUERY = "MissionWorkflowState";

    @WorkflowMethod(name = "MissionWorkflow")
    MissionWorkflowOutcome run(
            MissionWorkflowInput input,
            MissionWorkflowState continuedState
    );

    @UpdateMethod(name = APPROVE_UPDATE)
    MissionWorkflowState approve(
            MissionWorkflowCommand command
    );

    @UpdateMethod(name = REJECT_UPDATE)
    MissionWorkflowState reject(
            MissionWorkflowCommand command
    );

    @UpdateMethod(name = CANCEL_UPDATE)
    MissionWorkflowState cancel(
            MissionWorkflowCommand command
    );

    @QueryMethod(name = STATE_QUERY)
    MissionWorkflowState state();
}