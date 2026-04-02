package buildingblocks.infrastructure.persistence.outbox.model;

public enum MessageStatus {

    PENDING,      // written in same transaction as domain change
    PROCESSING,   // picked by outbox processor
    PROCESSED,    // successfully published
    FAILED,        // permanently failed after retries
    DEAD_LETTER
}