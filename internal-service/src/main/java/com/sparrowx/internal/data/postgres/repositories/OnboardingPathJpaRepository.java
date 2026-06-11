package com.sparrowx.internal.data.postgres.repositories;

import com.sparrowx.internal.data.postgres.entities.OnboardingPathEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

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
}