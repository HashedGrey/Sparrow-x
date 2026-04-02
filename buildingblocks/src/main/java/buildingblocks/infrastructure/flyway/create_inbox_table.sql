CREATE TABLE inbox_messages (
                                id UUID PRIMARY KEY,
                                event_type VARCHAR(255) NOT NULL,
                                processed_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_inbox_processed_at
    ON inbox_messages(processed_at);