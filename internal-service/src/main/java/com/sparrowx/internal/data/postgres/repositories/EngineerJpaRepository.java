package com.sparrowx.internal.data.postgres.repositories;

import com.sparrowx.internal.data.postgres.entities.EngineerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
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

    @Query("""
            select engineer
            from EngineerEntity engineer
            where engineer.tenantId = :tenantId
              and (
                    lower(engineer.fullName) like lower(concat('%', :query, '%'))
                 or lower(engineer.email) like lower(concat('%', :query, '%'))
              )
            """)
    List<EngineerEntity> searchByTenantIdAndText(
            @Param("tenantId") String tenantId,
            @Param("query") String query
    );
}