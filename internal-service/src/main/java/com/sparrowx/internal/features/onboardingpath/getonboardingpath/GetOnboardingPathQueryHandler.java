package com.sparrowx.internal.features.onboardingpath.getonboardingpath;

import buildingblocks.core.queries.QueryHandler;
import com.sparrowx.internal.data.postgres.mappers.OnboardingPathPersistenceMapper;
import com.sparrowx.internal.data.postgres.mappers.OnboardingTaskPersistenceMapper;
import com.sparrowx.internal.data.postgres.repositories.OnboardingPathJpaRepository;
import com.sparrowx.internal.data.postgres.repositories.OnboardingTaskJpaRepository;
import com.sparrowx.internal.exceptions.InternalNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class GetOnboardingPathQueryHandler
        implements QueryHandler<GetOnboardingPathQuery, GetOnboardingPathResult> {

    private final OnboardingPathJpaRepository onboardingPathJpaRepository;
    private final OnboardingTaskJpaRepository onboardingTaskJpaRepository;
    private final GetOnboardingPathQueryValidator validator;

    public GetOnboardingPathQueryHandler(
            OnboardingPathJpaRepository onboardingPathJpaRepository,
            OnboardingTaskJpaRepository onboardingTaskJpaRepository,
            GetOnboardingPathQueryValidator validator
    ) {
        this.onboardingPathJpaRepository = onboardingPathJpaRepository;
        this.onboardingTaskJpaRepository = onboardingTaskJpaRepository;
        this.validator = validator;
    }

    @Override
    @Transactional(readOnly = true)
    public GetOnboardingPathResult handle(GetOnboardingPathQuery query) {
        validator.validate(query);

        var path = onboardingPathJpaRepository
                .findByTenantIdAndOnboardingPathId(
                        query.tenantId(),
                        query.onboardingPathId()
                )
                .map(OnboardingPathPersistenceMapper::toDomain)
                .orElseThrow(() -> new InternalNotFoundException(
                        "Onboarding path not found: " + query.onboardingPathId()
                ));

        var tasks = onboardingTaskJpaRepository
                .findByTenantIdAndOnboardingPathIdOrderBySortOrderAsc(
                        query.tenantId(),
                        query.onboardingPathId()
                )
                .stream()
                .map(OnboardingTaskPersistenceMapper::toDomain)
                .toList();

        return new GetOnboardingPathResult(path, tasks);
    }
}