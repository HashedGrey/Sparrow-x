package com.sparrowx.agentic.governance;

import com.sparrowx.agentic.governance.model.GovernanceDecision;
import com.sparrowx.agentic.governance.model.GovernanceDecisionType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

@Component
public final class DataHandlingPolicy {

    private static final String REDACTION_MARKER = "[REDACTED]";
    private static final String TRUNCATION_MARKER = "…[TRUNCATED]";

    public Outcome apply(
            String decisionId,
            String tenantId,
            Map<String, Object> payload,
            Rules rules
    ) {
        requireText(decisionId, "decisionId");
        requireText(tenantId, "tenantId");
        Objects.requireNonNull(payload, "payload must not be null");
        Objects.requireNonNull(rules, "rules must not be null");

        if (!tenantId.equals(rules.tenantId())) {
            GovernanceDecision denied = new GovernanceDecision(
                    decisionId,
                    "data-handling",
                    GovernanceDecisionType.DENIED,
                    "Tenant does not match the frozen data-handling policy.",
                    Map.of(
                            "policyVersion", rules.version(),
                            "tenantMatch", false,
                            "redactionCount", 0
                    )
            );

            return new Outcome(
                    Map.of(),
                    List.of(),
                    denied
            );
        }

        List<String> changedPaths = new ArrayList<>();
        Map<String, Object> redacted = sanitizeMap(
                payload,
                "$",
                rules,
                changedPaths
        );

        List<String> stablePaths = changedPaths.stream()
                .distinct()
                .sorted()
                .toList();

        GovernanceDecision decision = new GovernanceDecision(
                decisionId,
                "data-handling",
                stablePaths.isEmpty()
                        ? GovernanceDecisionType.ALLOWED
                        : GovernanceDecisionType.REDACTED,
                stablePaths.isEmpty()
                        ? "Payload satisfies the frozen data-handling policy."
                        : "Sensitive or disallowed fields were deterministically redacted.",
                Map.of(
                        "policyVersion", rules.version(),
                        "tenantMatch", true,
                        "redactionCount", stablePaths.size()
                )
        );

        return new Outcome(
                redacted,
                stablePaths,
                decision
        );
    }

    private static Map<String, Object> sanitizeMap(
            Map<?, ?> source,
            String path,
            Rules rules,
            List<String> changedPaths
    ) {
        List<Map.Entry<?, ?>> entries =
                new ArrayList<>(source.entrySet());

        entries.sort(
                Comparator.comparing(
                        entry -> String.valueOf(entry.getKey())
                )
        );

        Map<String, Object> result = new LinkedHashMap<>();

        for (Map.Entry<?, ?> entry : entries) {
            String key = String.valueOf(entry.getKey());
            String childPath = path + "." + key;

            result.put(
                    key,
                    sanitizeValue(
                            entry.getValue(),
                            key,
                            childPath,
                            rules,
                            changedPaths
                    )
            );
        }

        return Collections.unmodifiableMap(result);
    }

    private static Object sanitizeValue(
            Object value,
            String fieldName,
            String path,
            Rules rules,
            List<String> changedPaths
    ) {
        if (rules.sensitiveFieldNames().contains(canonical(fieldName))
                || rules.redactUriFields() && isUriField(fieldName)
                || value instanceof byte[]
                || value instanceof char[]) {
            changedPaths.add(path);
            return REDACTION_MARKER;
        }

        if (value == null
                || value instanceof Number
                || value instanceof Boolean) {
            return value;
        }

        if (value instanceof String text) {
            if (rules.maxStringLength() > 0
                    && text.length() > rules.maxStringLength()) {
                changedPaths.add(path);

                return text.substring(
                        0,
                        rules.maxStringLength()
                ) + TRUNCATION_MARKER;
            }

            return text;
        }

        if (value instanceof Map<?, ?> map) {
            return sanitizeMap(
                    map,
                    path,
                    rules,
                    changedPaths
            );
        }

        if (value instanceof List<?> list) {
            List<Object> sanitized =
                    new ArrayList<>(list.size());

            for (int index = 0; index < list.size(); index++) {
                sanitized.add(
                        sanitizeValue(
                                list.get(index),
                                "",
                                path + "[" + index + "]",
                                rules,
                                changedPaths
                        )
                );
            }

            return Collections.unmodifiableList(sanitized);
        }

        if (value instanceof Collection<?>) {
            changedPaths.add(path);
            return REDACTION_MARKER;
        }

        changedPaths.add(path);
        return REDACTION_MARKER;
    }

    private static boolean isUriField(String fieldName) {
        String normalized = canonical(fieldName);

        return normalized.endsWith("uri")
                || normalized.endsWith("url")
                || normalized.endsWith("link");
    }

    private static String canonical(String value) {
        return value == null
                ? ""
                : value.trim()
                .toLowerCase(Locale.ROOT)
                .replace("-", "")
                .replace("_", "");
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    field + " must not be blank"
            );
        }
        return value;
    }

    public record Rules(
            String tenantId,
            String version,
            Set<String> sensitiveFieldNames,
            boolean redactUriFields,
            int maxStringLength
    ) {
        public Rules {
            tenantId = requireText(tenantId, "tenantId");
            version = requireText(version, "version");

            if (maxStringLength < 0) {
                throw new IllegalArgumentException(
                        "maxStringLength must be >= 0"
                );
            }

            TreeSet<String> normalized = new TreeSet<>();

            if (sensitiveFieldNames != null) {
                sensitiveFieldNames.stream()
                        .map(DataHandlingPolicy::canonical)
                        .filter(value -> !value.isBlank())
                        .forEach(normalized::add);
            }

            sensitiveFieldNames =
                    Collections.unmodifiableSet(normalized);
        }
    }

    public record Outcome(
            Map<String, Object> redactedPayload,
            List<String> redactedPaths,
            GovernanceDecision decision
    ) {
        public Outcome {
            redactedPayload = redactedPayload == null
                    ? Map.of()
                    : Collections.unmodifiableMap(
                    new LinkedHashMap<>(redactedPayload)
            );
            redactedPaths = redactedPaths == null
                    ? List.of()
                    : List.copyOf(redactedPaths);
            decision = Objects.requireNonNull(
                    decision,
                    "decision must not be null"
            );
        }
    }
}