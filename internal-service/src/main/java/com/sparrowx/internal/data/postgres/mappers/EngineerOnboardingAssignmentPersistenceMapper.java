package com.sparrowx.internal.data.postgres.mappers;

import com.sparrowx.internal.data.postgres.entities.EngineerOnboardingAssignmentEntity;
import com.sparrowx.internal.models.EngineerOnboardingAssignment;
import com.sparrowx.internal.valueobjects.EngineerId;
import com.sparrowx.internal.valueobjects.EngineerOnboardingAssignmentId;
import com.sparrowx.internal.valueobjects.OnboardingAssignmentStatus;
import com.sparrowx.internal.valueobjects.OnboardingPathId;
import com.sparrowx.internal.valueobjects.TenantId;

public final class EngineerOnboardingAssignmentPersistenceMapper {

    private EngineerOnboardingAssignmentPersistenceMapper() {
    }

    public static EngineerOnboardingAssignmentEntity toEntity(
            EngineerOnboardingAssignment assignment
    ) {
        return new EngineerOnboardingAssignmentEntity(
                assignment.assignmentId().value(),
                assignment.tenantId().value(),
                assignment.engineerId().value(),
                assignment.onboardingPathId().value(),
                assignment.status().name(),
                assignment.assignedAt(),
                assignment.startedAt(),
                assignment.completedAt()
        );
    }

    public static EngineerOnboardingAssignment toDomain(
            EngineerOnboardingAssignmentEntity entity
    ) {
        return new EngineerOnboardingAssignment(
                EngineerOnboardingAssignmentId.of(entity.getAssignmentId()),
                TenantId.of(entity.getTenantId()),
                EngineerId.of(entity.getEngineerId()),
                OnboardingPathId.of(entity.getOnboardingPathId()),
                OnboardingAssignmentStatus.from(entity.getStatus()),
                entity.getAssignedAt(),
                entity.getStartedAt(),
                entity.getCompletedAt()
        );
    }
}