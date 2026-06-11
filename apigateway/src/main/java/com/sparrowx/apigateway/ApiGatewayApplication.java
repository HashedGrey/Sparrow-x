package com.sparrowx.apigateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(
		scanBasePackages = {
				"com.sparrowx.apigateway",
				"buildingblocks.core.queries",
				"buildingblocks.core.observability",
				"buildingblocks.infrastructure.observability",
		},
		exclude = {
				org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class,
				org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration.class,
				org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration.class,
				org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration.class,
				org.springframework.boot.autoconfigure.cassandra.CassandraAutoConfiguration.class,
				org.springframework.boot.autoconfigure.data.cassandra.CassandraDataAutoConfiguration.class,
				org.springframework.boot.autoconfigure.data.cassandra.CassandraRepositoriesAutoConfiguration.class,
				org.springframework.boot.autoconfigure.data.neo4j.Neo4jDataAutoConfiguration.class,
				org.springframework.boot.autoconfigure.data.neo4j.Neo4jRepositoriesAutoConfiguration.class,
				org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration.class,
				org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration.class,

		}
)
public class ApiGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiGatewayApplication.class, args);
	}

}
