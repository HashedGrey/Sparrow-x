package com.sparrowx.document.data.qdrant;

import com.sparrowx.document.config.QdrantProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Configuration
public class QdrantCollectionInitializer {

    private static final Logger log =
            LoggerFactory.getLogger(QdrantCollectionInitializer.class);

    @Bean
    ApplicationRunner ensureQdrantCollection(
            QdrantProperties properties,
            RestTemplate qdrantRestTemplate
    ) {
        return args -> {
            if (!properties.enabled()) {
                log.info("Qdrant is disabled. Skipping collection initialization.");
                return;
            }

            String collectionUrl = properties.url()
                    + "/collections/"
                    + properties.collectionName();

            if (collectionExists(collectionUrl, qdrantRestTemplate)) {
                log.info(
                        "Qdrant collection already exists: {}",
                        properties.collectionName()
                );
                return;
            }

            Map<String, Object> body = Map.of(
                    "vectors", Map.of(
                            "size", properties.vectorDimension(),
                            "distance", "Cosine"
                    )
            );

            try {
                qdrantRestTemplate.put(collectionUrl, body);

                log.info(
                        "Created Qdrant collection: {} with vector dimension {}",
                        properties.collectionName(),
                        properties.vectorDimension()
                );
            } catch (HttpClientErrorException.Conflict ex) {
                // Another app instance or previous startup may have created it.
                log.info(
                        "Qdrant collection already exists after create attempt: {}",
                        properties.collectionName()
                );
            }
        };
    }

    private boolean collectionExists(
            String collectionUrl,
            RestTemplate qdrantRestTemplate
    ) {
        try {
            qdrantRestTemplate.getForObject(collectionUrl, Map.class);
            return true;
        } catch (HttpClientErrorException.NotFound ex) {
            return false;
        }
    }
}