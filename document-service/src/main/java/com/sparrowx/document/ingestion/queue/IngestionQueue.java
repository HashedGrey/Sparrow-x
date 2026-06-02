package com.sparrowx.document.ingestion.queue;

import java.util.Optional;

public interface IngestionQueue {

    void enqueue(IngestionQueueMessage message);

    Optional<IngestionQueueMessage> poll();

    int size();
}