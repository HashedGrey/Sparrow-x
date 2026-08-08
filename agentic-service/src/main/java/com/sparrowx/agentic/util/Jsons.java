package com.sparrowx.agentic.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Objects;

/**
 * Canonical JSON serialization helpers.
 *
 * The configured ObjectMapper is supplied by SerializationConfig.
 */
@Component
public final class Jsons {

    private final ObjectMapper objectMapper;

    public Jsons(ObjectMapper objectMapper) {
        this.objectMapper = Objects.requireNonNull(
                objectMapper,
                "objectMapper must not be null"
        );
    }

    public String write(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException(
                    "Failed to serialize value as JSON",
                    exception
            );
        }
    }

    public byte[] writeBytes(Object value) {
        try {
            return objectMapper.writeValueAsBytes(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException(
                    "Failed to serialize value as JSON bytes",
                    exception
            );
        }
    }

    public <T> T read(String json, Class<T> targetType) {
        Objects.requireNonNull(json, "json must not be null");
        Objects.requireNonNull(targetType, "targetType must not be null");

        try {
            return objectMapper.readValue(json, targetType);
        } catch (IOException exception) {
            throw malformedJson(targetType.getName(), exception);
        }
    }

    public <T> T read(
            String json,
            TypeReference<T> targetType
    ) {
        Objects.requireNonNull(json, "json must not be null");
        Objects.requireNonNull(targetType, "targetType must not be null");

        try {
            return objectMapper.readValue(json, targetType);
        } catch (IOException exception) {
            throw malformedJson(targetType.getType().getTypeName(), exception);
        }
    }

    public <T> T read(
            byte[] json,
            Class<T> targetType
    ) {
        Objects.requireNonNull(json, "json must not be null");
        Objects.requireNonNull(targetType, "targetType must not be null");

        try {
            return objectMapper.readValue(json, targetType);
        } catch (IOException exception) {
            throw malformedJson(targetType.getName(), exception);
        }
    }

    public <T> T read(
            byte[] json,
            TypeReference<T> targetType
    ) {
        Objects.requireNonNull(json, "json must not be null");
        Objects.requireNonNull(targetType, "targetType must not be null");

        try {
            return objectMapper.readValue(json, targetType);
        } catch (IOException exception) {
            throw malformedJson(targetType.getType().getTypeName(), exception);
        }
    }

    public <T> T convert(
            Object value,
            Class<T> targetType
    ) {
        Objects.requireNonNull(targetType, "targetType must not be null");

        try {
            return objectMapper.convertValue(value, targetType);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Failed to convert JSON value to "
                            + targetType.getName(),
                    exception
            );
        }
    }

    public <T> T convert(
            Object value,
            TypeReference<T> targetType
    ) {
        Objects.requireNonNull(targetType, "targetType must not be null");

        try {
            return objectMapper.convertValue(value, targetType);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Failed to convert JSON value to "
                            + targetType.getType().getTypeName(),
                    exception
            );
        }
    }

    public JsonNode tree(Object value) {
        try {
            return objectMapper.valueToTree(value);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Failed to convert value to JSON tree",
                    exception
            );
        }
    }

    private static IllegalArgumentException malformedJson(
            String targetType,
            Exception cause
    ) {
        return new IllegalArgumentException(
                "Malformed JSON for target type " + targetType,
                cause
        );
    }
}