package com.sparrowx.internal.features.module.createmodule;

import buildingblocks.core.commands.CommandHandler;
import com.sparrowx.internal.data.postgres.mappers.ModulePersistenceMapper;
import com.sparrowx.internal.data.postgres.repositories.ModuleJpaRepository;
import com.sparrowx.internal.data.postgres.repositories.TeamJpaRepository;
import com.sparrowx.internal.exceptions.InternalNotFoundException;
import com.sparrowx.internal.exceptions.InternalValidationException;
import com.sparrowx.internal.models.Module;
import com.sparrowx.internal.valueobjects.CreatedAt;
import com.sparrowx.internal.valueobjects.ModuleId;
import com.sparrowx.internal.valueobjects.TeamId;
import com.sparrowx.internal.valueobjects.TenantId;
import com.sparrowx.internal.valueobjects.UpdatedAt;
import org.springframework.stereotype.Component;

@Component
public class CreateModuleCommandHandler
        implements CommandHandler<CreateModuleCommand, CreateModuleResult> {

    private final ModuleJpaRepository moduleJpaRepository;
    private final TeamJpaRepository teamJpaRepository;
    private final CreateModuleCommandValidator validator;

    public CreateModuleCommandHandler(
            ModuleJpaRepository moduleJpaRepository,
            TeamJpaRepository teamJpaRepository,
            CreateModuleCommandValidator validator
    ) {
        this.moduleJpaRepository = moduleJpaRepository;
        this.teamJpaRepository = teamJpaRepository;
        this.validator = validator;
    }

    @Override
    public CreateModuleResult handle(CreateModuleCommand command) {
        validator.validate(command);

        if (teamJpaRepository.findByTenantIdAndTeamId(
                command.tenantId(),
                command.owningTeamId()
        ).isEmpty()) {
            throw new InternalNotFoundException(
                    "Owning team not found: " + command.owningTeamId()
            );
        }

        var slug = toSlug(command.name());

        if (moduleJpaRepository.existsByTenantIdAndSlug(
                command.tenantId(),
                slug
        )) {
            throw new InternalValidationException(
                    "Module already exists: " + slug
            );
        }

        var module = Module.create(
                ModuleId.newId(),
                TenantId.of(command.tenantId()),
                command.name(),
                slug,
                command.description(),
                TeamId.of(command.owningTeamId()),
                CreatedAt.now(),
                UpdatedAt.now()
        );

        var saved = moduleJpaRepository.save(
                ModulePersistenceMapper.toEntity(module)
        );

        var created = ModulePersistenceMapper.toDomain(saved);

        return new CreateModuleResult(created);
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