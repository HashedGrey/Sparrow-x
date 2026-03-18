package com.sparrowx.tweet.data.postgres.entities;

import buildingblocks.domain.model.BaseEntity;
import com.sparrowx.tweet.valueobjects.TweetContent;
import com.sparrowx.tweet.valueobjects.UserId;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Entity
@Table(name = "tweets")
@Getter
@NoArgsConstructor
public class TweetEntity extends BaseEntity<UUID> {

    @Embedded
    @AttributeOverride(
            name = "value",
            column = @Column(name = "user_id", nullable = false)
    )
    private UserId userId;

    @Embedded
    @AttributeOverride(
            name = "value",
            column = @Column(name = "content", nullable = false, length = 280)
    )
    private TweetContent content;

    public TweetEntity(
            UUID id,
            UserId userId,
            TweetContent content
    ) {
        this.id = id;
        this.userId = userId;
        this.content = content;
    }

    public void updateContent(TweetContent newContent) {
        this.content = newContent;
    }
}