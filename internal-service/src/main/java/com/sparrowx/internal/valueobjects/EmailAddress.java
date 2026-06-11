package com.sparrowx.internal.valueobjects;

import java.util.Locale;
import java.util.regex.Pattern;

public record EmailAddress(
        String value
) {
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$", Pattern.CASE_INSENSITIVE);

    public EmailAddress {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("email is required");
        }

        value = value.trim().toLowerCase(Locale.ROOT);

        if (!EMAIL_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("email is invalid");
        }
    }

    public static EmailAddress of(String value) {
        return new EmailAddress(value);
    }

    @Override
    public String toString() {
        return value;
    }
}