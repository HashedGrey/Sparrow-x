package com.sparrowx.tweet.data.postgres.repositories;

import com.sparrowx.tweet.data.postgres.entities.TweetEntity;
import com.sparrowx.tweet.valueobjects.UserId;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TweetRepository extends JpaRepository<TweetEntity, UUID> {

    Optional<TweetEntity> findByIdAndIsDeletedFalse(UUID id);

    List<TweetEntity> findByUserIdAndIsDeletedFalse(UserId userId);

}