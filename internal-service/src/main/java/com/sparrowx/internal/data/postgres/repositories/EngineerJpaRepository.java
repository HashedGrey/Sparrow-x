package com.sparrowx.internal.data.postgres.repositories;

import com.sparrowx.internal.data.postgres.entities.EngineerEntity;
import org.springframework.data.jpa.repository.JpaRepository;import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EngineerJpaRepository extends JpaRepository<EngineerEntity, String> {

    Optional<EngineerEntity> findByTenantIdAndEngineerId(
            String tenantId,
            String engineerId
    );

    Optional<EngineerEntity> findByTenantIdAndEmail(
            String tenantId,
            String email
    );

    boolean existsByTenantIdAndEmail(
            String tenantId,
            String email
    );
}