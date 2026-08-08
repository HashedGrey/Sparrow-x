package com.sparrowx.agentic.features.getmissionresult;

import com.sparrowx.agentic.exceptions.MissionValidationException;
import org.springframework.stereotype.Component;

@Component
public final class GetMissionResultQueryValidator {

    private static final int MAX_ID_LENGTH = 256;

    public void validate(GetMissionResultQuery query) {
        if (query == null) {
            throw invalid("Get mission result query is required.");
        }

        requireId(query.requestId(), "request ID");
        requireId(query.tenantId(), "tenant ID");
        requireId(query.userId(), "user ID");
        requireId(query.missionId(), "mission ID");
    }

    private static void requireId(String value, String field) {
        if (value == null || value.isBlank()) {
            throw invalid(field + " is required.");
        }

        if (value.length() > MAX_ID_LENGTH) {
            throw invalid(
                    field + " must not exceed "
                            + MAX_ID_LENGTH + " characters."
            );
        }
    }

    private static MissionValidationException invalid(String message) {
        return new MissionValidationException(message);
    }
}