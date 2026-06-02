package com.sparrowx.document.ingestion.indexing;

import com.sparrowx.document.config.EmbeddingConfig;
import com.sparrowx.document.exceptions.DocumentIndexingException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
@ConditionalOnProperty(
        prefix = "sparrowx.document.embedding",
        name = "provider",
        havingValue = "gemini"
)
public class GeminiEmbeddingService implements EmbeddingService {

    private final RestClient restClient;
    private final EmbeddingConfig.EmbeddingProperties properties;

    public GeminiEmbeddingService(
            EmbeddingConfig.EmbeddingProperties properties
    ) {
        this.properties = properties;
        if (properties.apiKey() == null || properties.apiKey().isBlank()) {
            throw new IllegalStateException("Gemini API key is blank");
        }

        if (properties.apiKey().contains("GEMINI_API_KEY")) {
            throw new IllegalStateException(
                    "GEMINI_API_KEY was not resolved. Check IntelliJ/Maven run configuration environment variables."
            );
        }
        this.restClient = RestClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com/v1beta")
                .build();
    }

    @Override
    public List<Float> embedDocument(String text) {
        return embed(text, "RETRIEVAL_DOCUMENT");
    }

    @Override
    public List<Float> embedQuery(String text) {
        return embed(text, "RETRIEVAL_QUERY");
    }

    private List<Float> embed(String text, String taskType) {
        if (text == null || text.isBlank()) {
            throw new DocumentIndexingException(
                    "Cannot embed blank text",
                    null
            );
        }

        try {
            Map<String, Object> body = Map.of(
                    "model", "models/" + properties.model(),
                    "content", Map.of(
                            "parts", List.of(
                                    Map.of("text", text)
                            )
                    ),
                    "taskType", taskType,
                    "outputDimensionality", properties.dimension()
            );

            GeminiEmbeddingResponse response = restClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/models/{model}:embedContent")
                            .queryParam("key", properties.apiKey())
                            .build(properties.model()))
                    .body(body)
                    .retrieve()
                    .body(GeminiEmbeddingResponse.class);

            if (response == null
                    || response.embedding() == null
                    || response.embedding().values() == null
                    || response.embedding().values().isEmpty()) {
                throw new DocumentIndexingException(
                        "Gemini embedding response was empty",
                        null
                );
            }

            return response.embedding().values();

        } catch (RuntimeException exception) {
            throw new DocumentIndexingException(
                    "Gemini embedding request failed",
                    exception
            );
        }
    }

    public record GeminiEmbeddingResponse(
            GeminiEmbedding embedding
    ) {
    }

    public record GeminiEmbedding(
            List<Float> values
    ) {
    }
}