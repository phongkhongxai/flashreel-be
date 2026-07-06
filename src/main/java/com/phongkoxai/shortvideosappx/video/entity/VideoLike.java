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
        name = "video_likes",
        uniqueConstraints = @UniqueConstraint(name = "uk_video_likes_user_video", columnNames = {"user_id", "video_id"}),
        indexes = {
                @Index(name = "idx_video_likes_user_id", columnList = "user_id"),
                @Index(name = "idx_video_likes_video_id", columnList = "video_id")
        }
)
public class VideoLike {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36)
    String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "video_id", nullable = false)
    Video video;

    @Column(nullable = false, updatable = false)
    Instant createdAt;

    @PrePersist
    void prePersist() {
        createdAt = Instant.now();
    }
}
