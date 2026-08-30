package com.sparrowx.agentic.features.cancelmission;

import buildingblocks.core.commands.CommandHandler;
import com.sparrowx.agentic.mission.model.MissionContext;
import com.sparrowx.agentic.mission.model.MissionStatus;
import com.sparrowx.agentic.temporal.client.TemporalMissionClient;
import com.sparrowx.agentic.temporal.model.MissionWorkflowCommand;
import com.sparrowx.agentic.temporal.model.MissionWorkflowState;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public final class CancelMissionCommandHandler
        implements CommandHandler<CancelMissionCommand, CancelMissionResult> {

    private final CancelMissionCommandValidator validator;
    private final TemporalMissionClient temporalMissionClient;

    public CancelMissionCommandHandler(
            CancelMissionCommandValidator validator,
            TemporalMissionClient temporalMissionClient
    ) {
        this.validator = validator;
        this.temporalMissionClient = temporalMissionClient;
    }

    @Override
    public CancelMissionResult handle(CancelMissionCommand command) {
        validator.validate(command);

        MissionContext context = command.context();
        MissionWorkflowCommand workflowCommand =
                MissionWorkflowCommand.cancel(
                        context.requestId(),
                        context.tenantId(),
                        command.missionId(),
                        context.userId(),
                        command.reason()
                );

        MissionWorkflowState state =
                temporalMissionClient.cancel(workflowCommand);

        verifyScope(state, context.tenantId(), command.missionId());

        if (state.status() != MissionStatus.CANCELLED) {
            throw new IllegalStateException(
                    "Cancel update returned before the mission reached CANCELLED"
            );
        }

        Instant cancelledAt = state.cancelledAt();
        if (cancelledAt == null) {
            throw new IllegalStateException(
                    "Cancel update returned no durable cancellation timestamp"
            );
        }

        return new CancelMissionResult(
                state.missionId(),
                state.status(),
                cancelledAt
        );
    }

    private static void verifyScope(
            MissionWorkflowState state,
            String tenantId,
            String missionId
    ) {
        if (state == null) {
            throw new IllegalStateException(
                    "Cancel update returned no Workflow state"
            );
        }

        if (!tenantId.equals(state.tenantId())
                || !missionId.equals(state.missionId())) {
            throw new IllegalStateException(
                    "Cancel update returned Workflow state outside the requested scope"
            );
        }
    }
}