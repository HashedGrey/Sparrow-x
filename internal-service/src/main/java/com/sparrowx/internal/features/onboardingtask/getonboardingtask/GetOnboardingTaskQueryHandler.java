package com.sparrowx.internal.features.onboardingtask.getonboardingtask;

import buildingblocks.core.queries.QueryHandler;
import com.sparrowx.internal.data.postgres.mappers.OnboardingTaskPersistenceMapper;
import com.sparrowx.internal.data.postgres.repositories.OnboardingTaskJpaRepository;
import com.sparrowx.internal.exceptions.InternalNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class GetOnboardingTaskQueryHandler
        implements QueryHandler<GetOnboardingTaskQuery, GetOnboardingTaskResult> {

    private final OnboardingTaskJpaRepository onboardingTaskJpaRepository;
    private final GetOnboardingTaskQueryValidator validator;

    public GetOnboardingTaskQueryHandler(
            OnboardingTaskJpaRepository onboardingTaskJpaRepository,
            GetOnboardingTaskQueryValidator validator
    ) {
        this.onboardingTaskJpaRepository = onboardingTaskJpaRepository;
        this.validator = validator;
    }

    @Override
    public GetOnboardingTaskResult handle(GetOnboardingTaskQuery query) {
        validator.validate(query);

        var task = onboardingTaskJpaRepository
                .findByTenantIdAndOnboardingTaskId(
                        query.tenantId(),
                        query.onboardingTaskId()
                )
                .map(OnboardingTaskPersistenceMapper::toDomain)
                .orElseThrow(() -> new InternalNotFoundException(
                        "Onboarding task not found: " + query.onboardingTaskId()
                ));

        return new GetOnboardingTaskResult(task);
    }
}