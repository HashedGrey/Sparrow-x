package com.sparrowx.internal.features.permission.getpermission;

import buildingblocks.core.queries.QueryHandler;
import com.sparrowx.internal.data.postgres.mappers.PermissionPersistenceMapper;
import com.sparrowx.internal.data.postgres.repositories.PermissionJpaRepository;
import com.sparrowx.internal.exceptions.InternalNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class GetPermissionQueryHandler
        implements QueryHandler<GetPermissionQuery, GetPermissionResult> {

    private final PermissionJpaRepository permissionJpaRepository;
    private final GetPermissionQueryValidator validator;

    public GetPermissionQueryHandler(
            PermissionJpaRepository permissionJpaRepository,
            GetPermissionQueryValidator validator
    ) {
        this.permissionJpaRepository = permissionJpaRepository;
        this.validator = validator;
    }

    @Override
    public GetPermissionResult handle(GetPermissionQuery query) {
        validator.validate(query);

        var permission = permissionJpaRepository
                .findByTenantIdAndPermissionId(
                        query.tenantId(),
                        query.permissionId()
                )
                .map(PermissionPersistenceMapper::toDomain)
                .orElseThrow(() -> new InternalNotFoundException(
                        "Permission not found: " + query.permissionId()
                ));

        return new GetPermissionResult(permission);
    }
}