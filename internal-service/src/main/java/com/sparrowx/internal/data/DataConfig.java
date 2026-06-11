package com.sparrowx.internal.data;

import jakarta.annotation.PreDestroy;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class DataConfig {

    private Driver neo4jDriver;

    @Bean
    public Driver neo4jDriver(
            @Value("${sparrowx.internal.neo4j.uri:bolt://localhost:7687}")
            String uri,
            @Value("${sparrowx.internal.neo4j.username:neo4j}")
            String username,
            @Value("${sparrowx.internal.neo4j.password:password}")
            String password
    ) {
        this.neo4jDriver = GraphDatabase.driver(
                uri,
                AuthTokens.basic(username, password),
                org.neo4j.driver.Config.builder()
                        .withConnectionTimeout(5, TimeUnit.SECONDS)
                        .withMaxConnectionPoolSize(20)
                        .withConnectionAcquisitionTimeout(5, TimeUnit.SECONDS)
                        .build()
        );

        return this.neo4jDriver;
    }

    @PreDestroy
    public void closeNeo4jDriver() {
        if (neo4jDriver != null) {
            neo4jDriver.close();
        }
    }
}