package com.sparrowx.internal.features.team.createteam;

import buildingblocks.core.commands.CommandHandler;
import com.sparrowx.internal.data.postgres.mappers.TeamPersistenceMapper;
import com.sparrowx.internal.data.postgres.repositories.TeamJpaRepository;
import com.sparrowx.internal.exceptions.InternalValidationException;
import com.sparrowx.internal.models.Team;
import com.sparrowx.internal.valueobjects.CreatedAt;
import com.sparrowx.internal.valueobjects.TeamId;
import com.sparrowx.internal.valueobjects.TenantId;
import com.sparrowx.internal.valueobjects.UpdatedAt;
import org.springframework.stereotype.Component;

@Component
public class CreateTeamCommandHandler
        implements CommandHandler<CreateTeamCommand, CreateTeamResult> {

    private final TeamJpaRepository teamJpaRepository;
    private final CreateTeamCommandValidator validator;

    public CreateTeamCommandHandler(
            TeamJpaRepository teamJpaRepository,
            CreateTeamCommandValidator validator
    ) {
        this.teamJpaRepository = teamJpaRepository;
        this.validator = validator;
    }

    @Override
    public CreateTeamResult handle(CreateTeamCommand command) {
        validator.validate(command);

        var slug = toSlug(command.name());

        if (teamJpaRepository.existsByTenantIdAndSlug(
                command.tenantId(),
                slug
        )) {
            throw new InternalValidationException(
                    "Team already exists: " + slug
            );
        }

        var team = Team.create(
                TeamId.newId(),
                TenantId.of(command.tenantId()),
                command.name(),
                slug,
                command.description(),
                CreatedAt.now(),
                UpdatedAt.now()
        );

        var saved = teamJpaRepository.save(
                TeamPersistenceMapper.toEntity(team)
        );

        var created = TeamPersistenceMapper.toDomain(saved);

        return new CreateTeamResult(created);
    }

    private String toSlug(String value) {
        return value == null
                ? ""
                : value.trim()
                .toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
    }
}