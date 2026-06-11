package com.sparrowx.internal.features.repository.getrepository;

import buildingblocks.core.queries.QueryHandler;
import com.sparrowx.internal.data.postgres.mappers.RepositoryPersistenceMapper;
import com.sparrowx.internal.data.postgres.repositories.RepositoryJpaRepository;
import com.sparrowx.internal.exceptions.InternalNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class GetRepositoryQueryHandler
        implements QueryHandler<GetRepositoryQuery, GetRepositoryResult> {

    private final RepositoryJpaRepository repositoryJpaRepository;
    private final GetRepositoryQueryValidator validator;

    public GetRepositoryQueryHandler(
            RepositoryJpaRepository repositoryJpaRepository,
            GetRepositoryQueryValidator validator
    ) {
        this.repositoryJpaRepository = repositoryJpaRepository;
        this.validator = validator;
    }

    @Override
    public GetRepositoryResult handle(GetRepositoryQuery query) {
        validator.validate(query);

        var repository = repositoryJpaRepository
                .findByTenantIdAndRepositoryId(
                        query.tenantId(),
                        query.repositoryId()
                )
                .map(RepositoryPersistenceMapper::toDomain)
                .orElseThrow(() -> new InternalNotFoundException(
                        "Repository not found: " + query.repositoryId()
                ));

        return new GetRepositoryResult(repository);
    }
}