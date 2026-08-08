package com.sparrowx.agentic.config;

import com.sparrowx.agentic.mission.MissionSubmissionService;
import com.sparrowx.agentic.mission.model.MissionVersionSnapshot;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration(proxyBeanMethods = false)
public class MissionConfig {

    @Bean
    public MissionVersionSnapshot missionVersionSnapshot() {
        return new MissionVersionSnapshot(
                "agentic-v1",
                Map.of(
                        "intent-model", "v1",
                        "planning-model", "v1",
                        "review-model", "v1",
                        "synthesis-model", "v1"
                ),
                Map.of(
                        "intent-prompt", "v1",
                        "planning-prompt", "v1",
                        "review-prompt", "v1",
                        "synthesis-prompt", "v1"
                ),
                Map.of(
                        "budget-policy", "v1",
                        "data-handling-policy", "v1",
                        "grounding-policy", "v1",
                        "human-approval-policy", "v1",
                        "source-authorization-policy", "v1",
                        "tool-authorization-policy", "v1"
                ),
                Map.of(
                        "document-service", "v1",
                        "internal-service", "v1"
                )
        );
    }

    @Bean
    public MissionSubmissionService.VersionSnapshotProvider
    versionSnapshotProvider(
            MissionVersionSnapshot snapshot
    ) {
        return () -> snapshot;
    }
}