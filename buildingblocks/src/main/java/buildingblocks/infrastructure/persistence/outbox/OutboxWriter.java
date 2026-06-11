package buildingblocks.infrastructure.persistence.outbox;

import buildingblocks.core.events.DomainEvent;
import buildingblocks.infrastructure.persistence.outbox.model.OutboxMessage;
import buildingblocks.shared.context.CorrelationContext;
import buildingblocks.shared.utils.JsonUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
@ConditionalOnBean(OutboxRepository.class)
@ConditionalOnProperty(
        prefix = "sparrowx.outbox",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = false
)
public class OutboxWriter implements DomainEventOutbox {

    private final OutboxRepository repository;

    public OutboxWriter(OutboxRepository repository) {
        this.repository = repository;
    }

    @Override
    public void write(DomainEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("DomainEvent must not be null");
        }

        String payload = serialize(event);
        String topic = resolveTopic(event);
        String headers = buildHeaders();

        OutboxMessage message = new OutboxMessage(
                UUID.randomUUID(),
                resolveAggregateType(event),
                event.getClass().getName(),
                payload,
                topic,
                resolveMessageKey(event),
                headers
        );

        repository.save(message);
    }

    private String serialize(DomainEvent event) {
        try {
            return JsonUtils.toJson(event);
        } catch (Exception ex) {
            throw new IllegalStateException(
                    "Failed to serialize domain event: " + event.getClass().getName(),
                    ex
            );
        }
    }

    private String resolveTopic(DomainEvent event) {
        return "domain-events." + event.getClass().getSimpleName().toLowerCase();
    }

    private String resolveAggregateType(DomainEvent event) {
        if (event.getAggregateId() == null) {
            return "unknown";
        }

        return event.getAggregateId().getClass().getSimpleName();
    }

    private String resolveMessageKey(DomainEvent event) {
        if (event.getAggregateId() != null) {
            return event.getAggregateId().toString();
        }

        return UUID.randomUUID().toString();
    }

    private String buildHeaders() {
        Map<String, String> headers = new HashMap<>();

        if (CorrelationContext.getCorrelationId() != null) {
            headers.put("correlationId", CorrelationContext.getCorrelationId());
        }

        if (CorrelationContext.getTraceId() != null) {
            headers.put("traceId", CorrelationContext.getTraceId());
        }

        return JsonUtils.toJson(headers);
    }
}