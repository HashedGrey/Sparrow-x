package com.sparrowx.internal.features.onboardingpath.createonboardingpath;

import buildingblocks.core.commands.CommandHandler;
import com.sparrowx.internal.data.postgres.mappers.OnboardingPathPersistenceMapper;
import com.sparrowx.internal.data.postgres.repositories.ModuleJpaRepository;
import com.sparrowx.internal.data.postgres.repositories.OnboardingPathJpaRepository;
import com.sparrowx.internal.exceptions.InternalNotFoundException;
import com.sparrowx.internal.exceptions.InternalValidationException;
import com.sparrowx.internal.models.OnboardingPath;
import com.sparrowx.internal.valueobjects.CreatedAt;
import com.sparrowx.internal.valueobjects.ModuleId;
import com.sparrowx.internal.valueobjects.OnboardingPathId;
import com.sparrowx.internal.valueobjects.TenantId;
import com.sparrowx.internal.valueobjects.UpdatedAt;
import org.springframework.stereotype.Component;

@Component
public class CreateOnboardingPathCommandHandler
        implements CommandHandler<CreateOnboardingPathCommand, CreateOnboardingPathResult> {

    private final OnboardingPathJpaRepository onboardingPathJpaRepository;
    private final ModuleJpaRepository moduleJpaRepository;
    private final CreateOnboardingPathCommandValidator validator;

    public CreateOnboardingPathCommandHandler(
            OnboardingPathJpaRepository onboardingPathJpaRepository,
            ModuleJpaRepository moduleJpaRepository,
            CreateOnboardingPathCommandValidator validator
    ) {
        this.onboardingPathJpaRepository = onboardingPathJpaRepository;
        this.moduleJpaRepository = moduleJpaRepository;
        this.validator = validator;
    }

    @Override
    public CreateOnboardingPathResult handle(CreateOnboardingPathCommand command) {
        validator.validate(command);

        if (moduleJpaRepository.findByTenantIdAndModuleId(
                command.tenantId(),
                command.targetModuleId()
        ).isEmpty()) {
            throw new InternalNotFoundException(
                    "Target module not found: " + command.targetModuleId()
            );
        }

        var slug = toSlug(command.name());

        if (onboardingPathJpaRepository.existsByTenantIdAndSlug(
                command.tenantId(),
                slug
        )) {
            throw new InternalValidationException(
                    "Onboarding path already exists: " + slug
            );
        }

        var onboardingPath = OnboardingPath.create(
                OnboardingPathId.newId(),
                TenantId.of(command.tenantId()),
                command.name(),
                slug,
                command.description(),
                ModuleId.of(command.targetModuleId()),
                CreatedAt.now(),
                UpdatedAt.now()
        );

        var saved = onboardingPathJpaRepository.save(
                OnboardingPathPersistenceMapper.toEntity(onboardingPath)
        );

        var created = OnboardingPathPersistenceMapper.toDomain(saved);

        return new CreateOnboardingPathResult(created);
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