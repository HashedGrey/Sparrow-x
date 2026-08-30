package com.sparrowx.agentic.temporal.client;

import com.sparrowx.agentic.temporal.model.MissionWorkflowCommand;
import com.sparrowx.agentic.temporal.model.MissionWorkflowInput;
import com.sparrowx.agentic.temporal.model.MissionWorkflowState;
import com.sparrowx.agentic.temporal.workflow.MissionWorkflow;
import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.api.enums.v1.WorkflowIdReusePolicy;
import io.temporal.client.UpdateOptions;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowExecutionAlreadyStarted;
import io.temporal.client.WorkflowNotFoundException;
import io.temporal.client.WorkflowOptions;
import io.temporal.client.WorkflowStub;
import io.temporal.client.WorkflowUpdateHandle;
import io.temporal.client.WorkflowUpdateStage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;

/** Sole application facade over Temporal's WorkflowClient. */
@Component
public final class TemporalMissionClient {

    private final WorkflowClient workflowClient;
    private final String taskQueue;

    public TemporalMissionClient(
            WorkflowClient workflowClient,
            @Value("${sparrowx.agentic.temporal.task-queue:"
                    + "agentic-mission-task-queue}")
            String taskQueue
    ) {
        this.workflowClient = Objects.requireNonNull(
                workflowClient,
                "workflowClient must not be null"
        );
        this.taskQueue = requireText(taskQueue, "taskQueue");
    }

    public WorkflowExecution startOrGet(MissionWorkflowInput input) {
        Objects.requireNonNull(input, "input must not be null");
        String workflowId = workflowId(
                input.tenantId(),
                input.missionId()
        );
        MissionWorkflow workflow = workflowClient.newWorkflowStub(
                MissionWorkflow.class,
                WorkflowOptions.newBuilder()
                        .setWorkflowId(workflowId)
                        .setTaskQueue(taskQueue)
                        .setWorkflowIdReusePolicy(
                                WorkflowIdReusePolicy
                                        .WORKFLOW_ID_REUSE_POLICY_ALLOW_DUPLICATE_FAILED_ONLY
                        )
                        .build()
        );
        try {
            return WorkflowClient.start(workflow::run, input, null);
        } catch (WorkflowExecutionAlreadyStarted exception) {
            return exception.getExecution();
        }
    }

    public MissionWorkflowState approve(MissionWorkflowCommand command) {
        return executeUpdate(
                command,
                MissionWorkflowCommand.CommandType.APPROVE,
                MissionWorkflow.APPROVE_UPDATE
        );
    }

    public MissionWorkflowState reject(MissionWorkflowCommand command) {
        return executeUpdate(
                command,
                MissionWorkflowCommand.CommandType.REJECT,
                MissionWorkflow.REJECT_UPDATE
        );
    }

    public MissionWorkflowState cancel(MissionWorkflowCommand command) {
        return executeUpdate(
                command,
                MissionWorkflowCommand.CommandType.CANCEL,
                MissionWorkflow.CANCEL_UPDATE
        );
    }

    private MissionWorkflowState executeUpdate(
            MissionWorkflowCommand command,
            MissionWorkflowCommand.CommandType expectedType,
            String updateName
    ) {
        Objects.requireNonNull(command, "command must not be null");
        if (command.type() != expectedType) {
            throw new IllegalArgumentException(
                    "Workflow Update command type mismatch"
            );
        }

        MissionWorkflow typed = workflowClient.newWorkflowStub(
                MissionWorkflow.class,
                workflowId(command.tenantId(), command.missionId())
        );
        WorkflowStub stub = WorkflowStub.fromTyped(typed);
        UpdateOptions<MissionWorkflowState> options =
                UpdateOptions.<MissionWorkflowState>newBuilder()
                        .setUpdateName(updateName)
                        .setUpdateId(command.updateId())
                        .setWaitForStage(WorkflowUpdateStage.COMPLETED)
                        .setResultClass(MissionWorkflowState.class)
                        .build();

        MissionWorkflowState result;
        try {
            WorkflowUpdateHandle<MissionWorkflowState> handle =
                    stub.startUpdate(options, command);
            result = handle.getResult();
        } catch (WorkflowNotFoundException exception) {
            result = stub.getUpdateHandle(
                    command.updateId(),
                    MissionWorkflowState.class
            ).getResult();
        }
        return requireMatchingUpdateResult(command, result);
    }

    private static MissionWorkflowState requireMatchingUpdateResult(
            MissionWorkflowCommand command,
            MissionWorkflowState state
    ) {
        Objects.requireNonNull(
                state,
                "Temporal Workflow Update returned null"
        );
        if (!command.tenantId().equals(state.tenantId())
                || !command.missionId().equals(state.missionId())) {
            throw new IllegalStateException(
                    "Temporal Workflow Update returned another mission"
            );
        }
        String fingerprint = state.processedUpdateFingerprints()
                .get(command.updateId());
        if (!command.fingerprint().equals(fingerprint)) {
            throw new IllegalStateException(
                    "WORKFLOW_UPDATE_ID_CONFLICT: " + command.updateId()
            );
        }
        return state;
    }

    public static String workflowId(String tenantId, String missionId) {
        return "mission:"
                + encode(requireText(tenantId, "tenantId"))
                + ":"
                + encode(requireText(missionId, "missionId"));
    }

    private static String encode(String value) {
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    field + " must not be blank"
            );
        }
        return value.trim();
    }
}
