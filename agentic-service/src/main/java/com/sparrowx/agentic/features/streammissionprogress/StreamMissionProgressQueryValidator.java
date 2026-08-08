package com.sparrowx.agentic.features.streammissionprogress;

import com.sparrowx.agentic.exceptions.MissionValidationException;
import org.springframework.stereotype.Component;

@Component
public final class StreamMissionProgressQueryValidator {

    private static final int MAX_ID_LENGTH = 256;
    private static final int MAX_RESUME_TOKEN_LENGTH = 1_024;

    public void validate(StreamMissionProgressQuery query) {
        if (query == null) {
            throw invalid("Stream mission progress query is required.");
        }

        requireId(query.requestId(), "request ID");
        requireId(query.tenantId(), "tenant ID");
        requireId(query.userId(), "user ID");
        requireId(query.missionId(), "mission ID");

        if (query.resumeToken().length() > MAX_RESUME_TOKEN_LENGTH) {
            throw invalid(
                    "Resume token must not exceed "
                            + MAX_RESUME_TOKEN_LENGTH + " characters."
            );
        }
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