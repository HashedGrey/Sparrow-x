package com.sparrowx.agentic.components;

import com.sparrowx.agentic.mission.artifact.PreparedArtifact;
import com.sparrowx.agentic.mission.model.MissionPath;
import com.sparrowx.agentic.planning.MissionIntent;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class IntentComponent {

    private final Interpreter interpreter;

    public IntentComponent(Interpreter interpreter) {
        this.interpreter = Objects.requireNonNull(
                interpreter,
                "interpreter must not be null");
    }

    public MissionIntent interpret(IntentRequest request) {
        Objects.requireNonNull(request, "request must not be null");

        MissionIntent intent = interpreter.interpret(request);

        return Objects.requireNonNull(
                intent,
                "interpreter returned null");
    }

    @FunctionalInterface
    public interface Interpreter {
        MissionIntent interpret(IntentRequest request);
    }

    public record IntentRequest(
            String missionId,
            String query,
            List<PreparedArtifact> artifacts,
            MissionPath preferredPath,
            Set<String> allowedTools,
            Set<String> allowedSourceServices,
            List<String> requiredOutputSections,
            boolean requireCitations,
            boolean requireHumanReview,
            boolean allowExternalSources,
            Map<String, Object> attributes) {

        public IntentRequest {
            missionId = requireText(missionId, "missionId");
            query = requireText(query, "query");

            artifacts = artifacts == null
                    ? List.of()
                    : List.copyOf(artifacts);

            preferredPath = Objects.requireNonNull(
                    preferredPath,
                    "preferredPath must not be null");

            allowedTools = allowedTools == null
                    ? Set.of()
                    : Set.copyOf(allowedTools);

            allowedSourceServices =
                    allowedSourceServices == null
                            ? Set.of()
                            : Set.copyOf(allowedSourceServices);

            requiredOutputSections =
                    requiredOutputSections == null
                            ? List.of()
                            : List.copyOf(requiredOutputSections);

            attributes = attributes == null
                    ? Map.of()
                    : Map.copyOf(attributes);
        }
    }

    private static String requireText(
            String value,
            String field) {

        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    field + " must not be blank");
        }

        return value;
    }
}