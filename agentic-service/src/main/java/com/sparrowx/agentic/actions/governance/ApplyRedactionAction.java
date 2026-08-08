package com.sparrowx.agentic.actions.governance;

import com.embabel.agent.api.annotation.Action;
import com.sparrowx.agentic.governance.DataHandlingPolicy;
import com.sparrowx.agentic.governance.model.GovernanceDecision;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class ApplyRedactionAction {

    private final DataHandlingPolicy dataHandlingPolicy;

    public ApplyRedactionAction(DataHandlingPolicy dataHandlingPolicy) {
        this.dataHandlingPolicy = Objects.requireNonNull(
                dataHandlingPolicy,
                "dataHandlingPolicy must not be null"
        );
    }

    @Action
    public Result execute(RedactionSpec spec) {
        Objects.requireNonNull(spec, "spec must not be null");

        DataHandlingPolicy.Outcome outcome = dataHandlingPolicy.apply(
                spec.decisionId(),
                spec.tenantId(),
                spec.payload(),
                spec.rules()
        );

        return new Result(
                outcome.redactedPayload(),
                outcome.redactedPaths(),
                List.of(outcome.decision())
        );
    }

    public record RedactionSpec(
            String decisionId,
            String tenantId,
            String subjectId,
            Map<String, Object> payload,
            DataHandlingPolicy.Rules rules
    ) {
        public RedactionSpec {
            decisionId = requireText(decisionId, "decisionId");
            tenantId = requireText(tenantId, "tenantId");
            subjectId = requireText(subjectId, "subjectId");
            payload = payload == null
                    ? Map.of()
                    : Collections.unmodifiableMap(new LinkedHashMap<>(payload));
            rules = Objects.requireNonNull(rules, "rules must not be null");
        }
    }

    public record Result(
            Map<String, Object> redactedPayload,
            List<String> redactedPaths,
            List<GovernanceDecision> governanceDecisions
    ) {
        public Result {
            redactedPayload = redactedPayload == null
                    ? Map.of()
                    : Collections.unmodifiableMap(new LinkedHashMap<>(redactedPayload));
            redactedPaths = redactedPaths == null
                    ? List.of()
                    : List.copyOf(redactedPaths);
            governanceDecisions = governanceDecisions == null
                    ? List.of()
                    : List.copyOf(governanceDecisions);
        }
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value;
    }
}