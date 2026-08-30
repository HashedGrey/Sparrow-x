package com.sparrowx.agentic.validation;

import com.sparrowx.agentic.mission.artifact.InputArtifact;
import com.sparrowx.agentic.mission.model.MissionBudget;
import com.sparrowx.agentic.mission.model.MissionConstraints;
import com.sparrowx.agentic.mission.model.MissionContext;
import com.sparrowx.agentic.mission.model.MissionRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

@Component
public final class MissionRequestValidator {


    private static final Pattern POLICY_IDENTIFIER = Pattern.compile(
            "[A-Za-z0-9][A-Za-z0-9._:/-]{0,127}"
    );

    private final ArtifactValidator artifactValidator;
    private final Limits limits;

    @Autowired
    public MissionRequestValidator(
            ArtifactValidator artifactValidator
    ) {
        this(artifactValidator, Limits.defaults());
    }

    private MissionRequestValidator(
            ArtifactValidator artifactValidator,
            Limits limits
    ) {
        this.artifactValidator = Objects.requireNonNull(
                artifactValidator,
                "artifactValidator must not be null"
        );
        this.limits = Objects.requireNonNull(
                limits,
                "limits must not be null"
        );
    }

    public static MissionRequestValidator configured(
            ArtifactValidator artifactValidator,
            Limits limits
    ) {
        return new MissionRequestValidator(artifactValidator, limits);
    }

    /**
     * Preserves the signature consumed by MissionSubmissionService.
     */
    public void validate(MissionRequest request) {
        if (request == null) {
            throw violation(
                    "REQUIRED",
                    "request must not be null"
            );
        }

        validateContext(request.context());
        validateQuery(request.query());
        validateArtifacts(request.inputArtifacts());
        validateConstraints(request.constraints());
        validateBudget(request.budget());
    }

    private void validateContext(MissionContext context) {
        if (context == null) {
            throw violation(
                    "CONTEXT_REQUIRED",
                    "context must not be null"
            );
        }

        requireText(
                context.requestId(),
                "context.request_id",
                256
        );
        requireText(
                context.tenantId(),
                "context.tenant_id",
                256
        );
        requireText(
                context.userId(),
                "context.user_id",
                256
        );

        validateOptionalText(
                context.username(),
                "context.username",
                512
        );
        validateOptionalText(
                context.projectId(),
                "context.project_id",
                256
        );
        validateOptionalText(
                context.teamId(),
                "context.team_id",
                256
        );
        validateOptionalText(
                context.traceId(),
                "context.trace_id",
                256
        );
        validateOptionalText(
                context.callerService(),
                "context.caller_service",
                256
        );
        validateOptionalText(
                context.sessionId(),
                "context.session_id",
                256
        );
        validateOptionalText(
                context.conversationId(),
                "context.conversation_id",
                256
        );
        validateOptionalText(
                context.clientChannel(),
                "context.client_channel",
                128
        );

        validateStringMap(
                context.metadata(),
                "context.metadata",
                limits.maxContextMetadataEntries(),
                limits.maxMetadataValueLength()
        );
    }

    private void validateQuery(String query) {
        requireText(
                query,
                "query",
                limits.maxQueryLength()
        );
    }

    private void validateArtifacts(
            List<InputArtifact> artifacts
    ) {
        if (artifacts == null) {
            throw violation(
                    "ARTIFACTS_REQUIRED",
                    "input_artifacts must not be null"
            );
        }

        if (artifacts.size() > limits.maxArtifacts()) {
            throw violation(
                    "TOO_MANY_ARTIFACTS",
                    "input_artifacts exceeds "
                            + limits.maxArtifacts()
            );
        }

        Set<String> artifactIds = new HashSet<>();
        for (InputArtifact artifact : artifacts) {
            if (artifact == null) {
                throw violation(
                        "NULL_ARTIFACT",
                        "input_artifacts must not contain null"
                );
            }

            artifactValidator.validate(artifact);

            if (!artifactIds.add(artifact.artifactId())) {
                throw violation(
                        "DUPLICATE_ARTIFACT_ID",
                        "artifact_id must be unique: "
                                + artifact.artifactId()
                );
            }
        }
    }

    private void validateConstraints(
            MissionConstraints constraints
    ) {
        if (constraints == null) {
            throw violation(
                    "CONSTRAINTS_REQUIRED",
                    "constraints must not be null"
            );
        }

        if (constraints.preferredPath() == null) {
            throw violation(
                    "PREFERRED_PATH_REQUIRED",
                    "constraints.preferred_path must not be null"
            );
        }

        validateIdentifierList(
                constraints.allowedTools(),
                "constraints.allowed_tools",
                limits.maxAllowlistEntries()
        );
        validateIdentifierList(
                constraints.allowedSourceServices(),
                "constraints.allowed_source_services",
                limits.maxAllowlistEntries()
        );
        validateSectionList(
                constraints.requiredOutputSections()
        );

        Duration maxRuntime = constraints.maxRuntime();
        if (maxRuntime == null || maxRuntime.isNegative()) {
            throw violation(
                    "MAX_RUNTIME_INVALID",
                    "constraints.max_runtime must be non-negative"
            );
        }

        if (maxRuntime.compareTo(limits.maximumRuntime()) > 0) {
            throw violation(
                    "MAX_RUNTIME_TOO_LARGE",
                    "constraints.max_runtime exceeds "
                            + limits.maximumRuntime()
            );
        }

        validateStringMap(
                constraints.policyHints(),
                "constraints.policy_hints",
                limits.maxPolicyHintEntries(),
                limits.maxPolicyHintValueLength()
        );
    }

    private static void validateBudget(MissionBudget budget) {
        if (budget == null) {
            throw violation(
                    "BUDGET_REQUIRED",
                    "budget must not be null"
            );
        }

        requireNonNegative(
                budget.maxLlmCalls(),
                "budget.max_llm_calls"
        );
        requireNonNegative(
                budget.maxToolCalls(),
                "budget.max_tool_calls"
        );
        requireNonNegative(
                budget.maxRetrievalQueries(),
                "budget.max_retrieval_queries"
        );
        requireNonNegative(
                budget.maxItemsToHydrate(),
                "budget.max_items_to_hydrate"
        );
        requireNonNegative(
                budget.maxInputTokens(),
                "budget.max_input_tokens"
        );
        requireNonNegative(
                budget.maxOutputTokens(),
                "budget.max_output_tokens"
        );
        requireNonNegative(
                budget.maxCostMicros(),
                "budget.max_cost_micros"
        );
    }

    private void validateIdentifierList(
            List<String> values,
            String field,
            int maximumEntries
    ) {
        if (values == null) {
            throw violation(
                    "LIST_REQUIRED",
                    field + " must not be null"
            );
        }

        if (values.size() > maximumEntries) {
            throw violation(
                    "LIST_TOO_LARGE",
                    field + " has too many entries"
            );
        }

        Set<String> unique = new HashSet<>();
        for (String value : values) {
            if (value == null
                    || !value.equals(value.trim())
                    || !POLICY_IDENTIFIER.matcher(value).matches()) {
                throw violation(
                        "IDENTIFIER_INVALID",
                        field + " contains an invalid identifier"
                );
            }

            String identity = value.toLowerCase(Locale.ROOT);
            if (!unique.add(identity)) {
                throw violation(
                        "DUPLICATE_IDENTIFIER",
                        field + " contains duplicate " + value
                );
            }
        }
    }

    private void validateSectionList(List<String> sections) {
        if (sections == null) {
            throw violation(
                    "SECTIONS_REQUIRED",
                    "constraints.required_output_sections "
                            + "must not be null"
            );
        }

        if (sections.size() > limits.maxRequiredSections()) {
            throw violation(
                    "TOO_MANY_SECTIONS",
                    "constraints.required_output_sections "
                            + "has too many entries"
            );
        }

        Set<String> unique = new HashSet<>();
        for (String section : sections) {
            requireText(
                    section,
                    "constraints.required_output_sections entry",
                    limits.maxSectionLength()
            );

            if (!section.equals(section.trim())) {
                throw violation(
                        "SECTION_NOT_NORMALIZED",
                        "required output sections must be trimmed"
                );
            }

            if (!unique.add(section.toLowerCase(Locale.ROOT))) {
                throw violation(
                        "DUPLICATE_SECTION",
                        "required output section must be unique: "
                                + section
                );
            }
        }
    }

    private static void validateStringMap(
            Map<String, String> values,
            String field,
            int maximumEntries,
            int maximumValueLength
    ) {
        if (values == null) {
            throw violation(
                    "MAP_REQUIRED",
                    field + " must not be null"
            );
        }

        if (values.size() > maximumEntries) {
            throw violation(
                    "MAP_TOO_LARGE",
                    field + " has too many entries"
            );
        }

        for (Map.Entry<String, String> entry : values.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            if (key == null
                    || key.isBlank()
                    || key.length() > 128
                    || !key.equals(key.trim())) {
                throw violation(
                        "MAP_KEY_INVALID",
                        field + " contains an invalid key"
                );
            }

            if (value == null
                    || value.length() > maximumValueLength) {
                throw violation(
                        "MAP_VALUE_INVALID",
                        field + " contains an invalid value for " + key
                );
            }
        }
    }

    private static void requireNonNegative(
            long value,
            String field
    ) {
        if (value < 0L) {
            throw violation(
                    "BUDGET_FIELD_NEGATIVE",
                    field + " must be >= 0"
            );
        }
    }

    private static String requireText(
            String value,
            String field,
            int maximumLength
    ) {
        if (value == null || value.isBlank()) {
            throw violation(
                    "FIELD_REQUIRED",
                    field + " must not be blank"
            );
        }

        if (value.length() > maximumLength) {
            throw violation(
                    "FIELD_TOO_LONG",
                    field + " exceeds "
                            + maximumLength + " characters"
            );
        }

        return value;
    }

    private static void validateOptionalText(
            String value,
            String field,
            int maximumLength
    ) {
        if (value != null && value.length() > maximumLength) {
            throw violation(
                    "FIELD_TOO_LONG",
                    field + " exceeds "
                            + maximumLength + " characters"
            );
        }
    }

    private static IllegalArgumentException violation(
            String code,
            String detail
    ) {
        return new IllegalArgumentException(
                "MISSION_REQUEST_" + code + ": " + detail
        );
    }

    public record Limits(
            int maxQueryLength,
            int maxArtifacts,
            int maxAllowlistEntries,
            int maxRequiredSections,
            int maxSectionLength,
            int maxContextMetadataEntries,
            int maxPolicyHintEntries,
            int maxMetadataValueLength,
            int maxPolicyHintValueLength,
            Duration maximumRuntime
    ) {
        public Limits {
            if (maxQueryLength < 1
                    || maxArtifacts < 0
                    || maxAllowlistEntries < 0
                    || maxRequiredSections < 0
                    || maxSectionLength < 1
                    || maxContextMetadataEntries < 0
                    || maxPolicyHintEntries < 0
                    || maxMetadataValueLength < 1
                    || maxPolicyHintValueLength < 1) {
                throw new IllegalArgumentException(
                        "mission request validation limits are invalid"
                );
            }

            maximumRuntime = Objects.requireNonNull(
                    maximumRuntime,
                    "maximumRuntime must not be null"
            );

            if (maximumRuntime.isNegative()
                    || maximumRuntime.isZero()) {
                throw new IllegalArgumentException(
                        "maximumRuntime must be positive"
                );
            }
        }

        public static Limits defaults() {
            return new Limits(
                    32_000,
                    32,
                    128,
                    32,
                    128,
                    64,
                    64,
                    2_048,
                    2_048,
                    Duration.ofHours(24)
            );
        }
    }
}