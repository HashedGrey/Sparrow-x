package com.sparrowx.agentic.runtime.gate;

import com.sparrowx.agentic.mission.evidence.EvidenceRef;
import org.springframework.stereotype.Component;

import java.time.Instant;
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
import java.util.TreeMap;
import java.util.regex.Pattern;

@Component
public final class ApprovalPayloadBuilder {

    private static final int MAX_CONTEXT_FIELDS = 32;
    private static final int MAX_LIST_ITEMS = 50;
    private static final int MAX_TEXT_LENGTH = 2_000;
    private static final int MAX_EVIDENCE_ITEMS = 200;

    private static final Set<String> DEFAULT_ALLOWED_CONTEXT_FIELDS = Set.of(
            "action",
            "action_type",
            "confidence",
            "estimated_cost_micros",
            "objective",
            "policy",
            "policy_reason",
            "requested_source",
            "requested_tool",
            "risk_summary",
            "source_service",
            "summary"
    );

    private static final Pattern OPAQUE_CHECKPOINT_REFERENCE = Pattern.compile(
            "[A-Za-z0-9][A-Za-z0-9._:/-]{0,255}"
    );

    private static final Pattern LIKELY_SECRET_VALUE = Pattern.compile(
            "(?i)(-----BEGIN [A-Z ]*PRIVATE KEY-----"
                    + "|\\bBearer\\s+[A-Za-z0-9._~+/=-]{12,}"
                    + "|\\b(password|passwd|api[_-]?key|access[_-]?token"
                    + "|refresh[_-]?token|client[_-]?secret)\\s*[:=])"
    );

    private final Set<String> allowedContextFields;

    public ApprovalPayloadBuilder() {
        this(DEFAULT_ALLOWED_CONTEXT_FIELDS);
    }

    private ApprovalPayloadBuilder(Set<String> allowedContextFields) {
        this.allowedContextFields = normalizeAllowedFields(allowedContextFields);
    }

    public static ApprovalPayloadBuilder configured(
            Set<String> allowedContextFields
    ) {
        return new ApprovalPayloadBuilder(allowedContextFields);
    }

    public Map<String, Object> build(Request request) {
        Objects.requireNonNull(request, "request must not be null");

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("gateId", safeText(request.gateId(), "gateId"));
        payload.put("missionId", safeText(request.missionId(), "missionId"));
        payload.put("title", safeText(request.title(), "title"));
        payload.put("reason", safeText(request.reason(), "reason"));

        Map<String, Object> context = safeContext(request.reviewContext());
        if (!context.isEmpty()) {
            payload.put("context", context);
        }

        List<Map<String, Object>> evidence = safeEvidence(request.evidenceRefs());
        if (!evidence.isEmpty()) {
            payload.put("evidence", evidence);
        }

        Map<String, String> checkpoints = safeCheckpointReferences(
                request.checkpointReferences()
        );
        if (!checkpoints.isEmpty()) {
            payload.put("checkpointReferences", checkpoints);
        }

        return Collections.unmodifiableMap(payload);
    }

    private Map<String, Object> safeContext(Map<String, Object> input) {
        if (input.isEmpty()) {
            return Map.of();
        }

        Map<String, Object> safe = new LinkedHashMap<>();
        input.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .filter(entry -> isAllowedContextKey(entry.getKey()))
                .limit(MAX_CONTEXT_FIELDS)
                .forEach(entry -> {
                    Object value = safeContextValue(
                            entry.getValue(),
                            entry.getKey()
                    );
                    if (value != UnsafeValue.INSTANCE) {
                        safe.put(entry.getKey(), value);
                    }
                });

        return Collections.unmodifiableMap(safe);
    }

    private List<Map<String, Object>> safeEvidence(
            List<EvidenceRef> evidenceRefs
    ) {
        if (evidenceRefs.isEmpty()) {
            return List.of();
        }
        if (evidenceRefs.size() > MAX_EVIDENCE_ITEMS) {
            throw new IllegalArgumentException(
                    "APPROVAL_PAYLOAD_TOO_MUCH_EVIDENCE"
            );
        }

        TreeMap<String, Map<String, Object>> byEvidenceId = new TreeMap<>();
        for (EvidenceRef evidence : evidenceRefs) {
            Objects.requireNonNull(
                    evidence,
                    "evidenceRefs must not contain null"
            );

            String evidenceId = safeText(
                    evidence.evidenceId(),
                    "evidenceId"
            );

            if (evidence.sourceType() == null) {
                throw new IllegalArgumentException(
                        "APPROVAL_PAYLOAD_EVIDENCE_SOURCE_TYPE_REQUIRED"
                );
            }

            Map<String, Object> summary = new LinkedHashMap<>();
            summary.put("evidenceId", evidenceId);
            summary.put("sourceType", evidence.sourceType().name());
            putSafeText(summary, "sourceService", evidence.sourceService());
            putSafeText(summary, "locationLabel", evidence.locationLabel());

            if (evidence.pageStart() > 0) {
                summary.put("pageStart", evidence.pageStart());
            }
            if (evidence.pageEnd() > 0) {
                summary.put("pageEnd", evidence.pageEnd());
            }

            putSafeText(summary, "section", evidence.section());
            putSafeText(summary, "sha256", evidence.sha256());

            Map<String, Object> immutableSummary =
                    Collections.unmodifiableMap(summary);

            Map<String, Object> previous = byEvidenceId.putIfAbsent(
                    evidenceId,
                    immutableSummary
            );

            if (previous != null && !previous.equals(immutableSummary)) {
                throw new IllegalArgumentException(
                        "APPROVAL_PAYLOAD_EVIDENCE_ID_CONFLICT: " + evidenceId
                );
            }
        }

        return List.copyOf(byEvidenceId.values());
    }

    private static Map<String, String> safeCheckpointReferences(
            Map<String, String> references
    ) {
        if (references.isEmpty()) {
            return Map.of();
        }

        Map<String, String> safe = new LinkedHashMap<>();
        references.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    String name = requireSafeKey(entry.getKey());
                    String reference = safeText(
                            entry.getValue(),
                            "checkpoint reference"
                    );

                    if (!OPAQUE_CHECKPOINT_REFERENCE
                            .matcher(reference)
                            .matches()) {
                        throw new IllegalArgumentException(
                                "APPROVAL_PAYLOAD_CHECKPOINT_REFERENCE_NOT_OPAQUE"
                        );
                    }

                    if (reference.contains("://")) {
                        throw new IllegalArgumentException(
                                "APPROVAL_PAYLOAD_CHECKPOINT_REFERENCE_NOT_OPAQUE"
                        );
                    }

                    safe.put(name, reference);
                });

        return Collections.unmodifiableMap(safe);
    }

    private boolean isAllowedContextKey(String key) {
        if (key == null || key.isBlank() || isSecretKey(key)) {
            return false;
        }
        return allowedContextFields.contains(key);
    }

    private static Object safeContextValue(Object value, String field) {
        if (value == null) {
            return null;
        }
        if (value instanceof String string) {
            return safeText(string, field);
        }
        if (value instanceof Boolean
                || value instanceof Byte
                || value instanceof Short
                || value instanceof Integer
                || value instanceof Long) {
            return value;
        }
        if (value instanceof Float number) {
            return Float.isFinite(number)
                    ? number
                    : UnsafeValue.INSTANCE;
        }
        if (value instanceof Double number) {
            return Double.isFinite(number)
                    ? number
                    : UnsafeValue.INSTANCE;
        }
        if (value instanceof Instant instant) {
            return instant.toString();
        }
        if (value instanceof Enum<?> enumValue) {
            return enumValue.name();
        }
        if (value instanceof Collection<?> collection) {
            if (collection.size() > MAX_LIST_ITEMS) {
                throw new IllegalArgumentException(
                        "APPROVAL_PAYLOAD_LIST_TOO_LARGE: " + field
                );
            }

            List<Object> safeItems = new ArrayList<>();
            for (Object item : collection) {
                Object safeItem = safeContextValue(item, field);
                if (safeItem == UnsafeValue.INSTANCE
                        || safeItem instanceof Collection<?>
                        || safeItem instanceof Map<?, ?>) {
                    return UnsafeValue.INSTANCE;
                }
                safeItems.add(safeItem);
            }

            return Collections.unmodifiableList(safeItems);
        }

        // Maps, arrays, uploaded bytes and arbitrary objects are omitted.
        return UnsafeValue.INSTANCE;
    }

    private static void putSafeText(
            Map<String, Object> target,
            String key,
            String value
    ) {
        if (value != null && !value.isBlank()) {
            target.put(key, safeText(value, key));
        }
    }

    private static Set<String> normalizeAllowedFields(Set<String> fields) {
        Objects.requireNonNull(
                fields,
                "allowedContextFields must not be null"
        );

        TreeMap<String, Boolean> normalized = new TreeMap<>();
        for (String field : fields) {
            String key = requireSafeKey(field);
            if (isSecretKey(key)) {
                throw new IllegalArgumentException(
                        "APPROVAL_PAYLOAD_SECRET_FIELD_NOT_ALLOWED: " + key
                );
            }
            normalized.put(key, Boolean.TRUE);
        }

        return Set.copyOf(normalized.keySet());
    }

    private static String requireSafeKey(String key) {
        if (key == null || key.isBlank() || key.length() > 128) {
            throw new IllegalArgumentException(
                    "APPROVAL_PAYLOAD_INVALID_KEY"
            );
        }
        if (isSecretKey(key)) {
            throw new IllegalArgumentException(
                    "APPROVAL_PAYLOAD_SECRET_FIELD_NOT_ALLOWED: " + key
            );
        }
        return key;
    }

    private static boolean isSecretKey(String key) {
        String normalized = key.toLowerCase(Locale.ROOT)
                .replace('-', '_');

        return normalized.contains("password")
                || normalized.contains("passwd")
                || normalized.contains("secret")
                || normalized.contains("credential")
                || normalized.contains("authorization")
                || normalized.contains("api_key")
                || normalized.contains("access_token")
                || normalized.contains("refresh_token")
                || normalized.contains("private_key")
                || normalized.contains("inline_bytes")
                || normalized.contains("raw_upload")
                || normalized.contains("checkpoint_payload");
    }

    private static String safeText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    "APPROVAL_PAYLOAD_TEXT_REQUIRED: " + field
            );
        }

        String normalized = value.trim();
        if (normalized.length() > MAX_TEXT_LENGTH) {
            throw new IllegalArgumentException(
                    "APPROVAL_PAYLOAD_TEXT_TOO_LONG: " + field
            );
        }
        if (LIKELY_SECRET_VALUE.matcher(normalized).find()) {
            throw new IllegalArgumentException(
                    "APPROVAL_PAYLOAD_SECRET_DETECTED: " + field
            );
        }

        return normalized;
    }

    public record Request(
            String gateId,
            String missionId,
            String title,
            String reason,
            Map<String, Object> reviewContext,
            List<EvidenceRef> evidenceRefs,
            Map<String, String> checkpointReferences
    ) {
        public Request {
            gateId = requireText(gateId, "gateId");
            missionId = requireText(missionId, "missionId");
            title = requireText(title, "title");
            reason = requireText(reason, "reason");
            reviewContext = immutableMap(reviewContext);
            evidenceRefs = evidenceRefs == null
                    ? List.of()
                    : List.copyOf(evidenceRefs);
            checkpointReferences = checkpointReferences == null
                    ? Map.of()
                    : Map.copyOf(checkpointReferences);
        }
    }

    private static Map<String, Object> immutableMap(
            Map<String, Object> input
    ) {
        if (input == null || input.isEmpty()) {
            return Map.of();
        }

        List<Map.Entry<String, Object>> entries = new ArrayList<>(
                input.entrySet()
        );
        entries.sort(Comparator.comparing(entry ->
                Objects.requireNonNull(
                        entry.getKey(),
                        "reviewContext key"
                )
        ));

        Map<String, Object> copy = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : entries) {
            copy.put(entry.getKey(), entry.getValue());
        }

        return Collections.unmodifiableMap(copy);
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    "APPROVAL_PAYLOAD_INVALID_REQUEST: "
                            + field + " must not be blank"
            );
        }
        return value.trim();
    }

    private enum UnsafeValue {
        INSTANCE
    }
}