package com.sparrowx.internal.data.postgres.mappers;

import com.sparrowx.internal.data.postgres.entities.TeamEntity;
import com.sparrowx.internal.models.Team;
import com.sparrowx.internal.valueobjects.CreatedAt;
import com.sparrowx.internal.valueobjects.TeamId;
import com.sparrowx.internal.valueobjects.TenantId;
import com.sparrowx.internal.valueobjects.UpdatedAt;

public final class TeamPersistenceMapper {

    private TeamPersistenceMapper() {
    }

    public static TeamEntity toEntity(Team team) {
        return new TeamEntity(
                team.teamId().value(),
                team.tenantId().value(),
                team.name(),
                team.slug(),
                team.description(),
                team.createdAt().value(),
                team.updatedAt().value()
        );
    }

    public static Team toDomain(TeamEntity entity) {
        return new Team(
                TeamId.of(entity.getTeamId()),
                TenantId.of(entity.getTenantId()),
                entity.getName(),
                entity.getSlug(),
                entity.getDescription(),
                CreatedAt.of(entity.getCreatedAt()),
                UpdatedAt.of(entity.getUpdatedAt())
        );
    }
}