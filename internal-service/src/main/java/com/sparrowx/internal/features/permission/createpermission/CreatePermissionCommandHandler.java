package com.sparrowx.internal.features.permission.createpermission;

import buildingblocks.core.commands.CommandHandler;
import com.sparrowx.internal.data.postgres.mappers.PermissionPersistenceMapper;
import com.sparrowx.internal.data.postgres.repositories.PermissionJpaRepository;
import com.sparrowx.internal.exceptions.InternalValidationException;
import com.sparrowx.internal.models.Permission;
import com.sparrowx.internal.valueobjects.CreatedAt;
import com.sparrowx.internal.valueobjects.PermissionId;
import com.sparrowx.internal.valueobjects.TenantId;
import com.sparrowx.internal.valueobjects.UpdatedAt;
import org.springframework.stereotype.Component;

@Component
public class CreatePermissionCommandHandler
        implements CommandHandler<CreatePermissionCommand, CreatePermissionResult> {

    private final PermissionJpaRepository permissionJpaRepository;
    private final CreatePermissionCommandValidator validator;

    public CreatePermissionCommandHandler(
            PermissionJpaRepository permissionJpaRepository,
            CreatePermissionCommandValidator validator
    ) {
        this.permissionJpaRepository = permissionJpaRepository;
        this.validator = validator;
    }

    @Override
    public CreatePermissionResult handle(CreatePermissionCommand command) {
        validator.validate(command);

        var name = normalizeName(command.name());

        if (permissionJpaRepository.existsByTenantIdAndName(
                command.tenantId(),
                name
        )) {
            throw new InternalValidationException(
                    "Permission already exists: " + name
            );
        }

        var permission = Permission.create(
                PermissionId.newId(),
                TenantId.of(command.tenantId()),
                name,
                command.description(),
                CreatedAt.now(),
                UpdatedAt.now()
        );

        var saved = permissionJpaRepository.save(
                PermissionPersistenceMapper.toEntity(permission)
        );

        var created = PermissionPersistenceMapper.toDomain(saved);

        return new CreatePermissionResult(created);
    }

    private String normalizeName(String value) {
        return value == null
                ? ""
                : value.trim()
                .toUpperCase()
                .replaceAll("[^A-Z0-9:_-]+", "_")
                .replaceAll("(^_|_$)", "");
    }
}