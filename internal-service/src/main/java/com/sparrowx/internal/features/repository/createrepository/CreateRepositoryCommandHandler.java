package com.sparrowx.internal.features.repository.createrepository;

import buildingblocks.core.commands.CommandHandler;
import com.sparrowx.internal.data.postgres.mappers.RepositoryPersistenceMapper;
import com.sparrowx.internal.data.postgres.repositories.ModuleJpaRepository;
import com.sparrowx.internal.data.postgres.repositories.RepositoryJpaRepository;
import com.sparrowx.internal.exceptions.InternalNotFoundException;
import com.sparrowx.internal.exceptions.InternalValidationException;
import com.sparrowx.internal.models.Repository;
import com.sparrowx.internal.valueobjects.CreatedAt;
import com.sparrowx.internal.valueobjects.ModuleId;
import com.sparrowx.internal.valueobjects.RepositoryId;
import com.sparrowx.internal.valueobjects.TenantId;
import com.sparrowx.internal.valueobjects.UpdatedAt;
import org.springframework.stereotype.Component;

@Component
public class CreateRepositoryCommandHandler
        implements CommandHandler<CreateRepositoryCommand, CreateRepositoryResult> {

    private final RepositoryJpaRepository repositoryJpaRepository;
    private final ModuleJpaRepository moduleJpaRepository;
    private final CreateRepositoryCommandValidator validator;

    public CreateRepositoryCommandHandler(
            RepositoryJpaRepository repositoryJpaRepository,
            ModuleJpaRepository moduleJpaRepository,
            CreateRepositoryCommandValidator validator
    ) {
        this.repositoryJpaRepository = repositoryJpaRepository;
        this.moduleJpaRepository = moduleJpaRepository;
        this.validator = validator;
    }

    @Override
    public CreateRepositoryResult handle(CreateRepositoryCommand command) {
        validator.validate(command);

        if (moduleJpaRepository.findByTenantIdAndModuleId(
                command.tenantId(),
                command.moduleId()
        ).isEmpty()) {
            throw new InternalNotFoundException(
                    "Module not found: " + command.moduleId()
            );
        }

        var url = command.url().trim();

        if (repositoryJpaRepository.existsByTenantIdAndUrl(
                command.tenantId(),
                url
        )) {
            throw new InternalValidationException(
                    "Repository already exists for url: " + url
            );
        }

        var repository = Repository.create(
                RepositoryId.newId(),
                TenantId.of(command.tenantId()),
                command.name(),
                url,
                ModuleId.of(command.moduleId()),
                CreatedAt.now(),
                UpdatedAt.now()
        );

        var saved = repositoryJpaRepository.save(
                RepositoryPersistenceMapper.toEntity(repository)
        );

        var created = RepositoryPersistenceMapper.toDomain(saved);

        return new CreateRepositoryResult(created);
    }
}