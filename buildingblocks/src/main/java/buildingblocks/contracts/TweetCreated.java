package buildingblocks.contracts;

import buildingblocks.core.events.IntegrationEvent;
import java.time.Instant;
import java.util.UUID;

public record TweetCreated(
        UUID tweetId,
        UUID userId,
        String content
) implements IntegrationEvent {

    private static final UUID EVENT_ID = UUID.randomUUID();
    private static final Instant OCCURRED = Instant.now();

    @Override
    public UUID getEventId() {
        return EVENT_ID;
    }

    @Override
    public Instant getOccurredAt() {
        return OCCURRED;
    }

    @Override
    public String getEventType() {
        return "TweetCreated";
    }
}