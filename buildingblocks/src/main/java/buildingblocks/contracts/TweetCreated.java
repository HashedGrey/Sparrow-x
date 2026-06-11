package buildingblocks.contracts;

import buildingblocks.core.events.IntegrationEvent;

import java.time.Instant;
import java.util.UUID;

public record TweetCreated(
        UUID eventId,
        Instant occurredAt,
        UUID tweetId,
        UUID userId,
        String content
) implements IntegrationEvent {

    public TweetCreated(
            UUID tweetId,
            UUID userId,
            String content
    ) {
        this(
                UUID.randomUUID(),
                Instant.now(),
                tweetId,
                userId,
                content
        );
    }

    @Override
    public UUID getEventId() {
        return eventId;
    }

    @Override
    public Instant getOccurredAt() {
        return occurredAt;
    }

    @Override
    public String getEventType() {
        return "TweetCreated";
    }
}