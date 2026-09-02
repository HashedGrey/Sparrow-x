package com.sparrowx.document.verification;

import com.sparrowx.document.domain.valueobjects.VerificationStatus;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public final class NumericComparisonEvaluator {

    private static final Pattern COMPARISON_PATTERN = Pattern.compile(
            "(?i)^\\s*(.+?)\\s+"
                    + "(?:explains?|explained|accounts?\\s+for|accounted\\s+for|is|are|was|were)"
                    + "\\s+(more|greater|higher|larger|less|lower|smaller)"
                    + "\\b.*?\\bthan\\b\\s+(.+?)[.!?]?\\s*$"
    );

    public Optional<Result> evaluate(
            String claimText,
            List<String> evidenceTexts
    ) {
        ComparisonClaim claim = parseClaim(claimText);

        if (claim == null) {
            return Optional.empty();
        }

        List<String> evidence = evidenceTexts == null
                ? List.of()
                : evidenceTexts;

        ExtractedValues left =
                extractValues(evidence, claim.leftSubject());

        ExtractedValues right =
                extractValues(evidence, claim.rightSubject());

        if (left.values().isEmpty() || right.values().isEmpty()) {
            return Optional.empty();
        }

        int actualComparison =
                compareValueSets(left.values(), right.values());

        Set<Integer> contributingIndexes = new LinkedHashSet<>();
        contributingIndexes.addAll(left.evidenceIndexes());
        contributingIndexes.addAll(right.evidenceIndexes());

        if (actualComparison == 0) {
            return Optional.of(new Result(
                    VerificationStatus.PARTIALLY_SUPPORTED,
                    0.55,
                    "Numeric evidence for the compared subjects is mixed or overlapping.",
                    List.copyOf(contributingIndexes)
            ));
        }

        if (actualComparison == claim.expectedComparison()) {
            return Optional.of(new Result(
                    VerificationStatus.SUPPORTED,
                    0.95,
                    "Numeric evidence supports the comparison claim.",
                    List.copyOf(contributingIndexes)
            ));
        }

        return Optional.of(new Result(
                VerificationStatus.CONTRADICTED,
                0.95,
                "Numeric evidence contradicts the comparison claim.",
                List.copyOf(contributingIndexes)
        ));
    }

    private ComparisonClaim parseClaim(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        Matcher matcher =
                COMPARISON_PATTERN.matcher(value.trim());

        if (!matcher.matches()) {
            return null;
        }

        String left = cleanSubject(matcher.group(1));
        String comparison = normalize(matcher.group(2));
        String right = cleanSubject(matcher.group(3));

        if (left.isBlank() || right.isBlank()) {
            return null;
        }

        int expectedComparison = switch (comparison) {
            case "more", "greater", "higher", "larger" -> 1;
            case "less", "lower", "smaller" -> -1;
            default -> 0;
        };

        if (expectedComparison == 0) {
            return null;
        }

        return new ComparisonClaim(
                left,
                right,
                expectedComparison
        );
    }

    private ExtractedValues extractValues(
            List<String> evidenceTexts,
            String subject
    ) {
        List<Double> values = new ArrayList<>();
        Set<Integer> evidenceIndexes = new LinkedHashSet<>();

        Pattern pattern = subjectPercentagePattern(subject);

        for (int index = 0; index < evidenceTexts.size(); index++) {
            String text = evidenceTexts.get(index);

            if (text == null || text.isBlank()) {
                continue;
            }

            Matcher matcher = pattern.matcher(normalize(text));

            boolean matched = false;

            while (matcher.find()) {
                values.add(Double.parseDouble(matcher.group(1)));
                matched = true;
            }

            if (matched) {
                evidenceIndexes.add(index);
            }
        }

        return new ExtractedValues(
                values,
                List.copyOf(evidenceIndexes)
        );
    }

    private Pattern subjectPercentagePattern(String subject) {
        String subjectRegex = List.of(
                        normalize(subject).split("[^a-z0-9]+")
                )
                .stream()
                .filter(token -> !token.isBlank())
                .map(Pattern::quote)
                .collect(Collectors.joining("[\\s_-]+"));

        return Pattern.compile(
                "(?i)\\b"
                        + subjectRegex
                        + "\\b.{0,100}?([0-9]+(?:\\.[0-9]+)?)\\s*%"
        );
    }

    private int compareValueSets(
            List<Double> leftValues,
            List<Double> rightValues
    ) {
        double leftMin =
                leftValues.stream()
                        .mapToDouble(Double::doubleValue)
                        .min()
                        .orElse(Double.NaN);

        double leftMax =
                leftValues.stream()
                        .mapToDouble(Double::doubleValue)
                        .max()
                        .orElse(Double.NaN);

        double rightMin =
                rightValues.stream()
                        .mapToDouble(Double::doubleValue)
                        .min()
                        .orElse(Double.NaN);

        double rightMax =
                rightValues.stream()
                        .mapToDouble(Double::doubleValue)
                        .max()
                        .orElse(Double.NaN);

        if (leftMin > rightMax) {
            return 1;
        }

        if (leftMax < rightMin) {
            return -1;
        }

        return 0;
    }

    private String cleanSubject(String value) {
        if (value == null) {
            return "";
        }

        return normalize(value)
                .replaceFirst("^(the|a|an)\\s+", "")
                .trim();
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }

        return value
                .toLowerCase(Locale.ROOT)
                .replace('-', ' ')
                .replaceAll("\\s+", " ")
                .trim();
    }

    public record Result(
            VerificationStatus status,
            double confidence,
            String explanation,
            List<Integer> evidenceIndexes
    ) {
        public Result {
            status = status == null
                    ? VerificationStatus.UNVERIFIED
                    : status;

            explanation = explanation == null
                    ? ""
                    : explanation;

            evidenceIndexes = evidenceIndexes == null
                    ? List.of()
                    : List.copyOf(evidenceIndexes);
        }
    }

    private record ComparisonClaim(
            String leftSubject,
            String rightSubject,
            int expectedComparison
    ) {
    }

    private record ExtractedValues(
            List<Double> values,
            List<Integer> evidenceIndexes
    ) {
    }
}