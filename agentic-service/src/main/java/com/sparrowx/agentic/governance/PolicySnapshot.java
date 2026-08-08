package com.sparrowx.agentic.governance;

import com.sparrowx.agentic.mission.model.MissionBudget;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

public record PolicySnapshot(
        String snapshotId,
        String version,
        String tenantId,
        Instant capturedAt,
        MissionBudget defaultBudget,
        MissionBudget maximumBudget,
        Set<String> defaultAllowedTools,
        Set<String> defaultAllowedSourceServices,
        DataHandlingPolicy.Rules dataHandlingRules,
        GroundingPolicy.Requirements groundingRequirements,
        HumanApprovalPolicy.ApprovalRules humanApprovalRules,
        Map<String, String> policyVersions
) {

    private static final Pattern VERSION_TOKEN =
            Pattern.compile(
                    "[A-Za-z0-9][A-Za-z0-9._:/-]{0,127}"
            );

    public PolicySnapshot {
        snapshotId = requireToken(
                snapshotId,
                "snapshotId"
        );
        version = requireToken(
                version,
                "version"
        );
        tenantId = requireText(
                tenantId,
                "tenantId"
        );
        capturedAt = Objects.requireNonNull(
                capturedAt,
                "capturedAt must not be null"
        );
        defaultBudget = Objects.requireNonNull(
                defaultBudget,
                "defaultBudget must not be null"
        );
        maximumBudget = Objects.requireNonNull(
                maximumBudget,
                "maximumBudget must not be null"
        );
        dataHandlingRules = Objects.requireNonNull(
                dataHandlingRules,
                "dataHandlingRules must not be null"
        );
        groundingRequirements = Objects.requireNonNull(
                groundingRequirements,
                "groundingRequirements must not be null"
        );
        humanApprovalRules = Objects.requireNonNull(
                humanApprovalRules,
                "humanApprovalRules must not be null"
        );

        if (!tenantId.equals(dataHandlingRules.tenantId())) {
            throw new IllegalArgumentException(
                    "dataHandlingRules.tenantId must match snapshot tenantId"
            );
        }

        validateBudgetPair(
                defaultBudget,
                maximumBudget
        );

        defaultAllowedTools =
                normalizedAllowlist(defaultAllowedTools);
        defaultAllowedSourceServices =
                normalizedAllowlist(defaultAllowedSourceServices);
        policyVersions =
                immutableVersions(policyVersions);
    }

    private static void validateBudgetPair(
            MissionBudget defaults,
            MissionBudget maximums
    ) {
        if (defaults.maxLlmCalls() < 0
                || defaults.maxToolCalls() < 0
                || defaults.maxRetrievalQueries() < 0
                || defaults.maxItemsToHydrate() < 0
                || defaults.maxInputTokens() < 0L
                || defaults.maxOutputTokens() < 0L
                || defaults.maxCostMicros() < 0L
                || maximums.maxLlmCalls()
                < defaults.maxLlmCalls()
                || maximums.maxToolCalls()
                < defaults.maxToolCalls()
                || maximums.maxRetrievalQueries()
                < defaults.maxRetrievalQueries()
                || maximums.maxItemsToHydrate()
                < defaults.maxItemsToHydrate()
                || maximums.maxInputTokens()
                < defaults.maxInputTokens()
                || maximums.maxOutputTokens()
                < defaults.maxOutputTokens()
                || maximums.maxCostMicros()
                < defaults.maxCostMicros()) {
            throw new IllegalArgumentException(
                    "snapshot budget defaults and maximums are inconsistent"
            );
        }
    }

    private static Set<String> normalizedAllowlist(
            Set<String> values
    ) {
        TreeSet<String> normalized = new TreeSet<>();

        if (values != null) {
            for (String value : values) {
                if (value == null || value.isBlank()) {
                    throw new IllegalArgumentException(
                            "policy allowlists must not contain blank entries"
                    );
                }

                normalized.add(
                        value.trim().toLowerCase(Locale.ROOT)
                );
            }
        }

        return Collections.unmodifiableSet(normalized);
    }

    private static Map<String, String> immutableVersions(
            Map<String, String> versions
    ) {
        if (versions == null || versions.isEmpty()) {
            return Map.of();
        }

        Map<String, String> normalized = new LinkedHashMap<>();

        versions.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> normalized.put(
                        requireToken(
                                entry.getKey(),
                                "policyVersions key"
                        ),
                        requireToken(
                                entry.getValue(),
                                "policyVersions value"
                        )
                ));

        return Collections.unmodifiableMap(normalized);
    }

    private static String requireToken(
            String value,
            String field
    ) {
        String text = requireText(value, field).trim();

        if (!VERSION_TOKEN.matcher(text).matches()) {
            throw new IllegalArgumentException(
                    field + " is not a valid version token"
            );
        }

        return text;
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