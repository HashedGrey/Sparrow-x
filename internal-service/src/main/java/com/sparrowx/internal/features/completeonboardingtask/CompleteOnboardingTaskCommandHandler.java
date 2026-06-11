package com.sparrowx.internal.features.completeonboardingtask;

import buildingblocks.core.commands.CommandHandler;
import com.sparrowx.internal.data.postgres.mappers.EngineerOnboardingTaskProgressPersistenceMapper;
import com.sparrowx.internal.data.postgres.repositories.EngineerOnboardingAssignmentJpaRepository;
import com.sparrowx.internal.data.postgres.repositories.EngineerOnboardingTaskProgressJpaRepository;
import com.sparrowx.internal.data.postgres.repositories.OnboardingTaskJpaRepository;
import com.sparrowx.internal.exceptions.InternalNotFoundException;
import com.sparrowx.internal.exceptions.InternalValidationException;
import com.sparrowx.internal.models.EngineerOnboardingTaskProgress;
import com.sparrowx.internal.valueobjects.EngineerOnboardingAssignmentId;
import com.sparrowx.internal.valueobjects.EngineerOnboardingTaskProgressId;
import com.sparrowx.internal.valueobjects.OnboardingTaskId;
import com.sparrowx.internal.valueobjects.TenantId;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class CompleteOnboardingTaskCommandHandler
        implements CommandHandler<CompleteOnboardingTaskCommand, CompleteOnboardingTaskResult> {

    private final EngineerOnboardingTaskProgressJpaRepository taskProgressJpaRepository;
    private final EngineerOnboardingAssignmentJpaRepository assignmentJpaRepository;
    private final OnboardingTaskJpaRepository onboardingTaskJpaRepository;
    private final CompleteOnboardingTaskCommandValidator validator;

    public CompleteOnboardingTaskCommandHandler(
            EngineerOnboardingTaskProgressJpaRepository taskProgressJpaRepository,
            EngineerOnboardingAssignmentJpaRepository assignmentJpaRepository,
            OnboardingTaskJpaRepository onboardingTaskJpaRepository,
            CompleteOnboardingTaskCommandValidator validator
    ) {
        this.taskProgressJpaRepository = taskProgressJpaRepository;
        this.assignmentJpaRepository = assignmentJpaRepository;
        this.onboardingTaskJpaRepository = onboardingTaskJpaRepository;
        this.validator = validator;
    }

    @Override
    public CompleteOnboardingTaskResult handle(
            CompleteOnboardingTaskCommand command
    ) {
        validator.validate(command);

        if (assignmentJpaRepository.findByTenantIdAndAssignmentId(
                command.tenantId(),
                command.assignmentId()
        ).isEmpty()) {
            throw new InternalNotFoundException(
                    "Onboarding assignment not found: " + command.assignmentId()
            );
        }

        if (onboardingTaskJpaRepository.findByTenantIdAndOnboardingTaskId(
                command.tenantId(),
                command.onboardingTaskId()
        ).isEmpty()) {
            throw new InternalNotFoundException(
                    "Onboarding task not found: " + command.onboardingTaskId()
            );
        }

        if (taskProgressJpaRepository.existsByTenantIdAndAssignmentIdAndOnboardingTaskId(
                command.tenantId(),
                command.assignmentId(),
                command.onboardingTaskId()
        )) {
            throw new InternalValidationException(
                    "Onboarding task already completed for assignment"
            );
        }

        var completedAt = Instant.now();

        var progress = EngineerOnboardingTaskProgress.completed(
                EngineerOnboardingTaskProgressId.newId(),
                TenantId.of(command.tenantId()),
                EngineerOnboardingAssignmentId.of(command.assignmentId()),
                OnboardingTaskId.of(command.onboardingTaskId()),
                command.completionNote(),
                completedAt
        );

        var saved = taskProgressJpaRepository.save(
                EngineerOnboardingTaskProgressPersistenceMapper.toEntity(progress)
        );

        var completed = EngineerOnboardingTaskProgressPersistenceMapper.toDomain(saved);

        return new CompleteOnboardingTaskResult(completed);
    }
}