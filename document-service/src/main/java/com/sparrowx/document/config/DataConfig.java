package com.sparrowx.document.config;

import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = {
        "com.sparrowx.document.data.postgres.repositories",
        "buildingblocks.infrastructure.messaging.inbox",
        "buildingblocks.infrastructure.persistence.outbox"
})
@EntityScan(basePackages = {
        "com.sparrowx.document.data.postgres.entities",
        "buildingblocks.infrastructure.messaging.inbox",
        "buildingblocks.infrastructure.persistence.outbox"
})
public class DataConfig {
}