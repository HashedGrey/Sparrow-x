package com.sparrowx.agentic.validation;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public final class OutputRepair {

    private static final int DEFAULT_MAX_INPUT_CHARACTERS = 1_000_000;
    private static final int DEFAULT_MAX_REPAIRS = 8;

    private final int maxInputCharacters;
    private final int maxRepairs;

    public OutputRepair() {
        this(
                DEFAULT_MAX_INPUT_CHARACTERS,
                DEFAULT_MAX_REPAIRS
        );
    }

    private OutputRepair(
            int maxInputCharacters,
            int maxRepairs
    ) {
        if (maxInputCharacters < 1 || maxRepairs < 1) {
            throw new IllegalArgumentException(
                    "output repair limits must be positive"
            );
        }

        this.maxInputCharacters = maxInputCharacters;
        this.maxRepairs = maxRepairs;
    }

    public static OutputRepair configured(
            int maxInputCharacters,
            int maxRepairs
    ) {
        return new OutputRepair(
                maxInputCharacters,
                maxRepairs
        );
    }

    public String repair(String malformedOutput) {
        return repairDetailed(malformedOutput).output();
    }

    /**
     * Applies one bounded deterministic pass. It never invokes an LLM and never
     * repeats generation.
     */
    public RepairResult repairDetailed(String malformedOutput) {
        if (malformedOutput == null) {
            throw violation("INPUT_REQUIRED");
        }

        if (malformedOutput.length() > maxInputCharacters) {
            throw violation("INPUT_TOO_LARGE");
        }

        String candidate = malformedOutput;
        List<String> repairs = new ArrayList<>();

        if (!candidate.isEmpty()
                && candidate.charAt(0) == '\ufeff') {
            candidate = candidate.substring(1);
            recordRepair(repairs, "REMOVED_UTF8_BOM");
        }

        String trimmed = candidate.trim();
        if (!trimmed.equals(candidate)) {
            candidate = trimmed;
            recordRepair(
                    repairs,
                    "TRIMMED_SURROUNDING_WHITESPACE"
            );
        }

        String withoutFence = stripSingleMarkdownFence(candidate);
        if (!withoutFence.equals(candidate)) {
            candidate = withoutFence;
            recordRepair(repairs, "REMOVED_MARKDOWN_FENCE");
        }

        JsonRange range = findSingleJsonRange(candidate);
        if (range.startInclusive() > 0
                || range.endExclusive() < candidate.length()) {
            candidate = candidate.substring(
                    range.startInclusive(),
                    range.endExclusive()
            );
            recordRepair(repairs, "EXTRACTED_JSON_VALUE");
        }

        TrailingCommaResult commaResult = removeTrailingCommas(
                candidate,
                maxRepairs - repairs.size()
        );
        candidate = commaResult.output();

        for (int index = 0;
             index < commaResult.removedCount();
             index++) {
            recordRepair(
                    repairs,
                    "REMOVED_TRAILING_COMMA"
            );
        }

        return new RepairResult(
                candidate,
                !candidate.equals(malformedOutput),
                repairs
        );
    }

    private String stripSingleMarkdownFence(String input) {
        if (!input.startsWith("```") || !input.endsWith("```")) {
            return input;
        }

        int firstLineEnd = input.indexOf('\n');
        if (firstLineEnd < 0) {
            return input;
        }

        String marker = input.substring(3, firstLineEnd).trim();
        if (!marker.isEmpty()
                && !marker.equalsIgnoreCase("json")) {
            return input;
        }

        return input.substring(
                firstLineEnd + 1,
                input.length() - 3
        ).trim();
    }

    private static JsonRange findSingleJsonRange(String input) {
        int start = -1;
        char opening = 0;

        for (int index = 0; index < input.length(); index++) {
            char current = input.charAt(index);
            if (current == '{' || current == '[') {
                start = index;
                opening = current;
                break;
            }
        }

        if (start < 0) {
            throw violation("JSON_VALUE_NOT_FOUND");
        }

        char closing = opening == '{' ? '}' : ']';
        int objectDepth = 0;
        int arrayDepth = 0;
        boolean inString = false;
        boolean escaped = false;

        for (int index = start; index < input.length(); index++) {
            char current = input.charAt(index);

            if (inString) {
                if (escaped) {
                    escaped = false;
                } else if (current == '\\') {
                    escaped = true;
                } else if (current == '"') {
                    inString = false;
                }
                continue;
            }

            if (current == '"') {
                inString = true;
            } else if (current == '{') {
                objectDepth++;
            } else if (current == '}') {
                objectDepth--;
            } else if (current == '[') {
                arrayDepth++;
            } else if (current == ']') {
                arrayDepth--;
            }

            if (objectDepth < 0 || arrayDepth < 0) {
                throw violation("UNBALANCED_JSON");
            }

            if (objectDepth == 0
                    && arrayDepth == 0
                    && current == closing) {
                return new JsonRange(start, index + 1);
            }
        }

        throw violation("UNBALANCED_JSON");
    }

    private static TrailingCommaResult removeTrailingCommas(
            String input,
            int remainingRepairs
    ) {
        StringBuilder output = new StringBuilder(input.length());
        boolean inString = false;
        boolean escaped = false;
        int removed = 0;

        for (int index = 0; index < input.length(); index++) {
            char current = input.charAt(index);

            if (inString) {
                output.append(current);

                if (escaped) {
                    escaped = false;
                } else if (current == '\\') {
                    escaped = true;
                } else if (current == '"') {
                    inString = false;
                }

                continue;
            }

            if (current == '"') {
                inString = true;
                output.append(current);
                continue;
            }

            if (current == ',') {
                int nextNonWhitespace = input.length();

                for (int lookAhead = index + 1;
                     lookAhead < input.length();
                     lookAhead++) {
                    if (!Character.isWhitespace(
                            input.charAt(lookAhead)
                    )) {
                        nextNonWhitespace = lookAhead;
                        break;
                    }
                }

                if (nextNonWhitespace < input.length()
                        && (input.charAt(nextNonWhitespace) == '}'
                        || input.charAt(nextNonWhitespace) == ']')) {
                    removed++;

                    if (removed > remainingRepairs) {
                        throw violation("REPAIR_LIMIT_EXCEEDED");
                    }

                    continue;
                }
            }

            output.append(current);
        }

        return new TrailingCommaResult(
                output.toString(),
                removed
        );
    }

    private void recordRepair(
            List<String> repairs,
            String repair
    ) {
        if (repairs.size() >= maxRepairs) {
            throw violation("REPAIR_LIMIT_EXCEEDED");
        }
        repairs.add(repair);
    }

    private static IllegalArgumentException violation(
            String code
    ) {
        return new IllegalArgumentException(
                "OUTPUT_REPAIR_" + code
        );
    }

    public record RepairResult(
            String output,
            boolean changed,
            List<String> repairs
    ) {
        public RepairResult {
            output = Objects.requireNonNull(
                    output,
                    "output must not be null"
            );
            repairs = repairs == null
                    ? List.of()
                    : List.copyOf(repairs);
        }
    }

    private record JsonRange(
            int startInclusive,
            int endExclusive
    ) {
    }

    private record TrailingCommaResult(
            String output,
            int removedCount
    ) {
    }
}