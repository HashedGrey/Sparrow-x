package com.sparrowx.internal.features.getengineeronboardingprogress;

import buildingblocks.core.queries.QueryHandler;
import com.sparrowx.internal.data.postgres.entities.EngineerOnboardingTaskProgressEntity;
import com.sparrowx.internal.data.postgres.repositories.EngineerOnboardingAssignmentJpaRepository;
import com.sparrowx.internal.data.postgres.repositories.EngineerOnboardingTaskProgressJpaRepository;
import com.sparrowx.internal.data.postgres.repositories.OnboardingTaskJpaRepository;
import com.sparrowx.internal.exceptions.InternalNotFoundException;
import com.sparrowx.internal.models.EngineerOnboardingProgress;
import com.sparrowx.internal.valueobjects.EngineerId;
import com.sparrowx.internal.valueobjects.EngineerOnboardingAssignmentId;
import com.sparrowx.internal.valueobjects.OnboardingAssignmentStatus;
import com.sparrowx.internal.valueobjects.OnboardingPathId;
import com.sparrowx.internal.valueobjects.OnboardingTaskId;
import com.sparrowx.internal.valueobjects.OnboardingTaskProgressStatus;
import org.springframework.stereotype.Component;

import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class GetEngineerOnboardingProgressQueryHandler
        implements QueryHandler<GetEngineerOnboardingProgressQuery, GetEngineerOnboardingProgressResult> {

    private final EngineerOnboardingAssignmentJpaRepository assignmentJpaRepository;
    private final OnboardingTaskJpaRepository onboardingTaskJpaRepository;
    private final EngineerOnboardingTaskProgressJpaRepository taskProgressJpaRepository;
    private final GetEngineerOnboardingProgressQueryValidator validator;

    public GetEngineerOnboardingProgressQueryHandler(
            EngineerOnboardingAssignmentJpaRepository assignmentJpaRepository,
            OnboardingTaskJpaRepository onboardingTaskJpaRepository,
            EngineerOnboardingTaskProgressJpaRepository taskProgressJpaRepository,
            GetEngineerOnboardingProgressQueryValidator validator
    ) {
        this.assignmentJpaRepository = assignmentJpaRepository;
        this.onboardingTaskJpaRepository = onboardingTaskJpaRepository;
        this.taskProgressJpaRepository = taskProgressJpaRepository;
        this.validator = validator;
    }

    @Override
    public GetEngineerOnboardingProgressResult handle(
            GetEngineerOnboardingProgressQuery query
    ) {
        validator.validate(query);

        var assignment = assignmentJpaRepository
                .findByTenantIdAndAssignmentId(
                        query.tenantId(),
                        query.assignmentId()
                )
                .orElseThrow(() -> new InternalNotFoundException(
                        "Onboarding assignment not found: " + query.assignmentId()
                ));

        var tasks = onboardingTaskJpaRepository
                .findByTenantIdAndOnboardingPathIdOrderBySortOrderAsc(
                        query.tenantId(),
                        assignment.getOnboardingPathId()
                );

        var progressByTaskId = taskProgressJpaRepository
                .findByTenantIdAndAssignmentId(
                        query.tenantId(),
                        query.assignmentId()
                )
                .stream()
                .collect(Collectors.toMap(
                        EngineerOnboardingTaskProgressEntity::getOnboardingTaskId,
                        Function.identity()
                ));

        var taskViews = tasks.stream()
                .map(task -> {
                    var progress = progressByTaskId.get(task.getOnboardingTaskId());

                    return new OnboardingProgressTaskView(
                            OnboardingTaskId.of(task.getOnboardingTaskId()),
                            task.getTitle(),
                            task.getDescription(),
                            progress == null
                                    ? OnboardingTaskProgressStatus.NOT_STARTED
                                    : OnboardingTaskProgressStatus.from(progress.getStatus()),
                            task.getSortOrder(),
                            progress == null ? null : progress.getCompletedAt()
                    );
                })
                .toList();

        var completedTasks = (int) taskViews.stream()
                .filter(task -> task.status() == OnboardingTaskProgressStatus.COMPLETED)
                .count();

        var progress = EngineerOnboardingProgress.of(
                EngineerOnboardingAssignmentId.of(assignment.getAssignmentId()),
                EngineerId.of(assignment.getEngineerId()),
                OnboardingPathId.of(assignment.getOnboardingPathId()),
                OnboardingAssignmentStatus.from(assignment.getStatus()),
                taskViews.size(),
                completedTasks,
                taskViews
        );

        return new GetEngineerOnboardingProgressResult(progress);
    }
}