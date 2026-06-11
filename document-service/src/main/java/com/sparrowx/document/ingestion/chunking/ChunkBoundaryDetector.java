package com.sparrowx.document.ingestion.chunking;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ChunkBoundaryDetector {

    private static final int DEFAULT_MAX_CHARS_PER_CHUNK = 1_500;
    private static final int MIN_SOFT_BOUNDARY_DISTANCE = 300;

    public List<ChunkBoundary> detect(
            String text,
            int maxCharsPerChunk,
            int overlapChars
    ) {
        if (text == null || text.isBlank()) {
            return List.of();
        }

        int safeMaxChars = maxCharsPerChunk <= 0
                ? DEFAULT_MAX_CHARS_PER_CHUNK
                : maxCharsPerChunk;

        int safeOverlap = Math.max(
                0,
                Math.min(overlapChars, safeMaxChars / 2)
        );

        List<ChunkBoundary> boundaries = new ArrayList<>();

        int start = skipLeadingWhitespace(text, 0);

        while (start < text.length()) {
            int hardEnd = Math.min(start + safeMaxChars, text.length());
            int end = findSoftEndBoundary(text, start, hardEnd);

            if (end <= start) {
                end = hardEnd;
            }

            end = trimTrailingWhitespace(text, end);

            if (end <= start) {
                end = hardEnd;
            }

            boundaries.add(new ChunkBoundary(start, end));

            if (end >= text.length()) {
                break;
            }

            int nextStart = Math.max(0, end - safeOverlap);
            nextStart = moveToSafeStartBoundary(text, nextStart, end);
            nextStart = skipLeadingWhitespace(text, nextStart);

            if (nextStart <= start) {
                nextStart = skipLeadingWhitespace(text, end);
            }

            start = nextStart;
        }

        return boundaries;
    }

    private int findSoftEndBoundary(
            String text,
            int start,
            int hardEnd
    ) {
        int minimumEnd = Math.min(
                hardEnd,
                start + MIN_SOFT_BOUNDARY_DISTANCE
        );

        for (int i = hardEnd - 1; i >= minimumEnd; i--) {
            char character = text.charAt(i);

            if (isSentenceBoundary(character)) {
                return i + 1;
            }
        }

        for (int i = hardEnd - 1; i >= minimumEnd; i--) {
            char character = text.charAt(i);

            if (Character.isWhitespace(character)) {
                return i;
            }
        }

        return hardEnd;
    }

    private int moveToSafeStartBoundary(
            String text,
            int proposedStart,
            int previousEnd
    ) {
        if (proposedStart <= 0 || proposedStart >= text.length()) {
            return Math.max(0, Math.min(proposedStart, text.length()));
        }

        if (Character.isWhitespace(text.charAt(proposedStart))) {
            return proposedStart;
        }

        if (isSentenceStart(text, proposedStart)) {
            return proposedStart;
        }

        int forwardLimit = Math.min(previousEnd, proposedStart + 120);

        for (int i = proposedStart; i < forwardLimit; i++) {
            char character = text.charAt(i);

            if (Character.isWhitespace(character)) {
                return i + 1;
            }

            if (isSentenceBoundary(character)) {
                return i + 1;
            }
        }

        int backwardLimit = Math.max(0, proposedStart - 120);

        for (int i = proposedStart; i > backwardLimit; i--) {
            char character = text.charAt(i);

            if (Character.isWhitespace(character)) {
                return i + 1;
            }

            if (isSentenceBoundary(character)) {
                return i + 1;
            }
        }

        return proposedStart;
    }

    private boolean isSentenceStart(String text, int index) {
        if (index <= 0 || index >= text.length()) {
            return false;
        }

        char previous = text.charAt(index - 1);
        return Character.isWhitespace(previous) || isSentenceBoundary(previous);
    }

    private boolean isSentenceBoundary(char character) {
        return character == '.'
                || character == '\n'
                || character == '?'
                || character == '!';
    }

    private int skipLeadingWhitespace(String text, int index) {
        int current = Math.max(0, index);

        while (current < text.length()
                && Character.isWhitespace(text.charAt(current))) {
            current++;
        }

        return current;
    }

    private int trimTrailingWhitespace(String text, int endExclusive) {
        int current = Math.min(endExclusive, text.length());

        while (current > 0
                && Character.isWhitespace(text.charAt(current - 1))) {
            current--;
        }

        return current;
    }

    public record ChunkBoundary(
            int startInclusive,
            int endExclusive
    ) {
    }
}