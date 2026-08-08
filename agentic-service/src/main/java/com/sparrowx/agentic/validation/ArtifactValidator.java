package com.sparrowx.agentic.validation;

import com.sparrowx.agentic.mission.artifact.InputArtifact;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

@Component
public final class ArtifactValidator {

    private static final Pattern SHA_256 =
            Pattern.compile("[0-9a-fA-F]{64}");

    private static final Pattern METADATA_KEY = Pattern.compile(
            "[A-Za-z0-9][A-Za-z0-9._-]{0,127}"
    );

    private static final Set<String> DECLARED_SIZE_KEYS = Set.of(
            "content_length",
            "content_length_bytes",
            "size_bytes"
    );

    private final Limits limits;

    public ArtifactValidator() {
        this(Limits.defaults());
    }

    private ArtifactValidator(Limits limits) {
        this.limits = Objects.requireNonNull(
                limits,
                "limits must not be null"
        );
    }

    public static ArtifactValidator configured(Limits limits) {
        return new ArtifactValidator(limits);
    }

    public void validate(InputArtifact artifact) {
        if (artifact == null) {
            throw violation(
                    "REQUIRED",
                    "artifact must not be null"
            );
        }

        requireText(artifact.artifactId(), "artifact_id");

        if (artifact.type() == null
                || artifact.type().name().equals("UNSPECIFIED")) {
            throw violation(
                    "TYPE_REQUIRED",
                    "type must not be unspecified"
            );
        }

        if (artifact.contentMode() == null
                || artifact.contentMode().name().equals("UNSPECIFIED")) {
            throw violation(
                    "CONTENT_MODE_REQUIRED",
                    "content mode must be specified"
            );
        }

        boolean hasObjectUri = hasText(artifact.objectUri());
        byte[] inlineBytes = artifact.inlineBytes();
        boolean hasInlineBytes =
                inlineBytes != null && inlineBytes.length > 0;
        boolean hasExternalUri = hasText(artifact.externalUri());
        boolean hasInlineText = hasText(artifact.inlineText());

        int populatedModes = booleanCount(
                hasObjectUri,
                hasInlineBytes,
                hasExternalUri,
                hasInlineText
        );

        if (populatedModes != 1) {
            throw violation(
                    "CONTENT_MODE_COUNT",
                    "exactly one content value must be populated"
            );
        }

        String actualMode = hasObjectUri
                ? "OBJECT_URI"
                : hasInlineBytes
                ? "INLINE_BYTES"
                : hasExternalUri
                ? "EXTERNAL_URI"
                : "INLINE_TEXT";

        if (!artifact.contentMode().name().equals(actualMode)) {
            throw violation(
                    "CONTENT_MODE_MISMATCH",
                    "declared mode " + artifact.contentMode().name()
                            + " does not match " + actualMode
            );
        }

        String hash = requireHash(artifact.sha256());

        switch (actualMode) {
            case "OBJECT_URI" ->
                    validateObjectUri(artifact.objectUri());
            case "INLINE_BYTES" ->
                    validateInlineBytes(inlineBytes, hash);
            case "EXTERNAL_URI" ->
                    validateExternalUri(artifact.externalUri());
            case "INLINE_TEXT" ->
                    validateInlineText(artifact.inlineText(), hash);
            default -> throw new IllegalStateException(
                    "ARTIFACT_UNREACHABLE_CONTENT_MODE"
            );
        }

        validateOptionalText(
                artifact.filename(),
                "filename",
                limits.maxFilenameLength()
        );
        validateOptionalText(
                artifact.contentType(),
                "content_type",
                limits.maxContentTypeLength()
        );
        validateMetadata(artifact.metadata());
    }

    /**
     * Use at the boundary that constructs Temporal workflow input. Submission
     * validation may accept bytes long enough to externalize them first.
     */
    public void validateWorkflowInput(InputArtifact artifact) {
        validate(artifact);

        if (artifact.contentMode().name().equals("INLINE_BYTES")) {
            throw violation(
                    "RAW_BYTES_IN_WORKFLOW_INPUT",
                    "inline bytes must be externalized "
                            + "and replaced by a reference"
            );
        }
    }

    private void validateInlineBytes(
            byte[] bytes,
            String expectedHash
    ) {
        if (bytes.length > limits.maxInlineBytes()) {
            throw violation(
                    "INLINE_BYTES_TOO_LARGE",
                    "inline byte payload exceeds "
                            + limits.maxInlineBytes()
            );
        }

        requireMatchingHash(bytes, expectedHash);
    }

    private void validateInlineText(
            String text,
            String expectedHash
    ) {
        byte[] utf8 = text.getBytes(StandardCharsets.UTF_8);

        if (utf8.length > limits.maxInlineTextBytes()) {
            throw violation(
                    "INLINE_TEXT_TOO_LARGE",
                    "UTF-8 text exceeds "
                            + limits.maxInlineTextBytes()
            );
        }

        requireMatchingHash(utf8, expectedHash);
    }

    private void validateObjectUri(String value) {
        URI uri = parseAbsoluteUri(value, "object_uri");
        String scheme = uri.getScheme().toLowerCase(Locale.ROOT);

        if (!limits.allowedObjectSchemes().contains(scheme)) {
            throw violation(
                    "OBJECT_URI_SCHEME",
                    "unsupported object URI scheme: " + scheme
            );
        }

        if (uri.getRawQuery() != null
                || uri.getRawFragment() != null) {
            throw violation(
                    "OBJECT_URI_NOT_CANONICAL",
                    "object URI must not contain query or fragment data"
            );
        }
    }

    private void validateExternalUri(String value) {
        URI uri = parseAbsoluteUri(value, "external_uri");
        String scheme = uri.getScheme().toLowerCase(Locale.ROOT);

        if (!limits.allowedExternalSchemes().contains(scheme)) {
            throw violation(
                    "EXTERNAL_URI_SCHEME",
                    "unsupported external URI scheme: " + scheme
            );
        }
    }

    private static URI parseAbsoluteUri(
            String value,
            String field
    ) {
        try {
            URI uri = new URI(requireText(value, field));

            if (!uri.isAbsolute() || uri.getScheme() == null) {
                throw violation(
                        "URI_NOT_ABSOLUTE",
                        field + " must be absolute"
                );
            }

            if (uri.getUserInfo() != null) {
                throw violation(
                        "URI_CONTAINS_USER_INFO",
                        field + " must not contain embedded credentials"
                );
            }

            return uri;
        } catch (URISyntaxException exception) {
            throw violation(
                    "URI_INVALID",
                    field + " is not a valid URI"
            );
        }
    }

    private void validateMetadata(Map<String, String> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return;
        }

        if (metadata.size() > limits.maxMetadataEntries()) {
            throw violation(
                    "METADATA_TOO_LARGE",
                    "too many metadata entries"
            );
        }

        for (Map.Entry<String, String> entry : metadata.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            if (key == null || !METADATA_KEY.matcher(key).matches()) {
                throw violation(
                        "METADATA_KEY_INVALID",
                        "invalid metadata key"
                );
            }

            if (value == null
                    || value.length() > limits.maxMetadataValueLength()) {
                throw violation(
                        "METADATA_VALUE_INVALID",
                        "invalid metadata value for " + key
                );
            }

            if (DECLARED_SIZE_KEYS.contains(
                    key.toLowerCase(Locale.ROOT)
            )) {
                validateDeclaredSize(value, key);
            }
        }
    }

    private void validateDeclaredSize(String value, String key) {
        try {
            long size = Long.parseLong(value);

            if (size < 0L || size > limits.maxReferencedBytes()) {
                throw violation(
                        "DECLARED_SIZE_OUT_OF_RANGE",
                        key + " exceeds the artifact size bound"
                );
            }
        } catch (NumberFormatException exception) {
            throw violation(
                    "DECLARED_SIZE_INVALID",
                    key + " must be an integer"
            );
        }
    }

    private static String requireHash(String hash) {
        if (hash == null || !SHA_256.matcher(hash).matches()) {
            throw violation(
                    "SHA256_INVALID",
                    "sha256 must contain exactly "
                            + "64 hexadecimal characters"
            );
        }

        return hash.toLowerCase(Locale.ROOT);
    }

    private static void requireMatchingHash(
            byte[] content,
            String expectedHash
    ) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(content);
            String actualHash = HexFormat.of().formatHex(digest);

            if (!MessageDigest.isEqual(
                    actualHash.getBytes(StandardCharsets.US_ASCII),
                    expectedHash.getBytes(StandardCharsets.US_ASCII)
            )) {
                throw violation(
                        "SHA256_MISMATCH",
                        "sha256 does not match inline content"
                );
            }
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "SHA-256 is unavailable",
                    exception
            );
        }
    }

    private static int booleanCount(boolean... values) {
        int count = 0;
        for (boolean value : values) {
            if (value) {
                count++;
            }
        }
        return count;
    }

    private static void validateOptionalText(
            String value,
            String field,
            int maximumLength
    ) {
        if (value != null && value.length() > maximumLength) {
            throw violation(
                    "FIELD_TOO_LONG",
                    field + " exceeds "
                            + maximumLength + " characters"
            );
        }
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private static String requireText(
            String value,
            String field
    ) {
        if (!hasText(value)) {
            throw violation(
                    "FIELD_REQUIRED",
                    field + " must not be blank"
            );
        }
        return value.trim();
    }

    private static IllegalArgumentException violation(
            String code,
            String detail
    ) {
        return new IllegalArgumentException(
                "ARTIFACT_" + code + ": " + detail
        );
    }

    public record Limits(
            int maxInlineBytes,
            int maxInlineTextBytes,
            long maxReferencedBytes,
            int maxFilenameLength,
            int maxContentTypeLength,
            int maxMetadataEntries,
            int maxMetadataValueLength,
            Set<String> allowedObjectSchemes,
            Set<String> allowedExternalSchemes
    ) {
        public Limits {
            if (maxInlineBytes < 1
                    || maxInlineTextBytes < 1
                    || maxReferencedBytes < 1L
                    || maxFilenameLength < 1
                    || maxContentTypeLength < 1
                    || maxMetadataEntries < 1
                    || maxMetadataValueLength < 1) {
                throw new IllegalArgumentException(
                        "artifact validation limits must be positive"
                );
            }

            allowedObjectSchemes = normalizeSchemes(
                    allowedObjectSchemes,
                    "allowedObjectSchemes"
            );
            allowedExternalSchemes = normalizeSchemes(
                    allowedExternalSchemes,
                    "allowedExternalSchemes"
            );
        }

        public static Limits defaults() {
            return new Limits(
                    8 * 1024 * 1024,
                    512 * 1024,
                    5L * 1024L * 1024L * 1024L,
                    512,
                    255,
                    64,
                    2_048,
                    Set.of("s3", "gs", "https"),
                    Set.of("https")
            );
        }

        private static Set<String> normalizeSchemes(
                Set<String> schemes,
                String field
        ) {
            Objects.requireNonNull(
                    schemes,
                    field + " must not be null"
            );

            Set<String> normalized = schemes.stream()
                    .map(value -> Objects.requireNonNull(
                            value,
                            field + " must not contain null"
                    ))
                    .map(value -> value
                            .toLowerCase(Locale.ROOT)
                            .trim())
                    .filter(value -> !value.isEmpty())
                    .collect(
                            java.util.stream.Collectors.toUnmodifiableSet()
                    );

            if (normalized.isEmpty()) {
                throw new IllegalArgumentException(
                        field + " must not be empty"
                );
            }

            return normalized;
        }
    }
}