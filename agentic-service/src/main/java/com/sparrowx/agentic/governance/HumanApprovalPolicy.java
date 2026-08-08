package com.sparrowx.agentic.governance;

import com.sparrowx.agentic.governance.model.GovernanceDecision;
import com.sparrowx.agentic.governance.model.GovernanceDecisionType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

@Component
public final class HumanApprovalPolicy {

    public GovernanceDecision evaluate(
            String decisionId,
            ApprovalRules rules,
            ApprovalContext context
    ) {
        requireText(decisionId, "decisionId");
        Objects.requireNonNull(
                rules,
                "rules must not be null"
        );
        Objects.requireNonNull(
                context,
                "context must not be null"
        );

        List<String> reasons = new ArrayList<>();

        if (context.missionRequestedReview()) {
            reasons.add("MISSION_REQUESTED_REVIEW");
        }

        if (rules.requireForSensitiveData()
                && context.sensitiveData()) {
            reasons.add("SENSITIVE_DATA");
        }

        if (rules.requireForExternalMutation()
                && context.externalMutation()) {
            reasons.add("EXTERNAL_MUTATION");
        }

        boolean invalidConfidence =
                !Double.isFinite(context.confidence())
                        || context.confidence() < 0.0d
                        || context.confidence() > 1.0d;

        if (invalidConfidence) {
            reasons.add("INVALID_CONFIDENCE");
        } else if (rules.requireForLowConfidence()
                && context.confidence()
                < rules.minimumConfidenceWithoutReview()) {
            reasons.add("LOW_CONFIDENCE");
        }

        if (!context.toolName().isBlank()
                && rules.gatedTools().contains(
                canonical(context.toolName())
        )) {
            reasons.add("GATED_TOOL");
        }

        if (!Collections.disjoint(
                rules.gatedPolicyTags(),
                context.policyTags()
        )) {
            reasons.add("GATED_POLICY_TAG");
        }

        Map<String, Object> attributes = new LinkedHashMap<>();
        attributes.put(
                "reasonCodes",
                List.copyOf(reasons)
        );
        attributes.put(
                "sensitiveData",
                context.sensitiveData()
        );
        attributes.put(
                "externalMutation",
                context.externalMutation()
        );
        attributes.put(
                "toolName",
                context.toolName()
        );
        attributes.put(
                "confidence",
                invalidConfidence ? 0.0d : context.confidence()
        );

        boolean required = !reasons.isEmpty();

        return new GovernanceDecision(
                decisionId,
                "human-approval",
                required
                        ? GovernanceDecisionType.REQUIRES_HUMAN_REVIEW
                        : GovernanceDecisionType.ALLOWED,
                required
                        ? "Human approval required: "
                        + String.join(",", reasons)
                        : "Human approval is not required by the frozen policy.",
                attributes
        );
    }

    private static String canonical(String value) {
        return value == null
                ? ""
                : value.trim().toLowerCase(Locale.ROOT);
    }

    private static Set<String> normalizedSet(Set<String> values) {
        TreeSet<String> normalized = new TreeSet<>();

        if (values != null) {
            values.stream()
                    .filter(Objects::nonNull)
                    .map(HumanApprovalPolicy::canonical)
                    .filter(value -> !value.isBlank())
                    .forEach(normalized::add);
        }

        return Collections.unmodifiableSet(normalized);
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    field + " must not be blank"
            );
        }
        return value;
    }

    public record ApprovalRules(
            boolean requireForSensitiveData,
            boolean requireForExternalMutation,
            boolean requireForLowConfidence,
            double minimumConfidenceWithoutReview,
            Set<String> gatedTools,
            Set<String> gatedPolicyTags
    ) {
        public ApprovalRules {
            if (!Double.isFinite(minimumConfidenceWithoutReview)
                    || minimumConfidenceWithoutReview < 0.0d
                    || minimumConfidenceWithoutReview > 1.0d) {
                throw new IllegalArgumentException(
                        "minimumConfidenceWithoutReview must be between 0 and 1"
                );
            }

            gatedTools = normalizedSet(gatedTools);
            gatedPolicyTags = normalizedSet(gatedPolicyTags);
        }
    }

    public record ApprovalContext(
            boolean missionRequestedReview,
            boolean sensitiveData,
            boolean externalMutation,
            double confidence,
            String toolName,
            Set<String> policyTags
    ) {
        public ApprovalContext {
            toolName = canonical(toolName);
            policyTags = normalizedSet(policyTags);
        }
    }
}