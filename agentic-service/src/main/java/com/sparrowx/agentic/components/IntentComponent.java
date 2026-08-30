package com.sparrowx.agentic.components;

import com.embabel.agent.api.common.OperationContext;
import com.sparrowx.agentic.mission.artifact.PreparedArtifact;
import com.sparrowx.agentic.mission.model.MissionPath;
import com.sparrowx.agentic.planning.MissionIntent;
import com.sparrowx.agentic.prompts.PromptPack;
import com.sparrowx.agentic.prompts.StructuredOutputSchemas;
import com.sparrowx.agentic.validation.StructuredOutputValidator;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class IntentComponent {

    private final PromptPack promptPack;
    private final StructuredOutputSchemas schemas;
    private final StructuredOutputValidator validator;

    public IntentComponent(
            PromptPack promptPack,
            StructuredOutputSchemas schemas,
            StructuredOutputValidator validator
    ) {
        this.promptPack = Objects.requireNonNull(promptPack);
        this.schemas = Objects.requireNonNull(schemas);
        this.validator = Objects.requireNonNull(validator);
    }

    public MissionIntent interpret(
            IntentRequest request,
            OperationContext context
    ) {
        Objects.requireNonNull(request, "request must not be null");
        Objects.requireNonNull(context, "context must not be null");

        Map<String, Object> variables = new LinkedHashMap<>();
        variables.put("missionId", request.missionId());
        variables.put("query", request.query());
        variables.put("artifacts", request.artifacts());
        variables.put("preferredPath", request.preferredPath().name());
        variables.put("allowedTools", request.allowedTools());
        variables.put(
                "allowedSourceServices",
                request.allowedSourceServices()
        );
        variables.put(
                "requiredOutputSections",
                request.requiredOutputSections()
        );
        variables.put(
                "requireCitations",
                request.requireCitations()
        );
        variables.put(
                "requireHumanReview",
                request.requireHumanReview()
        );
        variables.put(
                "allowExternalSources",
                request.allowExternalSources()
        );
        variables.put("attributes", request.attributes());
        variables.put(
                "outputSchema",
                schemas.schema(StructuredOutputSchemas.MISSION_INTENT)
        );

        PromptPack.RenderedPrompt rendered =
                promptPack.render(
                        PromptPack.INTENT_PROMPT,
                        variables
                );

        String prompt =
                rendered.systemPrompt()
                        + "\n\n"
                        + rendered.userPrompt();

        IntentProjection output =
                context.ai()
                        .withDefaultLlm()
                        .createObject(
                                prompt,
                                IntentProjection.class
                        );

        validateProjection(output);

        return new MissionIntent(
                request.missionId(),
                output.objective(),
                output.selectedPath(),
                setOf(output.targetEntities()),
                setOf(output.topics()),
                listOf(output.requiredOutputSections()),
                output.requiresDocumentEvidence(),
                output.requiresInternalContext(),
                output.requiresHumanReview(),
                output.requiresCitations(),
                output.requiresVerification(),
                output.allowsExternalSources(),
                setOf(output.allowedTools()),
                setOf(output.allowedSourceServices()),
                mapOf(output.attributes())
        );
    }

    private void validateProjection(IntentProjection output) {
        Objects.requireNonNull(
                output,
                "intent projection must not be null"
        );

        Map<String, Object> value = new LinkedHashMap<>();
        value.put("objective", output.objective());
        value.put(
                "selectedPath",
                output.selectedPath() == null
                        ? null
                        : output.selectedPath().name()
        );
        value.put("targetEntities", listOf(output.targetEntities()));
        value.put("topics", listOf(output.topics()));
        value.put(
                "requiredOutputSections",
                listOf(output.requiredOutputSections())
        );
        value.put(
                "requiresDocumentEvidence",
                output.requiresDocumentEvidence()
        );
        value.put(
                "requiresInternalContext",
                output.requiresInternalContext()
        );
        value.put(
                "requiresHumanReview",
                output.requiresHumanReview()
        );
        value.put(
                "requiresCitations",
                output.requiresCitations()
        );
        value.put(
                "requiresVerification",
                output.requiresVerification()
        );
        value.put(
                "allowsExternalSources",
                output.allowsExternalSources()
        );
        value.put("allowedTools", listOf(output.allowedTools()));
        value.put(
                "allowedSourceServices",
                listOf(output.allowedSourceServices())
        );
        value.put("attributes", mapOf(output.attributes()));

        validator.validateIntent(value);
    }

    public record IntentProjection(
            String objective,
            MissionPath selectedPath,
            List<String> targetEntities,
            List<String> topics,
            List<String> requiredOutputSections,
            boolean requiresDocumentEvidence,
            boolean requiresInternalContext,
            boolean requiresHumanReview,
            boolean requiresCitations,
            boolean requiresVerification,
            boolean allowsExternalSources,
            List<String> allowedTools,
            List<String> allowedSourceServices,
            Map<String, Object> attributes
    ) {
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
            Map<String, Object> attributes
    ) {
        public IntentRequest {
            missionId = requireText(missionId, "missionId");
            query = requireText(query, "query");

            artifacts = artifacts == null
                    ? List.of()
                    : List.copyOf(artifacts);

            preferredPath = Objects.requireNonNull(
                    preferredPath,
                    "preferredPath must not be null"
            );

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

    private static Set<String> setOf(List<String> values) {
        return values == null ? Set.of() : Set.copyOf(values);
    }

    private static List<String> listOf(List<String> values) {
        return values == null ? List.of() : List.copyOf(values);
    }

    private static Map<String, Object> mapOf(
            Map<String, Object> values
    ) {
        return values == null ? Map.of() : Map.copyOf(values);
    }

    private static String requireText(
            String value,
            String field
    ) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    field + " must not be blank"
            );
        }

        return value;
    }
}