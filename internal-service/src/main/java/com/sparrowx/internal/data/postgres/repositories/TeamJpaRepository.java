package com.sparrowx.internal.data.postgres.repositories;

import com.sparrowx.internal.data.postgres.entities.TeamEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TeamJpaRepository extends JpaRepository<TeamEntity, String> {

    Optional<TeamEntity> findByTenantIdAndTeamId(
            String tenantId,
            String teamId
    );

    Optional<TeamEntity> findByTenantIdAndSlug(
            String tenantId,
            String slug
    );

    boolean existsByTenantIdAndSlug(
            String tenantId,
            String slug
    );
}