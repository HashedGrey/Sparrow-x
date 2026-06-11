package com.sparrowx.internal.features.engineer.getengineer;

import buildingblocks.core.queries.QueryHandler;
import com.sparrowx.internal.data.postgres.mappers.EngineerPersistenceMapper;
import com.sparrowx.internal.data.postgres.repositories.EngineerJpaRepository;
import com.sparrowx.internal.exceptions.InternalNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class GetEngineerQueryHandler
        implements QueryHandler<GetEngineerQuery, GetEngineerResult> {

    private final EngineerJpaRepository engineerJpaRepository;
    private final GetEngineerQueryValidator validator;

    public GetEngineerQueryHandler(
            EngineerJpaRepository engineerJpaRepository,
            GetEngineerQueryValidator validator
    ) {
        this.engineerJpaRepository = engineerJpaRepository;
        this.validator = validator;
    }

    @Override
    public GetEngineerResult handle(GetEngineerQuery query) {
        validator.validate(query);

        var engineer = engineerJpaRepository
                .findByTenantIdAndEngineerId(
                        query.tenantId(),
                        query.engineerId()
                )
                .map(EngineerPersistenceMapper::toDomain)
                .orElseThrow(() -> new InternalNotFoundException(
                        "Engineer not found: " + query.engineerId()
                ));

        return new GetEngineerResult(engineer);
    }
}