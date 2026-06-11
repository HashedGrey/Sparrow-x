package com.sparrowx.internal.features.runbook.createrunbook;

import buildingblocks.core.commands.CommandHandler;
import com.sparrowx.internal.data.postgres.mappers.RunbookPersistenceMapper;
import com.sparrowx.internal.data.postgres.repositories.InternalDocumentJpaRepository;
import com.sparrowx.internal.data.postgres.repositories.ModuleJpaRepository;
import com.sparrowx.internal.data.postgres.repositories.RunbookJpaRepository;
import com.sparrowx.internal.exceptions.InternalNotFoundException;
import com.sparrowx.internal.exceptions.InternalValidationException;
import com.sparrowx.internal.models.Runbook;
import com.sparrowx.internal.valueobjects.CreatedAt;
import com.sparrowx.internal.valueobjects.InternalDocumentId;
import com.sparrowx.internal.valueobjects.ModuleId;
import com.sparrowx.internal.valueobjects.RunbookId;
import com.sparrowx.internal.valueobjects.TenantId;
import com.sparrowx.internal.valueobjects.UpdatedAt;
import org.springframework.stereotype.Component;

@Component
public class CreateRunbookCommandHandler
        implements CommandHandler<CreateRunbookCommand, CreateRunbookResult> {

    private final RunbookJpaRepository runbookJpaRepository;
    private final ModuleJpaRepository moduleJpaRepository;
    private final InternalDocumentJpaRepository internalDocumentJpaRepository;
    private final CreateRunbookCommandValidator validator;

    public CreateRunbookCommandHandler(
            RunbookJpaRepository runbookJpaRepository,
            ModuleJpaRepository moduleJpaRepository,
            InternalDocumentJpaRepository internalDocumentJpaRepository,
            CreateRunbookCommandValidator validator
    ) {
        this.runbookJpaRepository = runbookJpaRepository;
        this.moduleJpaRepository = moduleJpaRepository;
        this.internalDocumentJpaRepository = internalDocumentJpaRepository;
        this.validator = validator;
    }

    @Override
    public CreateRunbookResult handle(CreateRunbookCommand command) {
        validator.validate(command);

        if (moduleJpaRepository.findByTenantIdAndModuleId(
                command.tenantId(),
                command.moduleId()
        ).isEmpty()) {
            throw new InternalNotFoundException(
                    "Module not found: " + command.moduleId()
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

        var slug = toSlug(command.title());

        if (runbookJpaRepository.existsByTenantIdAndSlug(
                command.tenantId(),
                slug
        )) {
            throw new InternalValidationException(
                    "Runbook already exists: " + slug
            );
        }

        var runbook = Runbook.create(
                RunbookId.newId(),
                TenantId.of(command.tenantId()),
                command.title(),
                slug,
                command.summary(),
                ModuleId.of(command.moduleId()),
                InternalDocumentId.of(command.documentId()),
                CreatedAt.now(),
                UpdatedAt.now()
        );

        var saved = runbookJpaRepository.save(
                RunbookPersistenceMapper.toEntity(runbook)
        );

        var created = RunbookPersistenceMapper.toDomain(saved);

        return new CreateRunbookResult(created);
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