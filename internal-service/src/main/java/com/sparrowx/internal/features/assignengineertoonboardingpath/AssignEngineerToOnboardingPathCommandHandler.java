package com.sparrowx.internal.features.assignengineertoonboardingpath;

import buildingblocks.core.commands.CommandHandler;
import com.sparrowx.internal.data.postgres.mappers.EngineerOnboardingAssignmentPersistenceMapper;
import com.sparrowx.internal.data.postgres.repositories.EngineerJpaRepository;
import com.sparrowx.internal.data.postgres.repositories.EngineerOnboardingAssignmentJpaRepository;
import com.sparrowx.internal.data.postgres.repositories.OnboardingPathJpaRepository;
import com.sparrowx.internal.exceptions.InternalNotFoundException;
import com.sparrowx.internal.exceptions.InternalValidationException;
import com.sparrowx.internal.models.EngineerOnboardingAssignment;
import com.sparrowx.internal.valueobjects.EngineerId;
import com.sparrowx.internal.valueobjects.EngineerOnboardingAssignmentId;
import com.sparrowx.internal.valueobjects.OnboardingPathId;
import com.sparrowx.internal.valueobjects.TenantId;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class AssignEngineerToOnboardingPathCommandHandler
        implements CommandHandler<AssignEngineerToOnboardingPathCommand, AssignEngineerToOnboardingPathResult> {

    private final EngineerOnboardingAssignmentJpaRepository assignmentJpaRepository;
    private final EngineerJpaRepository engineerJpaRepository;
    private final OnboardingPathJpaRepository onboardingPathJpaRepository;
    private final AssignEngineerToOnboardingPathCommandValidator validator;

    public AssignEngineerToOnboardingPathCommandHandler(
            EngineerOnboardingAssignmentJpaRepository assignmentJpaRepository,
            EngineerJpaRepository engineerJpaRepository,
            OnboardingPathJpaRepository onboardingPathJpaRepository,
            AssignEngineerToOnboardingPathCommandValidator validator
    ) {
        this.assignmentJpaRepository = assignmentJpaRepository;
        this.engineerJpaRepository = engineerJpaRepository;
        this.onboardingPathJpaRepository = onboardingPathJpaRepository;
        this.validator = validator;
    }

    @Override
    public AssignEngineerToOnboardingPathResult handle(
            AssignEngineerToOnboardingPathCommand command
    ) {
        validator.validate(command);

        if (engineerJpaRepository.findByTenantIdAndEngineerId(
                command.tenantId(),
                command.engineerId()
        ).isEmpty()) {
            throw new InternalNotFoundException(
                    "Engineer not found: " + command.engineerId()
            );
        }

        if (onboardingPathJpaRepository.findByTenantIdAndOnboardingPathId(
                command.tenantId(),
                command.onboardingPathId()
        ).isEmpty()) {
            throw new InternalNotFoundException(
                    "Onboarding path not found: " + command.onboardingPathId()
            );
        }

        if (assignmentJpaRepository.existsByTenantIdAndEngineerIdAndOnboardingPathId(
                command.tenantId(),
                command.engineerId(),
                command.onboardingPathId()
        )) {
            throw new InternalValidationException(
                    "Engineer is already assigned to onboarding path"
            );
        }

        var assignment = EngineerOnboardingAssignment.assign(
                EngineerOnboardingAssignmentId.newId(),
                TenantId.of(command.tenantId()),
                EngineerId.of(command.engineerId()),
                OnboardingPathId.of(command.onboardingPathId()),
                Instant.now()
        );

        var saved = assignmentJpaRepository.save(
                EngineerOnboardingAssignmentPersistenceMapper.toEntity(assignment)
        );

        var created = EngineerOnboardingAssignmentPersistenceMapper.toDomain(saved);

        return new AssignEngineerToOnboardingPathResult(created);
    }
}