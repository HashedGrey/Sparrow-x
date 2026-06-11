package com.sparrowx.internal.grpc.health;

import com.sparrowx.internal.data.postgres.repositories.EngineerJpaRepository;
import org.neo4j.driver.Driver;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class InternalServiceStartupCheck {

    private final EngineerJpaRepository engineerJpaRepository;
    private final Driver neo4jDriver;

    public InternalServiceStartupCheck(
            EngineerJpaRepository engineerJpaRepository,
            Driver neo4jDriver
    ) {
        this.engineerJpaRepository = engineerJpaRepository;
        this.neo4jDriver = neo4jDriver;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void verifyStartupDependencies() {
        engineerJpaRepository.count();

        try (var session = neo4jDriver.session()) {
            session.executeRead(tx ->
                    tx.run("RETURN 1 AS ok").single().get("ok").asInt()
            );
        }
    }
}