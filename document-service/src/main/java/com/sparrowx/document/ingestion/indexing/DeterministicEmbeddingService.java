package com.sparrowx.document.ingestion.indexing;

import com.sparrowx.document.exceptions.DocumentIndexingException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;

@Component
@ConditionalOnProperty(
        prefix = "sparrowx.document.embedding",
        name = "provider",
        havingValue = "deterministic",
        matchIfMissing = true
)
public class DeterministicEmbeddingService implements EmbeddingService {

    private static final int VECTOR_DIMENSION = 384;

    @Override
    public List<Float> embedDocument(String text) {
        return embed(text);
    }

    @Override
    public List<Float> embedQuery(String text) {
        return embed(text);
    }

    @Override
    public List<List<Float>> embedDocuments(List<String> texts) {
        if (texts == null || texts.isEmpty()) {
            return List.of();
        }

        return texts.stream()
                .map(this::embedDocument)
                .toList();
    }

    private List<Float> embed(String text) {
        if (text == null || text.isBlank()) {
            return zeroVector();
        }

        try {
            List<Float> vector = new ArrayList<>(VECTOR_DIMENSION);
            String seed = sha256(text);

            for (int i = 0; i < VECTOR_DIMENSION; i++) {
                int charIndex = i % seed.length();
                int value = Character.digit(seed.charAt(charIndex), 16);
                vector.add(value / 15.0f);
            }

            return vector;
        } catch (RuntimeException exception) {
            throw new DocumentIndexingException(
                    "Failed to create deterministic placeholder embedding",
                    exception
            );
        }
    }

    private List<Float> zeroVector() {
        List<Float> vector = new ArrayList<>(VECTOR_DIMENSION);

        for (int i = 0; i < VECTOR_DIMENSION; i++) {
            vector.add(0.0f);
        }

        return vector;
    }

    private String sha256(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(text.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "SHA-256 algorithm is not available",
                    exception
            );
        }
    }
}