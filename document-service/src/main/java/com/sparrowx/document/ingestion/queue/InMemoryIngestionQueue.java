package com.sparrowx.document.ingestion.queue;

import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
public class InMemoryIngestionQueue implements IngestionQueue {

    private final Queue<IngestionQueueMessage> queue = new ConcurrentLinkedQueue<>();

    @Override
    public void enqueue(IngestionQueueMessage message) {
        if (message == null) {
            throw new IllegalArgumentException("IngestionQueueMessage must not be null");
        }

        queue.add(message);
    }

    @Override
    public Optional<IngestionQueueMessage> poll() {
        return Optional.ofNullable(queue.poll());
    }

    @Override
    public int size() {
        return queue.size();
    }
}