package com.sparrowx.internal.features.team.getteam;

import buildingblocks.core.queries.QueryHandler;
import com.sparrowx.internal.data.postgres.mappers.TeamPersistenceMapper;
import com.sparrowx.internal.data.postgres.repositories.TeamJpaRepository;
import com.sparrowx.internal.exceptions.InternalNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class GetTeamQueryHandler
        implements QueryHandler<GetTeamQuery, GetTeamResult> {

    private final TeamJpaRepository teamJpaRepository;
    private final GetTeamQueryValidator validator;

    public GetTeamQueryHandler(
            TeamJpaRepository teamJpaRepository,
            GetTeamQueryValidator validator
    ) {
        this.teamJpaRepository = teamJpaRepository;
        this.validator = validator;
    }

    @Override
    public GetTeamResult handle(GetTeamQuery query) {
        validator.validate(query);

        var team = teamJpaRepository
                .findByTenantIdAndTeamId(
                        query.tenantId(),
                        query.teamId()
                )
                .map(TeamPersistenceMapper::toDomain)
                .orElseThrow(() -> new InternalNotFoundException(
                        "Team not found: " + query.teamId()
                ));

        return new GetTeamResult(team);
    }
}