package com.phongkoxai.shortvideosappx.video.entity;

import com.phongkoxai.shortvideosappx.auth.entity.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(
        name = "user_follows",
        uniqueConstraints = @UniqueConstraint(name = "uk_user_follows_follower_author", columnNames = {"follower_id", "author_id"}),
        indexes = {
                @Index(name = "idx_user_follows_follower_id", columnList = "follower_id"),
                @Index(name = "idx_user_follows_author_id", columnList = "author_id")
        }
)
public class UserFollow {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36)
    String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "follower_id", nullable = false)
    User follower;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    User author;

    @Column(nullable = false, updatable = false)
    Instant createdAt;

    @PrePersist
    void prePersist() {
        createdAt = Instant.now();
    }
}
