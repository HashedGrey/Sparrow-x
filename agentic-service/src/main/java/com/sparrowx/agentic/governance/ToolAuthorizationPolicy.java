package com.sparrowx.agentic.governance;

import com.sparrowx.agentic.governance.model.GovernanceDecision;
import com.sparrowx.agentic.governance.model.GovernanceDecisionType;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

@Component
public final class ToolAuthorizationPolicy {

    public Authorization authorize(
            String decisionId,
            String toolName,
            Collection<String> publicAllowlist,
            Collection<String> intentAllowlist,
            Collection<String> configuredDefaultAllowlist
    ) {
        requireText(decisionId, "decisionId");

        String tool = canonical(toolName);

        Set<String> configured =
                normalized(configuredDefaultAllowlist);
        Set<String> requested =
                normalized(publicAllowlist);
        Set<String> intended =
                normalized(intentAllowlist);

        Set<String> effective = effectiveAllowlist(
                configured,
                requested,
                intended
        );

        boolean named = !tool.isBlank();
        boolean allowed = named && effective.contains(tool);

        String reason;

        if (!named) {
            reason =
                    "Tool name is blank; authorization fails closed.";
        } else if (configured.isEmpty()) {
            reason =
                    "Configured tool policy is empty; authorization fails closed.";
        } else if (allowed) {
            reason =
                    "Tool is present in the effective allowlist.";
        } else {
            reason =
                    "Tool is absent from the effective allowlist.";
        }

        Map<String, Object> attributes = new LinkedHashMap<>();
        attributes.put("toolName", tool);
        attributes.put(
                "configuredAllowlistCount",
                configured.size()
        );
        attributes.put(
                "publicAllowlistCount",
                requested.size()
        );
        attributes.put(
                "intentAllowlistCount",
                intended.size()
        );
        attributes.put(
                "effectiveAllowlist",
                effective
        );

        GovernanceDecision decision = new GovernanceDecision(
                decisionId,
                "tool-authorization",
                allowed
                        ? GovernanceDecisionType.ALLOWED
                        : GovernanceDecisionType.DENIED,
                reason,
                attributes
        );

        return new Authorization(
                allowed,
                effective,
                decision
        );
    }

    public Set<String> effectiveAllowlist(
            Collection<String> configuredDefaultAllowlist,
            Collection<String> publicAllowlist,
            Collection<String> intentAllowlist
    ) {
        return effectiveAllowlist(
                normalized(configuredDefaultAllowlist),
                normalized(publicAllowlist),
                normalized(intentAllowlist)
        );
    }

    private static Set<String> effectiveAllowlist(
            Set<String> configured,
            Set<String> requested,
            Set<String> intended
    ) {
        TreeSet<String> effective =
                new TreeSet<>(configured);

        if (!requested.isEmpty()) {
            effective.retainAll(requested);
        }

        if (!intended.isEmpty()) {
            effective.retainAll(intended);
        }

        return Collections.unmodifiableSet(effective);
    }

    private static Set<String> normalized(
            Collection<String> values
    ) {
        TreeSet<String> normalized = new TreeSet<>();

        if (values != null) {
            values.stream()
                    .filter(Objects::nonNull)
                    .map(ToolAuthorizationPolicy::canonical)
                    .filter(value -> !value.isBlank())
                    .forEach(normalized::add);
        }

        return Collections.unmodifiableSet(normalized);
    }

    private static String canonical(String value) {
        return value == null
                ? ""
                : value.trim().toLowerCase(Locale.ROOT);
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

    public record Authorization(
            boolean allowed,
            Set<String> effectiveAllowlist,
            GovernanceDecision decision
    ) {
        public Authorization {
            effectiveAllowlist = effectiveAllowlist == null
                    ? Set.of()
                    : Collections.unmodifiableSet(
                    new TreeSet<>(effectiveAllowlist)
            );
            decision = Objects.requireNonNull(
                    decision,
                    "decision must not be null"
            );
        }
    }
}