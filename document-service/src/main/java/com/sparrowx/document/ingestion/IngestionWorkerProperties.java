package com.sparrowx.document.ingestion;

import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Setter
@ConfigurationProperties(prefix = "sparrowx.document.ingestion.worker")
public class IngestionWorkerProperties {

    private boolean enabled = true;
    private long pollIntervalMillis = 1_000L;
    private int maxJobsPerPoll = 5;

    public boolean enabled() {
        return enabled;
    }

    public long pollIntervalMillis() {
        return pollIntervalMillis;
    }

    public int maxJobsPerPoll() {
        return maxJobsPerPoll;
    }

}