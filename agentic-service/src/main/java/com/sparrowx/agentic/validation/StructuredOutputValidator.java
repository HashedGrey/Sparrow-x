package com.sparrowx.agentic.validation;

import com.sparrowx.agentic.components.SynthesisComponent;
import com.sparrowx.agentic.prompts.StructuredOutputSchemas;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

@Component
public final class StructuredOutputValidator {

    private static final int MAX_DEPTH = 32;
    private static final int MAX_NODES = 20_000;

    private final StructuredOutputSchemas schemas;

    public StructuredOutputValidator(
            StructuredOutputSchemas schemas
    ) {
        this.schemas = Objects.requireNonNull(
                schemas,
                "schemas must not be null"
        );
    }

    public Map<String, Object> validateIntent(
            Map<String, Object> output
    ) {
        return validate(
                StructuredOutputSchemas.MISSION_INTENT,
                output
        );
    }

    public Map<String, Object> validatePlan(
            Map<String, Object> output
    ) {
        return validate(
                StructuredOutputSchemas.MISSION_PLAN,
                output
        );
    }

    public Map<String, Object> validateObservation(
            Map<String, Object> output
    ) {
        return validate(
                StructuredOutputSchemas.OBSERVATION,
                output
        );
    }

    public Map<String, Object> validateReview(
            Map<String, Object> output
    ) {
        return validate(
                StructuredOutputSchemas.REVIEW,
                output
        );
    }

    public Map<String, Object> validateSynthesis(
            Map<String, Object> output
    ) {
        return validate(
                StructuredOutputSchemas.SYNTHESIS,
                output
        );
    }

    public SynthesisComponent.SynthesisDraft validateSynthesis(
            SynthesisComponent.SynthesisRequest request,
            SynthesisComponent.SynthesisDraft draft
    ) {
        if (request == null) {
            throw violation(
                    "$",
                    "SYNTHESIS_REQUEST_REQUIRED"
            );
        }

        if (draft == null) {
            throw violation(
                    "$",
                    "SYNTHESIS_DRAFT_REQUIRED"
            );
        }

        if (request.missionId() == null
                || request.missionId().isBlank()) {
            throw violation(
                    "$",
                    "SYNTHESIS_MISSION_ID_REQUIRED"
            );
        }

        if (!request.reactorComplete()) {
            throw violation(
                    "$",
                    "SYNTHESIS_BEFORE_REACTOR_COMPLETION"
            );
        }

        if (draft.executiveSummary() == null
                || draft.executiveSummary().isBlank()) {
            throw violation(
                    "$.executiveSummary",
                    "VALUE_REQUIRED"
            );
        }

        if (draft.finalAnswer() == null
                || draft.finalAnswer().isBlank()) {
            throw violation(
                    "$.finalAnswer",
                    "VALUE_REQUIRED"
            );
        }

        validateSynthesis(draft.structuredOutput());
        return draft;
    }

    public Map<String, Object> validate(
            String schemaName,
            Map<String, Object> output
    ) {
        if (schemaName == null || schemaName.isBlank()) {
            throw new IllegalArgumentException(
                    "STRUCTURED_OUTPUT_SCHEMA_NAME_REQUIRED"
            );
        }

        if (output == null) {
            throw violation("$", "OUTPUT_REQUIRED");
        }

        Map<String, Object> schema = schemas.schema(schemaName);
        NodeBudget budget = new NodeBudget(MAX_NODES);

        validateNode(
                "$",
                output,
                schema,
                0,
                budget
        );

        return immutableObject(output, "$");
    }

    private static void validateNode(
            String path,
            Object value,
            Map<String, Object> schema,
            int depth,
            NodeBudget budget
    ) {
        if (depth > MAX_DEPTH) {
            throw violation(
                    path,
                    "MAXIMUM_DEPTH_EXCEEDED"
            );
        }

        budget.consume(path);

        Set<String> expectedTypes =
                expectedTypes(schema.get("type"));

        if (!expectedTypes.isEmpty()
                && !expectedTypes.stream().anyMatch(
                type -> matchesType(value, type)
        )) {
            throw violation(
                    path,
                    "TYPE_MISMATCH_EXPECTED_"
                            + String.join("_OR_", expectedTypes)
            );
        }

        validateConstAndEnum(path, value, schema);

        if (value instanceof Map<?, ?> objectValue) {
            validateObject(
                    path,
                    objectValue,
                    schema,
                    depth,
                    budget
            );
        } else if (value instanceof List<?> listValue) {
            validateArray(
                    path,
                    listValue,
                    schema,
                    depth,
                    budget
            );
        } else if (value instanceof String stringValue) {
            validateString(path, stringValue, schema);
        } else if (value instanceof Number numberValue) {
            validateNumber(path, numberValue, schema);
        }
    }

    private static void validateObject(
            String path,
            Map<?, ?> value,
            Map<String, Object> schema,
            int depth,
            NodeBudget budget
    ) {
        int minimumProperties = integerKeyword(
                schema,
                "minProperties",
                0
        );
        int maximumProperties = integerKeyword(
                schema,
                "maxProperties",
                Integer.MAX_VALUE
        );

        if (value.size() < minimumProperties
                || value.size() > maximumProperties) {
            throw violation(
                    path,
                    "PROPERTY_COUNT_OUT_OF_RANGE"
            );
        }

        Set<String> keys = new LinkedHashSet<>();
        for (Object rawKey : value.keySet()) {
            if (!(rawKey instanceof String key)) {
                throw violation(
                        path,
                        "NON_STRING_OBJECT_KEY"
                );
            }
            keys.add(key);
        }

        for (String required :
                stringListKeyword(schema, "required")) {
            if (!keys.contains(required)) {
                throw violation(
                        childPath(path, required),
                        "REQUIRED_PROPERTY_MISSING"
                );
            }
        }

        Map<String, Object> properties = schemaMapKeyword(
                schema,
                "properties"
        );

        boolean additionalPropertiesAllowed =
                !Boolean.FALSE.equals(
                        schema.get("additionalProperties")
                );

        for (String key : keys) {
            Object propertySchema = properties.get(key);

            if (propertySchema == null) {
                if (!additionalPropertiesAllowed) {
                    throw violation(
                            childPath(path, key),
                            "ADDITIONAL_PROPERTY_NOT_ALLOWED"
                    );
                }

                validateNode(
                        childPath(path, key),
                        value.get(key),
                        Map.of(),
                        depth + 1,
                        budget
                );
                continue;
            }

            validateNode(
                    childPath(path, key),
                    value.get(key),
                    asSchema(
                            propertySchema,
                            childPath(path, key)
                    ),
                    depth + 1,
                    budget
            );
        }
    }

    private static void validateArray(
            String path,
            List<?> value,
            Map<String, Object> schema,
            int depth,
            NodeBudget budget
    ) {
        int minimumItems = integerKeyword(
                schema,
                "minItems",
                0
        );
        int maximumItems = integerKeyword(
                schema,
                "maxItems",
                Integer.MAX_VALUE
        );

        if (value.size() < minimumItems
                || value.size() > maximumItems) {
            throw violation(
                    path,
                    "ITEM_COUNT_OUT_OF_RANGE"
            );
        }

        if (Boolean.TRUE.equals(schema.get("uniqueItems"))) {
            Set<Object> unique = new LinkedHashSet<>();
            for (Object item : value) {
                if (!unique.add(item)) {
                    throw violation(
                            path,
                            "DUPLICATE_ARRAY_ITEM"
                    );
                }
            }
        }

        Object itemSchema = schema.get("items");
        if (itemSchema == null) {
            return;
        }

        Map<String, Object> typedItemSchema = asSchema(
                itemSchema,
                path + "[]"
        );

        for (int index = 0; index < value.size(); index++) {
            validateNode(
                    path + "[" + index + "]",
                    value.get(index),
                    typedItemSchema,
                    depth + 1,
                    budget
            );
        }
    }

    private static void validateString(
            String path,
            String value,
            Map<String, Object> schema
    ) {
        int length = value.codePointCount(0, value.length());
        int minimumLength = integerKeyword(
                schema,
                "minLength",
                0
        );
        int maximumLength = integerKeyword(
                schema,
                "maxLength",
                Integer.MAX_VALUE
        );

        if (length < minimumLength || length > maximumLength) {
            throw violation(
                    path,
                    "STRING_LENGTH_OUT_OF_RANGE"
            );
        }

        Object patternValue = schema.get("pattern");
        if (patternValue != null) {
            if (!(patternValue instanceof String expression)) {
                throw schemaProblem(
                        path,
                        "pattern must be a string"
                );
            }

            try {
                if (!Pattern.compile(expression)
                        .matcher(value)
                        .find()) {
                    throw violation(
                            path,
                            "STRING_PATTERN_MISMATCH"
                    );
                }
            } catch (PatternSyntaxException exception) {
                throw schemaProblem(
                        path,
                        "pattern is invalid"
                );
            }
        }
    }

    private static void validateNumber(
            String path,
            Number value,
            Map<String, Object> schema
    ) {
        BigDecimal number;

        try {
            number = new BigDecimal(value.toString());
        } catch (NumberFormatException exception) {
            throw violation(path, "NUMBER_NOT_FINITE");
        }

        BigDecimal minimum = decimalKeyword(
                schema,
                "minimum"
        );
        BigDecimal maximum = decimalKeyword(
                schema,
                "maximum"
        );

        if (minimum != null
                && number.compareTo(minimum) < 0) {
            throw violation(
                    path,
                    "NUMBER_BELOW_MINIMUM"
            );
        }

        if (maximum != null
                && number.compareTo(maximum) > 0) {
            throw violation(
                    path,
                    "NUMBER_ABOVE_MAXIMUM"
            );
        }
    }

    private static void validateConstAndEnum(
            String path,
            Object value,
            Map<String, Object> schema
    ) {
        if (schema.containsKey("const")
                && !Objects.equals(schema.get("const"), value)) {
            throw violation(path, "CONST_MISMATCH");
        }

        Object enumValue = schema.get("enum");
        if (enumValue == null) {
            return;
        }

        if (!(enumValue instanceof Collection<?> allowedValues)) {
            throw schemaProblem(
                    path,
                    "enum must be an array"
            );
        }

        if (!allowedValues.contains(value)) {
            throw violation(
                    path,
                    "ENUM_VALUE_NOT_ALLOWED"
            );
        }
    }

    private static Set<String> expectedTypes(
            Object typeKeyword
    ) {
        if (typeKeyword == null) {
            return Set.of();
        }

        if (typeKeyword instanceof String type) {
            return Set.of(type);
        }

        if (typeKeyword instanceof Collection<?> collection) {
            Set<String> types = new LinkedHashSet<>();

            for (Object value : collection) {
                if (!(value instanceof String type)) {
                    throw schemaProblem(
                            "$",
                            "type array must contain strings"
                    );
                }
                types.add(type);
            }

            return Collections.unmodifiableSet(types);
        }

        throw schemaProblem(
                "$",
                "type must be a string or array"
        );
    }

    private static boolean matchesType(
            Object value,
            String type
    ) {
        return switch (type) {
            case "null" -> value == null;
            case "object" -> value instanceof Map<?, ?>;
            case "array" -> value instanceof List<?>;
            case "string" -> value instanceof String;
            case "boolean" -> value instanceof Boolean;
            case "number" -> value instanceof Number
                    && isFinite((Number) value);
            case "integer" -> value instanceof Number number
                    && isFinite(number)
                    && isInteger(number);
            default -> throw schemaProblem(
                    "$",
                    "unsupported schema type: " + type
            );
        };
    }

    private static boolean isFinite(Number value) {
        if (value instanceof Double doubleValue) {
            return Double.isFinite(doubleValue);
        }

        if (value instanceof Float floatValue) {
            return Float.isFinite(floatValue);
        }

        return true;
    }

    private static boolean isInteger(Number value) {
        try {
            return new BigDecimal(value.toString())
                    .stripTrailingZeros()
                    .scale() <= 0;
        } catch (NumberFormatException exception) {
            return false;
        }
    }

    private static int integerKeyword(
            Map<String, Object> schema,
            String keyword,
            int fallback
    ) {
        Object value = schema.get(keyword);
        if (value == null) {
            return fallback;
        }

        if (!(value instanceof Number number)
                || !isInteger(number)) {
            throw schemaProblem(
                    "$",
                    keyword + " must be an integer"
            );
        }

        try {
            return new BigDecimal(number.toString())
                    .intValueExact();
        } catch (ArithmeticException exception) {
            throw schemaProblem(
                    "$",
                    keyword + " is out of range"
            );
        }
    }

    private static BigDecimal decimalKeyword(
            Map<String, Object> schema,
            String keyword
    ) {
        Object value = schema.get(keyword);
        if (value == null) {
            return null;
        }

        if (!(value instanceof Number number)
                || !isFinite(number)) {
            throw schemaProblem(
                    "$",
                    keyword + " must be a finite number"
            );
        }

        try {
            return new BigDecimal(number.toString());
        } catch (NumberFormatException exception) {
            throw schemaProblem(
                    "$",
                    keyword + " must be a number"
            );
        }
    }

    private static List<String> stringListKeyword(
            Map<String, Object> schema,
            String keyword
    ) {
        Object value = schema.get(keyword);
        if (value == null) {
            return List.of();
        }

        if (!(value instanceof Collection<?> collection)) {
            throw schemaProblem(
                    "$",
                    keyword + " must be an array"
            );
        }

        List<String> result =
                new ArrayList<>(collection.size());

        for (Object item : collection) {
            if (!(item instanceof String string)) {
                throw schemaProblem(
                        "$",
                        keyword + " must contain only strings"
                );
            }
            result.add(string);
        }

        return List.copyOf(result);
    }

    private static Map<String, Object> schemaMapKeyword(
            Map<String, Object> schema,
            String keyword
    ) {
        Object value = schema.get(keyword);
        return value == null
                ? Map.of()
                : asSchema(value, "$." + keyword);
    }

    private static Map<String, Object> asSchema(
            Object value,
            String path
    ) {
        if (!(value instanceof Map<?, ?> map)) {
            throw schemaProblem(
                    path,
                    "schema node must be an object"
            );
        }

        Map<String, Object> typed = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (!(entry.getKey() instanceof String key)) {
                throw schemaProblem(
                        path,
                        "schema keys must be strings"
                );
            }
            typed.put(key, entry.getValue());
        }

        return typed;
    }

    private static Map<String, Object> immutableObject(
            Map<String, Object> value,
            String path
    ) {
        Map<String, Object> copy = new LinkedHashMap<>();

        for (Map.Entry<String, Object> entry : value.entrySet()) {
            if (entry.getKey() == null) {
                throw violation(
                        path,
                        "NULL_OBJECT_KEY"
                );
            }

            copy.put(
                    entry.getKey(),
                    immutableValue(
                            entry.getValue(),
                            childPath(path, entry.getKey())
                    )
            );
        }

        return Collections.unmodifiableMap(copy);
    }

    private static Object immutableValue(
            Object value,
            String path
    ) {
        if (value == null
                || value instanceof String
                || value instanceof Boolean
                || value instanceof Number) {
            return value;
        }

        if (value instanceof Map<?, ?> map) {
            Map<String, Object> typed = new LinkedHashMap<>();

            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (!(entry.getKey() instanceof String key)) {
                    throw violation(
                            path,
                            "NON_STRING_OBJECT_KEY"
                    );
                }
                typed.put(key, entry.getValue());
            }

            return immutableObject(typed, path);
        }

        if (value instanceof List<?> list) {
            List<Object> copy = new ArrayList<>(list.size());

            for (int index = 0; index < list.size(); index++) {
                copy.add(
                        immutableValue(
                                list.get(index),
                                path + "[" + index + "]"
                        )
                );
            }

            return Collections.unmodifiableList(copy);
        }

        throw violation(path, "NON_JSON_VALUE");
    }

    private static String childPath(
            String parent,
            String child
    ) {
        return parent + "." + child;
    }

    private static SchemaViolation violation(
            String path,
            String code
    ) {
        return new SchemaViolation(
                "STRUCTURED_OUTPUT_" + code
                        + " at " + path
        );
    }

    private static IllegalStateException schemaProblem(
            String path,
            String detail
    ) {
        return new IllegalStateException(
                "STRUCTURED_OUTPUT_SCHEMA_INVALID at "
                        + path + ": " + detail
        );
    }

    public static final class SchemaViolation
            extends IllegalArgumentException {

        public SchemaViolation(String message) {
            super(message);
        }
    }

    private static final class NodeBudget {

        private int remaining;

        private NodeBudget(int remaining) {
            this.remaining = remaining;
        }

        private void consume(String path) {
            remaining--;

            if (remaining < 0) {
                throw violation(
                        path,
                        "NODE_LIMIT_EXCEEDED"
                );
            }
        }
    }
}