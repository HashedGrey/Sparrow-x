package com.sparrowx.agentic.mission.evidence;

/**
 * User-facing citation linked to registered evidence.
 */
public record Citation(
        String citationId,
        String label,
        String evidenceId,
        String excerpt
) {

    public Citation {
        citationId = nullToEmpty(citationId);
        label = nullToEmpty(label);
        evidenceId = nullToEmpty(evidenceId);
        excerpt = nullToEmpty(excerpt);
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}