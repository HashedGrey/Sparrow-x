package com.sparrowx.agentic.actions.synthesis;

import com.embabel.agent.api.annotation.Action;
import com.sparrowx.agentic.mission.evidence.Citation;
import com.sparrowx.agentic.mission.evidence.EvidenceRef;
import com.sparrowx.agentic.mission.evidence.EvidenceRegistry;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class BuildCitationsAction {

    @Action
    public Result execute(BuildSpec spec) {
        Objects.requireNonNull(spec, "spec must not be null");

        EvidenceRegistry registry = new EvidenceRegistry();

        spec.evidenceRefs().stream()
                .sorted(
                        Comparator
                                .comparing(
                                        BuildCitationsAction::stableEvidenceIdentity
                                )
                                .thenComparing(
                                        evidence -> nullToEmpty(
                                                evidence.evidenceId()
                                        )
                                )
                )
                .forEach(registry::register);

        List<EvidenceRef> evidence = registry.snapshot().stream()
                .sorted(Comparator.comparing(EvidenceRef::evidenceId))
                .toList();

        List<Citation> citations =
                java.util.stream.IntStream.range(0, evidence.size())
                        .mapToObj(index -> {
                            EvidenceRef ref = evidence.get(index);

                            if (ref.evidenceId() == null
                                    || ref.evidenceId().isBlank()) {
                                throw new IllegalStateException(
                                        "registered evidence must have a stable evidenceId"
                                );
                            }

                            return new Citation(
                                    citationId(ref.evidenceId()),
                                    "[" + (index + 1) + "]",
                                    ref.evidenceId(),
                                    spec.excerptsByEvidenceId()
                                            .getOrDefault(ref.evidenceId(), "")
                            );
                        })
                        .toList();

        return new Result(citations, evidence);
    }

    private static String citationId(String evidenceId) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(evidenceId.getBytes(StandardCharsets.UTF_8));

            StringBuilder hex = new StringBuilder(24);
            for (int index = 0; index < 12; index++) {
                hex.append("%02x".formatted(digest[index] & 0xff));
            }

            return "citation-" + hex;
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "SHA-256 is unavailable",
                    exception
            );
        }
    }

    private static String stableEvidenceIdentity(EvidenceRef evidence) {
        Objects.requireNonNull(
                evidence,
                "evidenceRefs must not contain null"
        );

        return String.join(
                "\u001f",
                evidence.sourceType().name(),
                nullToEmpty(evidence.sourceService()),
                nullToEmpty(evidence.sourceId()),
                nullToEmpty(evidence.sourceUri()),
                nullToEmpty(evidence.artifactId()),
                nullToEmpty(evidence.objectId()),
                nullToEmpty(evidence.parentObjectId()),
                nullToEmpty(evidence.locationLabel()),
                Integer.toString(evidence.pageStart()),
                Integer.toString(evidence.pageEnd()),
                nullToEmpty(evidence.section()),
                nullToEmpty(evidence.chunkId()),
                nullToEmpty(evidence.sha256())
        );
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    public record BuildSpec(
            List<EvidenceRef> evidenceRefs,
            Map<String, String> excerptsByEvidenceId
    ) {
        public BuildSpec {
            evidenceRefs = evidenceRefs == null
                    ? List.of()
                    : List.copyOf(evidenceRefs);
            excerptsByEvidenceId =
                    immutableExcerpts(excerptsByEvidenceId);
        }
    }

    public record Result(
            List<Citation> citations,
            List<EvidenceRef> evidenceRefs
    ) {
        public Result {
            citations = citations == null
                    ? List.of()
                    : List.copyOf(citations);
            evidenceRefs = evidenceRefs == null
                    ? List.of()
                    : List.copyOf(evidenceRefs);
        }
    }

    private static Map<String, String> immutableExcerpts(
            Map<String, String> values
    ) {
        if (values == null || values.isEmpty()) {
            return Map.of();
        }

        Map<String, String> result = new LinkedHashMap<>();

        values.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    String evidenceId = Objects.requireNonNull(
                            entry.getKey(),
                            "excerpt evidenceId must not be null"
                    );
                    result.put(
                            evidenceId,
                            nullToEmpty(entry.getValue())
                    );
                });

        return Map.copyOf(result);
    }
}