package buildingblocks.infrastructure.grpc.health;

import buildingblocks.shared.exceptions.ServiceUnavailableException;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class PostgresHealthCheck {

    private static final Logger LOG = LoggerFactory.getLogger(PostgresHealthCheck.class);
    private final EntityManager entityManager;

    public PostgresHealthCheck(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public boolean isConnected() {
        try {
            entityManager.createNativeQuery("SELECT 1").getSingleResult();
            return true;
        } catch (Exception e) {
            LOG.error("Postgres health check failed", e);
            throw new ServiceUnavailableException("Postgres database not reachable", e);
        }
    }
}
