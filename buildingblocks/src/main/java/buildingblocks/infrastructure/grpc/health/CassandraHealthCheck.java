package buildingblocks.infrastructure.grpc.health;

import buildingblocks.shared.exceptions.ServiceUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.stereotype.Component;

@Component
public class CassandraHealthCheck {

    private static final Logger LOG = LoggerFactory.getLogger(CassandraHealthCheck.class);
    private final CassandraTemplate cassandraTemplate;

    public CassandraHealthCheck(CassandraTemplate cassandraTemplate) {
        this.cassandraTemplate = cassandraTemplate;
    }

    public boolean isConnected() {
        try {
            cassandraTemplate.selectOne("SELECT release_version FROM system.local", String.class);
            return true;
        } catch (Exception e) {
            LOG.error("Cassandra health check failed", e);
            throw new ServiceUnavailableException("Cassandra database not reachable", e);
        }
    }
}
