package com.sparrowx.document.ingestion;

import com.sparrowx.document.ingestion.queue.IngestionQueue;
import com.sparrowx.document.ingestion.queue.IngestionQueueMessage;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class IngestionWorker {

    private final IngestionQueue ingestionQueue;
    private final IngestionJobRunner ingestionJobRunner;
    private final IngestionWorkerProperties properties;

    public IngestionWorker(
            IngestionQueue ingestionQueue,
            IngestionJobRunner ingestionJobRunner,
            IngestionWorkerProperties properties
    ) {
        this.ingestionQueue = ingestionQueue;
        this.ingestionJobRunner = ingestionJobRunner;
        this.properties = properties;
    }

    @Scheduled(fixedDelayString = "${sparrowx.document.ingestion.worker.poll-interval-millis:1000}")
    public void poll() {
        if (!properties.enabled()) {
            return;
        }

        int maxJobs = Math.max(1, properties.maxJobsPerPoll());

        for (int i = 0; i < maxJobs; i++) {
            Optional<IngestionQueueMessage> nextMessage = ingestionQueue.poll();

            if (nextMessage.isEmpty()) {
                return;
            }

            ingestionJobRunner.run(nextMessage.get());
        }
    }
}