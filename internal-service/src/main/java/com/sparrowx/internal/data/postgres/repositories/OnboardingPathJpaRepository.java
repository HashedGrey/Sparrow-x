package com.sparrowx.internal.data.postgres.repositories;

import com.sparrowx.internal.data.postgres.entities.OnboardingPathEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OnboardingPathJpaRepository
        extends JpaRepository<OnboardingPathEntity, String> {

    Optional<OnboardingPathEntity> findByTenantIdAndOnboardingPathId(
            String tenantId,
            String onboardingPathId
    );

    Optional<OnboardingPathEntity> findByTenantIdAndSlug(
            String tenantId,
            String slug
    );

    List<OnboardingPathEntity> findByTenantIdAndTargetModuleId(
            String tenantId,
            String targetModuleId
    );

    boolean existsByTenantIdAndSlug(
            String tenantId,
            String slug
    );

    @Query("""
            select onboardingPath
            from OnboardingPathEntity onboardingPath
            where onboardingPath.tenantId = :tenantId
              and (
                    lower(onboardingPath.name) like lower(concat('%', :query, '%'))
                 or lower(onboardingPath.slug) like lower(concat('%', :query, '%'))
                 or lower(onboardingPath.description) like lower(concat('%', :query, '%'))
              )
            """)
    List<OnboardingPathEntity> searchByTenantIdAndText(
            @Param("tenantId") String tenantId,
            @Param("query") String query
    );
}