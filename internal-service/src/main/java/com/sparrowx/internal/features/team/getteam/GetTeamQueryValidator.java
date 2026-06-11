package com.sparrowx.internal.features.team.getteam;

import com.sparrowx.internal.exceptions.InternalValidationException;
import org.springframework.stereotype.Component;

@Component
public class GetTeamQueryValidator {

    public void validate(GetTeamQuery query) {
        if (query == null) {
            throw new InternalValidationException("GetTeamQuery is required");
        }

        if (query.tenantId() == null || query.tenantId().isBlank()) {
            throw new InternalValidationException("tenantId is required");
        }

        if (query.actorId() == null || query.actorId().isBlank()) {
            throw new InternalValidationException("actorId is required");
        }

        if (query.requestId() == null || query.requestId().isBlank()) {
            throw new InternalValidationException("requestId is required");
        }

        if (query.teamId() == null || query.teamId().isBlank()) {
            throw new InternalValidationException("teamId is required");
        }
    }
}