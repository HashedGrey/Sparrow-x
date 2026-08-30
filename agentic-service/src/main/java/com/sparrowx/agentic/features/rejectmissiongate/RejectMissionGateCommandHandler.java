package com.sparrowx.agentic.features.rejectmissiongate;

import buildingblocks.core.commands.CommandHandler;
import com.sparrowx.agentic.mission.model.MissionContext;
import com.sparrowx.agentic.temporal.client.TemporalMissionClient;
import com.sparrowx.agentic.temporal.model.MissionWorkflowCommand;
import com.sparrowx.agentic.temporal.model.MissionWorkflowState;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

@Component
public final class RejectMissionGateCommandHandler
        implements CommandHandler<RejectMissionGateCommand, RejectMissionGateResult> {

    private static final Set<String> ROLE_METADATA_KEYS =
            Set.of("reviewer_roles", "roles", "role");

    private final RejectMissionGateCommandValidator validator;
    private final TemporalMissionClient temporalMissionClient;

    public RejectMissionGateCommandHandler(
            RejectMissionGateCommandValidator validator,
            TemporalMissionClient temporalMissionClient
    ) {
        this.validator = validator;
        this.temporalMissionClient = temporalMissionClient;
    }

    @Override
    public RejectMissionGateResult handle(RejectMissionGateCommand command) {
        validator.validate(command);

        MissionContext context = command.context();
        MissionWorkflowCommand workflowCommand =
                MissionWorkflowCommand.reject(
                        context.requestId(),
                        context.tenantId(),
                        command.missionId(),
                        command.gateId(),
                        context.userId(),
                        reviewerRoles(context),
                        command.reason()
                );

        MissionWorkflowState state =
                temporalMissionClient.reject(workflowCommand);

        verifyScope(state, context.tenantId(), command.missionId());

        if (state.status() == null) {
            throw new IllegalStateException(
                    "Reject update returned no mission status"
            );
        }

        Instant rejectedAt = state.rejectedAt();
        if (rejectedAt == null) {
            throw new IllegalStateException(
                    "Reject update returned no durable rejection timestamp"
            );
        }

        return new RejectMissionGateResult(
                state.missionId(),
                state.status(),
                rejectedAt
        );
    }

    private static Set<String> reviewerRoles(MissionContext context) {
        Map<String, String> metadata = context.metadata();
        if (metadata == null || metadata.isEmpty()) {
            return Set.of();
        }

        TreeSet<String> roles = new TreeSet<>();

        metadata.forEach((key, value) -> {
            if (!isRoleMetadataKey(key) || value == null) {
                return;
            }

            for (String candidate : value.split("[,;]")) {
                String role = candidate.trim();
                if (!role.isEmpty()) {
                    roles.add(role);
                }
            }
        });

        return Collections.unmodifiableSet(
                new LinkedHashSet<>(roles)
        );
    }

    private static boolean isRoleMetadataKey(String key) {
        return key != null
                && ROLE_METADATA_KEYS.contains(
                key.trim().toLowerCase(Locale.ROOT)
        );
    }

    private static void verifyScope(
            MissionWorkflowState state,
            String tenantId,
            String missionId
    ) {
        if (state == null) {
            throw new IllegalStateException(
                    "Reject update returned no Workflow state"
            );
        }

        if (!tenantId.equals(state.tenantId())
                || !missionId.equals(state.missionId())) {
            throw new IllegalStateException(
                    "Reject update returned Workflow state outside the requested scope"
            );
        }
    }
}