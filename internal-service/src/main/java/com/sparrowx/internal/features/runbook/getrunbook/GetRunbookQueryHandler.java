package com.sparrowx.internal.features.runbook.getrunbook;

import buildingblocks.core.queries.QueryHandler;
import com.sparrowx.internal.data.postgres.mappers.RunbookPersistenceMapper;
import com.sparrowx.internal.data.postgres.repositories.RunbookJpaRepository;
import com.sparrowx.internal.exceptions.InternalNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class GetRunbookQueryHandler
        implements QueryHandler<GetRunbookQuery, GetRunbookResult> {

    private final RunbookJpaRepository runbookJpaRepository;
    private final GetRunbookQueryValidator validator;

    public GetRunbookQueryHandler(
            RunbookJpaRepository runbookJpaRepository,
            GetRunbookQueryValidator validator
    ) {
        this.runbookJpaRepository = runbookJpaRepository;
        this.validator = validator;
    }

    @Override
    public GetRunbookResult handle(GetRunbookQuery query) {
        validator.validate(query);

        var runbook = runbookJpaRepository
                .findByTenantIdAndRunbookId(
                        query.tenantId(),
                        query.runbookId()
                )
                .map(RunbookPersistenceMapper::toDomain)
                .orElseThrow(() -> new InternalNotFoundException(
                        "Runbook not found: " + query.runbookId()
                ));

        return new GetRunbookResult(runbook);
    }
}