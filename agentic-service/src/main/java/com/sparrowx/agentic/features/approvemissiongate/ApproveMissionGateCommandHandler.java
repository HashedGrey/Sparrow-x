package com.sparrowx.agentic.features.approvemissiongate;

import buildingblocks.core.commands.CommandHandler;
import com.sparrowx.agentic.temporal.client.TemporalMissionClient;
import com.sparrowx.agentic.temporal.model.MissionWorkflowCommand;
import com.sparrowx.agentic.temporal.model.MissionWorkflowState;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

@Component
public final class ApproveMissionGateCommandHandler
        implements CommandHandler<
        ApproveMissionGateCommand,
        ApproveMissionGateResult> {

    private final ApproveMissionGateCommandValidator validator;
    private final TemporalMissionClient temporalMissionClient;

    public ApproveMissionGateCommandHandler(
            ApproveMissionGateCommandValidator validator,
            TemporalMissionClient temporalMissionClient
    ) {
        this.validator = Objects.requireNonNull(
                validator,
                "validator must not be null"
        );
        this.temporalMissionClient = Objects.requireNonNull(
                temporalMissionClient,
                "temporalMissionClient must not be null"
        );
    }

    @Override
    public ApproveMissionGateResult handle(
            ApproveMissionGateCommand command
    ) {
        validator.validate(command);

        MissionWorkflowCommand update =
                MissionWorkflowCommand.approve(
                        command.context().requestId(),
                        command.context().tenantId(),
                        command.missionId(),
                        command.gateId(),
                        command.context().userId(),
                        reviewerRoles(command.context().metadata()),
                        command.note()
                );

        MissionWorkflowState state =
                temporalMissionClient.approve(update);

        requireScope(command, state);

        Instant approvedAt = Objects.requireNonNull(
                state.approvedAt(),
                "approved Workflow state requires approvedAt"
        );

        return new ApproveMissionGateResult(
                state.missionId(),
                state.status(),
                approvedAt
        );
    }

    private static void requireScope(
            ApproveMissionGateCommand command,
            MissionWorkflowState state
    ) {
        Objects.requireNonNull(
                state,
                "Temporal approve update returned null"
        );

        if (!command.missionId().equals(state.missionId())
                || !command.context().tenantId().equals(
                state.tenantId()
        )) {
            throw new IllegalStateException(
                    "Temporal approve update returned another mission"
            );
        }
    }

    private static Set<String> reviewerRoles(
            Map<String, String> metadata
    ) {
        Map<String, String> values =
                metadata == null ? Map.of() : metadata;

        String encoded = firstText(
                values.get("reviewer_roles"),
                values.get("roles"),
                values.get("role")
        );

        if (encoded.isBlank()) {
            return Set.of();
        }

        TreeSet<String> roles = new TreeSet<>();
        for (String candidate : encoded.split(",")) {
            String role = candidate.trim();
            if (!role.isEmpty()) {
                roles.add(role);
            }
        }

        return Collections.unmodifiableSet(roles);
    }

    private static String firstText(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "";
    }
}