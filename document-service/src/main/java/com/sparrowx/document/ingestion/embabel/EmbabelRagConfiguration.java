package com.sparrowx.document.ingestion.embabel;

import com.embabel.agent.rag.ingestion.ChunkTransformer;
import com.embabel.agent.rag.ingestion.ContentChunker;
import com.embabel.agent.rag.ingestion.InMemoryContentChunker;
import com.embabel.agent.rag.ingestion.TikaHierarchicalContentReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EmbabelRagConfiguration {

    private static final int MAX_CHUNK_SIZE = 1_500;
    private static final int OVERLAP_SIZE = 200;
    private static final int EMBEDDING_BATCH_SIZE = 100;

    @Bean
    public TikaHierarchicalContentReader tikaHierarchicalContentReader() {
        return new TikaHierarchicalContentReader();
    }

    @Bean
    public ContentChunker embabelContentChunker() {
        ContentChunker.Config config =
                new ContentChunker.Config(
                        MAX_CHUNK_SIZE,
                        OVERLAP_SIZE,
                        EMBEDDING_BATCH_SIZE
                );

        return new InMemoryContentChunker(
                config,
                ChunkTransformer.NO_OP
        );
    }
}