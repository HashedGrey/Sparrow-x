package com.sparrowx.internal.grpc.policies;

import com.sparrowx.internal.exceptions.InternalPermissionDeniedException;
import com.sparrowx.internal.models.EngineerOnboardingAssignment;
import org.springframework.stereotype.Component;

@Component
public class OnboardingVisibilityPolicy {

    public void assertCanViewAssignment(
            String actorId,
            EngineerOnboardingAssignment assignment
    ) {
        if (actorId == null || actorId.isBlank()) {
            throw new InternalPermissionDeniedException("actorId is required");
        }

        if (assignment == null) {
            throw new InternalPermissionDeniedException("assignment is required");
        }

        /*
         * Current skeleton:
         * - any authenticated tenant actor can view onboarding progress
         * - later this can restrict to self, admin, manager, or team owner
         */
    }

    public void assertCanMutateAssignment(
            String actorId,
            EngineerOnboardingAssignment assignment
    ) {
        if (actorId == null || actorId.isBlank()) {
            throw new InternalPermissionDeniedException("actorId is required");
        }

        if (assignment == null) {
            throw new InternalPermissionDeniedException("assignment is required");
        }

        /*
         * Current skeleton:
         * - mutation is allowed after tenant/actor gRPC policy passes
         * - later this can enforce admin/mentor/owner permissions
         */
    }
}