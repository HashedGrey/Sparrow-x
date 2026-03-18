CREATE TABLE tweets (
                        id uuid,
                        user_id uuid,
                        created_at timestamp,
                        content text,
                        PRIMARY KEY (user_id, created_at)
) WITH CLUSTERING ORDER BY (created_at DESC);