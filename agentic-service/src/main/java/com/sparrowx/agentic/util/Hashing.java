package com.sparrowx.agentic.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

/**
 * Stable hashing for snapshots, dedupe keys and replay integrity.
 */
public final class Hashing {

    private static final char PART_SEPARATOR = '\u001f';

    private Hashing() {
    }

    public static String sha256Hex(byte[] value) {
        Objects.requireNonNull(value, "value must not be null");

        try {
            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            return toHex(digest.digest(value));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "SHA-256 is unavailable",
                    exception
            );
        }
    }

    public static String sha256Hex(String value) {
        Objects.requireNonNull(value, "value must not be null");

        return sha256Hex(
                value.getBytes(StandardCharsets.UTF_8)
        );
    }

    public static String fingerprint(String... parts) {
        Objects.requireNonNull(parts, "parts must not be null");

        StringBuilder canonical = new StringBuilder();

        for (int i = 0; i < parts.length; i++) {
            if (i > 0) {
                canonical.append(PART_SEPARATOR);
            }

            canonical.append(
                    Objects.requireNonNull(
                            parts[i],
                            "parts[" + i + "] must not be null"
                    )
            );
        }

        return sha256Hex(canonical.toString());
    }

    private static String toHex(byte[] bytes) {
        StringBuilder result =
                new StringBuilder(bytes.length * 2);

        for (byte value : bytes) {
            result.append(
                    Character.forDigit(
                            (value >>> 4) & 0x0f,
                            16
                    )
            );
            result.append(
                    Character.forDigit(
                            value & 0x0f,
                            16
                    )
            );
        }

        return result.toString();
    }
}