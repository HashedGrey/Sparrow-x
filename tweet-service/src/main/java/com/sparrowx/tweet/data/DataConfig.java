package com.sparrowx.tweet.data;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.repository.config.EnableCassandraRepositories;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;


@Configuration
@EnableJpaRepositories(
        basePackages = "com.sparrowx.tweet.data.postgres.repositories"
)
@EnableCassandraRepositories(
        basePackages = "com.sparrowx.tweet.data.cassandra.repositories"
)
@EntityScan(
        basePackages = {"com.sparrowx.tweet.data.postgres.entities",
        "buildingblocks"}
)

public class DataConfig {
}