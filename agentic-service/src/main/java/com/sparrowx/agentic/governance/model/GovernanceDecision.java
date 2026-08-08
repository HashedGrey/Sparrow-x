package com.sparrowx.agentic.governance.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * One auditable governance outcome included in the mission result.
 */
public record GovernanceDecision(
        String decisionId,
        String policyName,
        GovernanceDecisionType decision,
        String reason,
        Map<String, Object> attributes
) {

    public GovernanceDecision {
        decisionId = nullToEmpty(decisionId);
        policyName = nullToEmpty(policyName);
        decision = decision == null ? GovernanceDecisionType.UNSPECIFIED : decision;
        reason = nullToEmpty(reason);
        attributes = immutableStruct(attributes);
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private static Map<String, Object> immutableStruct(Map<String, Object> value) {
        return value == null
                ? Map.of()
                : Collections.unmodifiableMap(new LinkedHashMap<>(value));
    }
}