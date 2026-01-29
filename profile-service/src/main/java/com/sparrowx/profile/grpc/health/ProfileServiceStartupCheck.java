package com.sparrowx.profile.grpc.health;

import buildingblocks.infrastructure.grpc.health.CassandraHealthCheck;
import buildingblocks.infrastructure.grpc.health.GrpcHealthAdapter;
import buildingblocks.infrastructure.grpc.health.PostgresHealthCheck;
import buildingblocks.shared.exceptions.ServiceUnavailableException;
import com.sparrowx.profile.data.neo4j.ProfileNeo4jHealth;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ProfileServiceStartupCheck {

    private static final Logger LOG = LoggerFactory.getLogger(ProfileServiceStartupCheck.class);

    private final GrpcHealthAdapter healthAdapter;
    private final PostgresHealthCheck postgresHealth;
    private final CassandraHealthCheck cassandraHealth;
    private final ProfileNeo4jHealth neo4jHealth;

    public ProfileServiceStartupCheck(
            GrpcHealthAdapter healthAdapter,
            PostgresHealthCheck postgresHealth,
            CassandraHealthCheck cassandraHealth,
            ProfileNeo4jHealth neo4jHealth) {
        this.healthAdapter = healthAdapter;
        this.postgresHealth = postgresHealth;
        this.cassandraHealth = cassandraHealth;
        this.neo4jHealth = neo4jHealth;
    }

    @PostConstruct
    public void checkDependencies() {
        try {
            boolean pgReady = postgresHealth.isConnected();
            boolean cassReady = cassandraHealth.isConnected();
            boolean neo4jReady = neo4jHealth.isConnected();

            if (pgReady && cassReady && neo4jReady) {
                LOG.info("ProfileService: all dependencies healthy. gRPC health = SERVING");
                healthAdapter.setServing("ProfileService");
            }
        } catch (ServiceUnavailableException e) {
            LOG.warn("ProfileService: some dependencies unavailable. gRPC health = NOT_SERVING", e);
            healthAdapter.setNotServing("ProfileService");
        } catch (Exception e) {
            LOG.error("ProfileService: unexpected error during startup health check", e);
            healthAdapter.setNotServing("ProfileService");
        }
    }
}
