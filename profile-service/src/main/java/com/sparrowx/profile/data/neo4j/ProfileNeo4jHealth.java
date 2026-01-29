package com.sparrowx.profile.data.neo4j;

import buildingblocks.shared.exceptions.ServiceUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Component;

@Component
public class ProfileNeo4jHealth {

    private static final Logger LOG = LoggerFactory.getLogger(ProfileNeo4jHealth.class);
    private final Neo4jClient neo4jClient;

    public ProfileNeo4jHealth(Neo4jClient neo4jClient) {
        this.neo4jClient = neo4jClient;
    }

    public boolean isConnected() {
        try {
            neo4jClient.query("RETURN 1").fetch().one();
            return true;
        } catch (Exception e) {
            LOG.error("Neo4j health check failed", e);
            throw new ServiceUnavailableException("Neo4j database not reachable", e);
        }
    }
}
