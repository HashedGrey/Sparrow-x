package com.sparrowx.internal.features.document.createdocument;

import buildingblocks.core.commands.CommandHandler;
import com.sparrowx.internal.data.postgres.mappers.InternalDocumentPersistenceMapper;
import com.sparrowx.internal.data.postgres.repositories.InternalDocumentJpaRepository;
import com.sparrowx.internal.data.postgres.repositories.ModuleJpaRepository;
import com.sparrowx.internal.data.postgres.repositories.RepositoryJpaRepository;
import com.sparrowx.internal.exceptions.InternalNotFoundException;
import com.sparrowx.internal.exceptions.InternalValidationException;
import com.sparrowx.internal.models.InternalDocument;
import com.sparrowx.internal.valueobjects.CreatedAt;
import com.sparrowx.internal.valueobjects.InternalDocumentId;
import com.sparrowx.internal.valueobjects.ModuleId;
import com.sparrowx.internal.valueobjects.RepositoryId;
import com.sparrowx.internal.valueobjects.TenantId;
import com.sparrowx.internal.valueobjects.UpdatedAt;
import org.springframework.stereotype.Component;

@Component
public class CreateDocumentCommandHandler
        implements CommandHandler<CreateDocumentCommand, CreateDocumentResult> {

    private final InternalDocumentJpaRepository internalDocumentJpaRepository;
    private final ModuleJpaRepository moduleJpaRepository;
    private final RepositoryJpaRepository repositoryJpaRepository;
    private final CreateDocumentCommandValidator validator;

    public CreateDocumentCommandHandler(
            InternalDocumentJpaRepository internalDocumentJpaRepository,
            ModuleJpaRepository moduleJpaRepository,
            RepositoryJpaRepository repositoryJpaRepository,
            CreateDocumentCommandValidator validator
    ) {
        this.internalDocumentJpaRepository = internalDocumentJpaRepository;
        this.moduleJpaRepository = moduleJpaRepository;
        this.repositoryJpaRepository = repositoryJpaRepository;
        this.validator = validator;
    }

    @Override
    public CreateDocumentResult handle(CreateDocumentCommand command) {
        validator.validate(command);

        if (moduleJpaRepository.findByTenantIdAndModuleId(
                command.tenantId(),
                command.moduleId()
        ).isEmpty()) {
            throw new InternalNotFoundException(
                    "Module not found: " + command.moduleId()
            );
        }

        if (repositoryJpaRepository.findByTenantIdAndRepositoryId(
                command.tenantId(),
                command.repositoryId()
        ).isEmpty()) {
            throw new InternalNotFoundException(
                    "Repository not found: " + command.repositoryId()
            );
        }

        var slug = toSlug(command.title());

        if (internalDocumentJpaRepository.existsByTenantIdAndSlug(
                command.tenantId(),
                slug
        )) {
            throw new InternalValidationException(
                    "Internal document already exists: " + slug
            );
        }

        var document = InternalDocument.create(
                InternalDocumentId.newId(),
                TenantId.of(command.tenantId()),
                command.title(),
                slug,
                command.summary(),
                ModuleId.of(command.moduleId()),
                RepositoryId.of(command.repositoryId()),
                command.externalRef(),
                CreatedAt.now(),
                UpdatedAt.now()
        );

        var saved = internalDocumentJpaRepository.save(
                InternalDocumentPersistenceMapper.toEntity(document)
        );

        var created = InternalDocumentPersistenceMapper.toDomain(saved);

        return new CreateDocumentResult(created);
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