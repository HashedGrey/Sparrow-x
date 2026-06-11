package com.sparrowx.internal.features.module.getmodule;

import buildingblocks.core.queries.QueryHandler;
import com.sparrowx.internal.data.postgres.mappers.ModulePersistenceMapper;
import com.sparrowx.internal.data.postgres.repositories.ModuleJpaRepository;
import com.sparrowx.internal.exceptions.InternalNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class GetModuleQueryHandler
        implements QueryHandler<GetModuleQuery, GetModuleResult> {

    private final ModuleJpaRepository moduleJpaRepository;
    private final GetModuleQueryValidator validator;

    public GetModuleQueryHandler(
            ModuleJpaRepository moduleJpaRepository,
            GetModuleQueryValidator validator
    ) {
        this.moduleJpaRepository = moduleJpaRepository;
        this.validator = validator;
    }

    @Override
    public GetModuleResult handle(GetModuleQuery query) {
        validator.validate(query);

        var module = moduleJpaRepository
                .findByTenantIdAndModuleId(
                        query.tenantId(),
                        query.moduleId()
                )
                .map(ModulePersistenceMapper::toDomain)
                .orElseThrow(() -> new InternalNotFoundException(
                        "Module not found: " + query.moduleId()
                ));

        return new GetModuleResult(module);
    }
}