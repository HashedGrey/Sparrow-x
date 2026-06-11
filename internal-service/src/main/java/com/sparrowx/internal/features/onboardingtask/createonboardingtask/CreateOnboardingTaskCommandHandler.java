package com.sparrowx.internal.features.onboardingtask.createonboardingtask;

import buildingblocks.core.commands.CommandHandler;
import com.sparrowx.internal.data.postgres.mappers.OnboardingTaskPersistenceMapper;
import com.sparrowx.internal.data.postgres.repositories.InternalDocumentJpaRepository;
import com.sparrowx.internal.data.postgres.repositories.OnboardingPathJpaRepository;
import com.sparrowx.internal.data.postgres.repositories.OnboardingTaskJpaRepository;
import com.sparrowx.internal.data.postgres.repositories.RunbookJpaRepository;
import com.sparrowx.internal.exceptions.InternalNotFoundException;
import com.sparrowx.internal.models.OnboardingTask;
import com.sparrowx.internal.valueobjects.CreatedAt;
import com.sparrowx.internal.valueobjects.InternalDocumentId;
import com.sparrowx.internal.valueobjects.OnboardingPathId;
import com.sparrowx.internal.valueobjects.OnboardingTaskId;
import com.sparrowx.internal.valueobjects.RunbookId;
import com.sparrowx.internal.valueobjects.TenantId;
import com.sparrowx.internal.valueobjects.UpdatedAt;
import org.springframework.stereotype.Component;

@Component
public class CreateOnboardingTaskCommandHandler
        implements CommandHandler<CreateOnboardingTaskCommand, CreateOnboardingTaskResult> {

    private final OnboardingTaskJpaRepository onboardingTaskJpaRepository;
    private final OnboardingPathJpaRepository onboardingPathJpaRepository;
    private final InternalDocumentJpaRepository internalDocumentJpaRepository;
    private final RunbookJpaRepository runbookJpaRepository;
    private final CreateOnboardingTaskCommandValidator validator;

    public CreateOnboardingTaskCommandHandler(
            OnboardingTaskJpaRepository onboardingTaskJpaRepository,
            OnboardingPathJpaRepository onboardingPathJpaRepository,
            InternalDocumentJpaRepository internalDocumentJpaRepository,
            RunbookJpaRepository runbookJpaRepository,
            CreateOnboardingTaskCommandValidator validator
    ) {
        this.onboardingTaskJpaRepository = onboardingTaskJpaRepository;
        this.onboardingPathJpaRepository = onboardingPathJpaRepository;
        this.internalDocumentJpaRepository = internalDocumentJpaRepository;
        this.runbookJpaRepository = runbookJpaRepository;
        this.validator = validator;
    }

    @Override
    public CreateOnboardingTaskResult handle(CreateOnboardingTaskCommand command) {
        validator.validate(command);

        if (onboardingPathJpaRepository.findByTenantIdAndOnboardingPathId(
                command.tenantId(),
                command.onboardingPathId()
        ).isEmpty()) {
            throw new InternalNotFoundException(
                    "Onboarding path not found: " + command.onboardingPathId()
            );
        }

        if (internalDocumentJpaRepository.findByTenantIdAndDocumentId(
                command.tenantId(),
                command.documentId()
        ).isEmpty()) {
            throw new InternalNotFoundException(
                    "Internal document not found: " + command.documentId()
            );
        }

        if (runbookJpaRepository.findByTenantIdAndRunbookId(
                command.tenantId(),
                command.runbookId()
        ).isEmpty()) {
            throw new InternalNotFoundException(
                    "Runbook not found: " + command.runbookId()
            );
        }

        var task = OnboardingTask.create(
                OnboardingTaskId.newId(),
                TenantId.of(command.tenantId()),
                OnboardingPathId.of(command.onboardingPathId()),
                command.title(),
                command.description(),
                InternalDocumentId.of(command.documentId()),
                RunbookId.of(command.runbookId()),
                command.sortOrder(),
                CreatedAt.now(),
                UpdatedAt.now()
        );

        var saved = onboardingTaskJpaRepository.save(
                OnboardingTaskPersistenceMapper.toEntity(task)
        );

        var created = OnboardingTaskPersistenceMapper.toDomain(saved);

        return new CreateOnboardingTaskResult(created);
    }
}