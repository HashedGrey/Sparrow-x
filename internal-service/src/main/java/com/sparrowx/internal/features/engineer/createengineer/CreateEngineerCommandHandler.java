package com.sparrowx.internal.features.engineer.createengineer;

import buildingblocks.core.commands.CommandHandler;
import com.sparrowx.internal.data.postgres.mappers.EngineerPersistenceMapper;
import com.sparrowx.internal.data.postgres.repositories.EngineerJpaRepository;
import com.sparrowx.internal.exceptions.InternalValidationException;
import com.sparrowx.internal.models.Engineer;
import com.sparrowx.internal.valueobjects.CreatedAt;
import com.sparrowx.internal.valueobjects.EmailAddress;
import com.sparrowx.internal.valueobjects.EngineerId;
import com.sparrowx.internal.valueobjects.EngineerRole;
import com.sparrowx.internal.valueobjects.TenantId;
import com.sparrowx.internal.valueobjects.UpdatedAt;
import org.springframework.stereotype.Component;

@Component
public class CreateEngineerCommandHandler
        implements CommandHandler<CreateEngineerCommand, CreateEngineerResult> {

    private final EngineerJpaRepository engineerJpaRepository;
    private final CreateEngineerCommandValidator validator;

    public CreateEngineerCommandHandler(
            EngineerJpaRepository engineerJpaRepository,
            CreateEngineerCommandValidator validator
    ) {
        this.engineerJpaRepository = engineerJpaRepository;
        this.validator = validator;
    }

    @Override
    public CreateEngineerResult handle(CreateEngineerCommand command) {
        validator.validate(command);

        var email = EmailAddress.of(command.email());

        if (engineerJpaRepository.existsByTenantIdAndEmail(
                command.tenantId(),
                email.value()
        )) {
            throw new InternalValidationException(
                    "Engineer already exists for email: " + email.value()
            );
        }

        var engineer = Engineer.create(
                EngineerId.newId(),
                TenantId.of(command.tenantId()),
                command.fullName(),
                email,
                EngineerRole.from(command.role()),
                CreatedAt.now(),
                UpdatedAt.now()
        );

        var saved = engineerJpaRepository.save(
                EngineerPersistenceMapper.toEntity(engineer)
        );

        var created = EngineerPersistenceMapper.toDomain(saved);

        return new CreateEngineerResult(created);
    }
}