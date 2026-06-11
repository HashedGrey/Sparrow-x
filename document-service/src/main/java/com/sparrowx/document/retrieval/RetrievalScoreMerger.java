package com.sparrowx.document.retrieval;

import org.springframework.stereotype.Component;

@Component
public class RetrievalScoreMerger {

    private static final double DEFAULT_KEYWORD_WEIGHT = 0.45;
    private static final double DEFAULT_VECTOR_WEIGHT = 0.55;

    public double merge(double keywordScore, double vectorScore) {
        return merge(
                keywordScore,
                vectorScore,
                DEFAULT_KEYWORD_WEIGHT,
                DEFAULT_VECTOR_WEIGHT
        );
    }

    public double merge(
            double keywordScore,
            double vectorScore,
            double keywordWeight,
            double vectorWeight
    ) {
        double safeKeywordScore = clamp(keywordScore);
        double safeVectorScore = clamp(vectorScore);

        double safeKeywordWeight = Math.max(0.0, keywordWeight);
        double safeVectorWeight = Math.max(0.0, vectorWeight);

        double totalWeight = safeKeywordWeight + safeVectorWeight;
        if (totalWeight == 0.0) {
            return 0.0;
        }

        return ((safeKeywordScore * safeKeywordWeight)
                + (safeVectorScore * safeVectorWeight)) / totalWeight;
    }

    private double clamp(double score) {
        if (Double.isNaN(score) || Double.isInfinite(score)) {
            return 0.0;
        }

        if (score < 0.0) {
            return 0.0;
        }

        if (score > 1.0) {
            return 1.0;
        }

        return score;
    }
}