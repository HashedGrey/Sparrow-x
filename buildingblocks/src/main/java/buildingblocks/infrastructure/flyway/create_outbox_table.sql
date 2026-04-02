CREATE TABLE outbox_messages (
    id UUID PRIMARY KEY,

    aggregate_type VARCHAR(255) NOT NULL,
    event_type VARCHAR(255) NOT NULL,
    topic VARCHAR(255) NOT NULL,

    message_key VARCHAR(255),

    payload TEXT NOT NULL,
    headers TEXT,

    status VARCHAR(50) NOT NULL,

    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    processed_at TIMESTAMP WITH TIME ZONE,

    attempts INTEGER NOT NULL DEFAULT 0,
    next_retry_at TIMESTAMP WITH TIME ZONE,

    last_error TEXT,

    version BIGINT
);

-- Indexes
CREATE INDEX idx_outbox_status_created
ON outbox_messages (status, created_at);

CREATE INDEX idx_outbox_retry
ON outbox_messages (status, next_retry_at);