
# Sparrow-X

A distributed systems playground where the main feature is GraphRAG, using graph-structured data from a realistic social network to power LLM queries, insights, and experiments. While the twitter clone is just the substrate inspired by Grok, whose access to over a quarter-billion active user interactions transforms X into a second-generation, directory-like network of human knowledge, Sparrowx distills that idea into an accessible MVP for junior devs( Through Bottom-Up Understanding) and Senior Engineers coming from other languages (Through Top-Down ).

## Goals Of This Project
- 🔹 Using Vertical Slice Architecture for architecture level.
- 🔹 Using Spring MVC as a Web Framework.
- 🔹 Using Domain Driven Design (DDD) to implement all business processes in microservices.
- 🔹 Using Spring AMQP on top of Rabbitmq for Event Driven Architecture between our microservices.
- 🔹 Using gRPC for internal communication between our microservices.
- 🔹 Using CQRS implementation with a Mediator library.
- 🔹 Using Spring AI for GraphRAG, enabling hybrid retrieval (graph traversals + vector search) and natural language queries over the social graph --> Supporting multi-hop GraphRAG queries, where the system can traverse multiple edges (e.g., User → Follows → User → Tweets → Hashtags) to generate richer, context-aware answers and recommendations.
- 🔹 Using Spring Data JPA for data persistence and ORM in write side with Postgres.
- 🔹 Using Spring Data Cassandra for data persistence and ORM in read side with CassandraDB.
- 🔹 Using Spring Data Neo4j for graph-based queries, social graph traversal, and recommendation logic.
- 🔹 Using Inbox Pattern for ensuring message idempotency for receiver and Exactly once Delivery.
- 🔹 Using Outbox Pattern for ensuring no message is lost and there is at At Least One Delivery.
- 🔹 Using Unit Testing for testing small units and mocking our dependencies with Mockito.
- 🔹 Using End-To-End Testing and Integration Testing for testing features with all dependencies using testcontainers.
- 🔹 Using Spring Validator and a Validation Pipeline Behavior on top of Mediator.
- 🔹 Using Springdoc Openapi for generating OpenAPI documentation in Spring Boot.
- 🔹 Using OpenTelemetry Collector for collecting Metrics, Tracings, and Structured Logs.
- 🔹 Using Loki for Logging.
- 🔹 Using Tempo for Distributed Tracing.
- 🔹 Using Prometheus and Grafana for monitoring.
- 🔹 Using Keycloak for authentication and authorization based on OpenID-Connect and OAuth2.
- 🔹 Using Spring Cloud Gateway MVC as a Microservices' gateway.



## Roadmap

| Feature              | Dormant | In Progress | Completed |
|----------------------|---------|-------------|-----------|
| API Gateway          |        |             |      ✅     |
| Building Blocks      |         |            |     ✅       |
| Fanout Service         |    ✅     |             |           |
| Profile Service         |        |     ✅        |           |
| Storage Service         | ✅       |             |           |
| Search Service | ✅       |             |           |
| Timeline Service | ✅       |             |           |
| Tweet Service | ✅       |             |           |


