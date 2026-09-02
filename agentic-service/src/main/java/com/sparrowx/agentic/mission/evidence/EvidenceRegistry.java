package com.sparrowx.agentic.mission.evidence;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Deduplicates evidence and assigns stable evidence identifiers.
 */
public final class EvidenceRegistry {

    private static final char IDENTITY_SEPARATOR = '\u001f';

    private final Map<String, EvidenceRef> evidenceById = new LinkedHashMap<>();
    private final Map<String, String> evidenceIdByIdentity = new LinkedHashMap<>();

    public EvidenceRef register(EvidenceRef candidate) {
        Objects.requireNonNull(candidate, "candidate");

        String identity = identityOf(candidate);
        String existingId = evidenceIdByIdentity.get(identity);
        if (existingId != null) {
            return evidenceById.get(existingId);
        }

        String evidenceId = candidate.evidenceId().isBlank()
                ? stableEvidenceId(identity)
                : candidate.evidenceId();

        EvidenceRef conflicting = evidenceById.get(evidenceId);
        if (conflicting != null) {
            throw new IllegalArgumentException(
                    "Evidence id already belongs to different provenance: " + evidenceId
            );
        }

        EvidenceRef registered = candidate.evidenceId().equals(evidenceId)
                ? candidate
                : candidate.withEvidenceId(evidenceId);

        evidenceById.put(evidenceId, registered);
        evidenceIdByIdentity.put(identity, evidenceId);
        return registered;
    }

    public Optional<EvidenceRef> findById(String evidenceId) {
        return Optional.ofNullable(evidenceById.get(evidenceId));
    }

    public List<EvidenceRef> snapshot() {
        return List.copyOf(evidenceById.values());
    }

    public int size() {
        return evidenceById.size();
    }

    private static String identityOf(EvidenceRef evidence) {
        if (evidence.sourceType() == EvidenceSourceType.INTERNAL_ENTITY) {
            String entityId = firstNonBlank(evidence.sourceId(), evidence.objectId());

            if (!entityId.isBlank()) {
                return String.join(
                        String.valueOf(IDENTITY_SEPARATOR),
                        evidence.sourceType().name(),
                        evidence.sourceService(),
                        entityId
                );
            }
        }

        return String.join(
                String.valueOf(IDENTITY_SEPARATOR),
                evidence.sourceType().name(),
                evidence.sourceService(),
                evidence.sourceId(),
                evidence.artifactId(),
                evidence.objectId(),
                evidence.parentObjectId(),
                evidence.chunkId(),
                Integer.toString(evidence.pageStart()),
                Integer.toString(evidence.pageEnd()),
                evidence.section(),
                evidence.sha256()
        );
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }

        return "";
    }

    private static String stableEvidenceId(String identity) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(identity.getBytes(StandardCharsets.UTF_8));
            return "ev_" + HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }
}