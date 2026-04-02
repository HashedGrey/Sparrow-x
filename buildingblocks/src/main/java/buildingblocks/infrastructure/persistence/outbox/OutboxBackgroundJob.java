package buildingblocks.infrastructure.persistence.outbox;

import buildingblocks.infrastructure.messaging.integration.KafkaIntegrationPublisher;
import buildingblocks.infrastructure.persistence.outbox.model.MessageStatus;
import buildingblocks.infrastructure.persistence.outbox.model.OutboxMessage;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OutboxBackgroundJob {

    private static final Logger logger =
            LoggerFactory.getLogger(OutboxBackgroundJob.class);

    private final OutboxRepository repository;
    private final KafkaIntegrationPublisher publisher;

    private final int batchSize;
    private final int maxRetries;

    public OutboxBackgroundJob(
            OutboxRepository repository,
            KafkaIntegrationPublisher publisher
    ) {
        this.repository = repository;
        this.publisher = publisher;
        this.batchSize = 100;
        this.maxRetries = 5;
    }

    @Scheduled(fixedDelayString = "${outbox.poll-interval-ms:1000}")
    public void process() {

        List<OutboxMessage> messages =
                repository.findByStatusOrderByCreatedAtAsc(
                        MessageStatus.PENDING,
                        PageRequest.of(0, batchSize)
                );

        if (messages.isEmpty()) {
            return;
        }

        logger.debug("Processing {} outbox messages", messages.size());

        for (OutboxMessage message : messages) {
            processSingle(message);
        }
    }

    @Transactional
    protected void processSingle(OutboxMessage message) {

        try {

            message.markProcessing();

            publisher.publish(message);

            message.markProcessed();

            repository.save(message);

        } catch (Exception ex) {

            logger.error(
                    "Failed to publish outbox message {}",
                    message.getId(),
                    ex
            );

            if (message.canRetry(maxRetries)) {
                message.markFailed(ex.getMessage());
            } else {
                message.markDeadLetter(ex.getMessage());
            }

            repository.save(message);
        }
    }
}