package com.sparrowx.document.dice.gemini;

import org.springframework.stereotype.Component;

@Component
public class GeminiJsonExtractor {

    public String extractJsonObject(String rawText) {
        if (rawText == null || rawText.isBlank()) {
            return "";
        }

        String text = stripMarkdownFence(rawText.trim());

        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');

        if (start < 0 || end < 0 || end <= start) {
            return text;
        }

        return text.substring(start, end + 1);
    }

    public String repairTruncatedJsonObject(String rawText) {
        String json = extractJsonObject(rawText);

        if (json == null || json.isBlank()) {
            return "";
        }

        int openBraces = 0;
        int openBrackets = 0;
        boolean inString = false;
        boolean escaped = false;

        StringBuilder repaired = new StringBuilder();

        for (int index = 0; index < json.length(); index++) {
            char current = json.charAt(index);
            repaired.append(current);

            if (escaped) {
                escaped = false;
                continue;
            }

            if (current == '\\') {
                escaped = true;
                continue;
            }

            if (current == '"') {
                inString = !inString;
                continue;
            }

            if (!inString) {
                if (current == '{') {
                    openBraces++;
                } else if (current == '}') {
                    openBraces = Math.max(0, openBraces - 1);
                } else if (current == '[') {
                    openBrackets++;
                } else if (current == ']') {
                    openBrackets = Math.max(0, openBrackets - 1);
                }
            }
        }

        if (inString) {
            repaired.append('"');
        }

        while (openBrackets > 0) {
            repaired.append(']');
            openBrackets--;
        }

        while (openBraces > 0) {
            repaired.append('}');
            openBraces--;
        }

        return repaired.toString();
    }

    private String stripMarkdownFence(String text) {
        if (text.startsWith("```json")) {
            return text.substring("```json".length()).replaceFirst("```$", "").trim();
        }

        if (text.startsWith("```")) {
            return text.substring("```".length()).replaceFirst("```$", "").trim();
        }

        return text;
    }
}