package buildingblocks.infrastructure.persistence.outbox;

import buildingblocks.infrastructure.messaging.integration.KafkaIntegrationPublisher;
import buildingblocks.infrastructure.persistence.outbox.model.MessageStatus;
import buildingblocks.infrastructure.persistence.outbox.model.OutboxMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OutboxProcessor {

    private static final Logger log =
            LoggerFactory.getLogger(OutboxProcessor.class);

    private static final int BATCH_SIZE = 50;
    private static final int MAX_RETRIES = 5;

    private final OutboxRepository repository;
    private final KafkaIntegrationPublisher publisher;

    public OutboxProcessor(
            OutboxRepository repository,
            KafkaIntegrationPublisher publisher
    ) {
        this.repository = repository;
        this.publisher = publisher;
    }


    @Scheduled(fixedDelay = 2000)
    public void process() {

        List<OutboxMessage> messages =
                repository.findByStatusOrderByCreatedAtAsc(
                        MessageStatus.PENDING,
                        PageRequest.of(0, BATCH_SIZE)
                );

        if (messages.isEmpty()) {
            return;
        }

        for (OutboxMessage message : messages) {

            try {

                message.markProcessing();
                repository.save(message);

                publisher.publish(message);

                message.markProcessed();
                repository.save(message);

                log.debug(
                        "Outbox message {} published successfully",
                        message.getId()
                );

            } catch (Exception ex) {

                log.error(
                        "Failed to publish outbox message {}",
                        message.getId(),
                        ex
                );

                if (message.canRetry(MAX_RETRIES)) {

                    message.markFailed(ex.getMessage());

                } else {

                    message.markDeadLetter(ex.getMessage());

                }

                repository.save(message);
            }
        }
    }
}