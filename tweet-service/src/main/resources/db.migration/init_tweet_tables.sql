CREATE TABLE tweets (
                        id UUID PRIMARY KEY,
                        user_id UUID NOT NULL,
                        content VARCHAR(280) NOT NULL,
                        created_at TIMESTAMPTZ NOT NULL,
                        created_by BIGINT,
                        last_modified TIMESTAMPTZ,
                        last_modified_by BIGINT,
                        version BIGINT,
                        is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_tweets_user_id ON tweets(user_id);
CREATE INDEX idx_tweets_created_at ON tweets(created_at);
CREATE INDEX idx_tweets_not_deleted ON tweets(is_deleted);