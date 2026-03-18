package com.sparrowx.tweet.data.cassandra.repositories;

import com.sparrowx.tweet.data.cassandra.tables.TweetTable;
import org.jspecify.annotations.NonNull;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TweetCassandraRepository extends CassandraRepository<TweetTable, UUID> {

    Optional<TweetTable> findById(@NonNull UUID id);

    List<TweetTable> findByUserId(UUID userId);
}