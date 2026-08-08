package com.sparrowx.agentic.prompts;

import com.sparrowx.agentic.planning.StepKind;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class StructuredOutputSchemas {

    public static final String MISSION_INTENT =
            "mission-intent";
    public static final String MISSION_PLAN =
            "mission-plan";
    public static final String OBSERVATION =
            "observation";
    public static final String SYNTHESIS =
            "synthesis";
    public static final String REVIEW =
            "review";

    private static final Set<String> REQUIRED_SCHEMA_NAMES =
            Set.of(
                    MISSION_INTENT,
                    MISSION_PLAN,
                    OBSERVATION,
                    SYNTHESIS,
                    REVIEW);

    private final Map<String, VersionedSchema> schemas;

    public StructuredOutputSchemas(
            Map<String, VersionedSchema> schemas) {

        Objects.requireNonNull(
                schemas,
                "schemas must not be null");

        Map<String, VersionedSchema> copy =
                Map.copyOf(schemas);

        if (!copy.keySet().containsAll(
                REQUIRED_SCHEMA_NAMES)) {
            throw new IllegalArgumentException(
                    "intent, plan, observation, synthesis "
                            + "and review schemas are required");
        }

        for (Map.Entry<String, VersionedSchema> entry
                : copy.entrySet()) {

            if (!entry.getKey().equals(
                    entry.getValue().name())) {
                throw new IllegalArgumentException(
                        "schema registry key does not match name: "
                                + entry.getKey());
            }
        }

        this.schemas = copy;
    }

    public Map<String, Object> schema(String schemaName) {
        return definition(schemaName).schema();
    }

    public String version(String schemaName) {
        return definition(schemaName).version();
    }

    public VersionedSchema definition(String schemaName) {
        String name = requireText(schemaName, "schemaName");
        VersionedSchema schema = schemas.get(name);

        if (schema == null) {
            throw new IllegalArgumentException(
                    "unknown structured-output schema: " + name);
        }

        return schema;
    }

    public static StructuredOutputSchemas defaults() {
        Map<String, VersionedSchema> defaults =
                new LinkedHashMap<>();

        defaults.put(
                MISSION_INTENT,
                new VersionedSchema(
                        MISSION_INTENT,
                        "1",
                        missionIntentSchema()));
        defaults.put(
                MISSION_PLAN,
                new VersionedSchema(
                        MISSION_PLAN,
                        "1",
                        missionPlanSchema()));
        defaults.put(
                OBSERVATION,
                new VersionedSchema(
                        OBSERVATION,
                        "1",
                        observationSchema()));
        defaults.put(
                SYNTHESIS,
                new VersionedSchema(
                        SYNTHESIS,
                        "1",
                        synthesisSchema()));
        defaults.put(
                REVIEW,
                new VersionedSchema(
                        REVIEW,
                        "1",
                        reviewSchema()));

        return new StructuredOutputSchemas(defaults);
    }

    private static Map<String, Object> missionIntentSchema() {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("missionId", stringSchema());
        properties.put("objective", stringSchema());
        properties.put(
                "selectedPath",
                enumSchema(List.of(
                        "FAST",
                        "RESEARCH",
                        "GOVERNED")));
        properties.put(
                "targetEntities",
                arraySchema(stringSchema()));
        properties.put(
                "topics",
                arraySchema(stringSchema()));
        properties.put(
                "requiredOutputSections",
                arraySchema(stringSchema()));
        properties.put(
                "requiresDocumentEvidence",
                booleanSchema());
        properties.put(
                "requiresInternalContext",
                booleanSchema());
        properties.put(
                "requiresHumanReview",
                booleanSchema());
        properties.put(
                "requiresCitations",
                booleanSchema());
        properties.put(
                "requiresVerification",
                booleanSchema());
        properties.put(
                "allowsExternalSources",
                booleanSchema());
        properties.put(
                "allowedTools",
                arraySchema(stringSchema()));
        properties.put(
                "allowedSourceServices",
                arraySchema(stringSchema()));
        properties.put("attributes", objectSchema());

        return objectSchema(
                properties,
                List.of(
                        "missionId",
                        "objective",
                        "selectedPath",
                        "requiresDocumentEvidence",
                        "requiresInternalContext",
                        "requiresHumanReview",
                        "requiresCitations",
                        "requiresVerification",
                        "allowsExternalSources"));
    }

    private static Map<String, Object> missionPlanSchema() {
        Map<String, Object> stepProperties =
                new LinkedHashMap<>();
        stepProperties.put("stepId", stringSchema());
        stepProperties.put(
                "kind",
                enumSchema(Arrays.stream(StepKind.values())
                        .map(Enum::name)
                        .toList()));
        stepProperties.put(
                "dependencyStepIds",
                arraySchema(stringSchema()));
        stepProperties.put("objective", stringSchema());
        stepProperties.put(
                "expectedOutput",
                stringSchema());
        stepProperties.put(
                "requiresHumanApproval",
                booleanSchema());
        stepProperties.put("arguments", objectSchema());
        stepProperties.put("attributes", objectSchema());

        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("planId", stringSchema());
        properties.put("missionId", stringSchema());
        properties.put(
                "revision",
                Map.of(
                        "type", "integer",
                        "minimum", 1));
        properties.put(
                "steps",
                arraySchema(objectSchema(
                        stepProperties,
                        List.of(
                                "stepId",
                                "kind",
                                "objective",
                                "expectedOutput",
                                "requiresHumanApproval"))));
        properties.put("rationale", stringSchema());
        properties.put("attributes", objectSchema());

        return objectSchema(
                properties,
                List.of(
                        "planId",
                        "missionId",
                        "revision",
                        "steps"));
    }

    private static Map<String, Object> observationSchema() {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("stepId", stringSchema());
        properties.put(
                "stepKind",
                enumSchema(Arrays.stream(StepKind.values())
                        .map(Enum::name)
                        .toList()));
        properties.put("summary", stringSchema());
        properties.put(
                "checkpointReference",
                stringSchema());
        properties.put("attributes", objectSchema());

        return objectSchema(
                properties,
                List.of(
                        "stepId",
                        "stepKind",
                        "summary"));
    }

    private static Map<String, Object> synthesisSchema() {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put(
                "executiveSummary",
                stringSchema());
        properties.put("finalAnswer", stringSchema());
        properties.put(
                "sections",
                arraySchema(objectSchema()));
        properties.put(
                "findings",
                arraySchema(objectSchema()));
        properties.put(
                "recommendations",
                arraySchema(objectSchema()));
        properties.put(
                "structuredOutput",
                objectSchema());
        properties.put("debugSummary", objectSchema());

        return objectSchema(
                properties,
                List.of(
                        "executiveSummary",
                        "finalAnswer",
                        "sections",
                        "findings",
                        "recommendations"));
    }

    private static Map<String, Object> reviewSchema() {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put(
                "type",
                enumSchema(List.of(
                        "CONTINUE",
                        "REPLAN",
                        "COMPLETE",
                        "WAIT_FOR_APPROVAL",
                        "FAIL")));
        properties.put("reason", stringSchema());
        properties.put("planHints", objectSchema());

        return objectSchema(
                properties,
                List.of("type", "reason"));
    }

    private static Map<String, Object> objectSchema(
            Map<String, Object> properties,
            List<String> required) {

        return Map.of(
                "type", "object",
                "additionalProperties", false,
                "properties", Map.copyOf(properties),
                "required", List.copyOf(required));
    }

    private static Map<String, Object> objectSchema() {
        return Map.of(
                "type", "object",
                "additionalProperties", true);
    }

    private static Map<String, Object> arraySchema(
            Map<String, Object> itemSchema) {

        return Map.of(
                "type", "array",
                "items", itemSchema);
    }

    private static Map<String, Object> enumSchema(
            List<String> values) {

        return Map.of(
                "type", "string",
                "enum", List.copyOf(values));
    }

    private static Map<String, Object> stringSchema() {
        return Map.of("type", "string");
    }

    private static Map<String, Object> booleanSchema() {
        return Map.of("type", "boolean");
    }

    public record VersionedSchema(
            String name,
            String version,
            Map<String, Object> schema) {

        public VersionedSchema {
            name = requireText(name, "name");
            version = requireText(version, "version");
            schema = schema == null
                    ? Map.of()
                    : Map.copyOf(schema);

            if (schema.isEmpty()) {
                throw new IllegalArgumentException(
                        "schema must not be empty");
            }
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