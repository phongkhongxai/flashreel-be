package com.phongkoxai.shortvideosappx.video.entity;

import com.phongkoxai.shortvideosappx.auth.entity.User;
import com.phongkoxai.shortvideosappx.video.enums.CommentStatus;
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
        name = "video_comments",
        indexes = {
                @Index(name = "idx_video_comments_video_status_created", columnList = "video_id,status,created_at"),
                @Index(name = "idx_video_comments_author_id", columnList = "author_id")
        }
)
public class VideoComment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36)
    String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "video_id", nullable = false)
    Video video;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    User author;

    @Column(nullable = false, length = 500)
    String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    CommentStatus status;

    @Column(nullable = false, updatable = false)
    Instant createdAt;

    @Column(nullable = false)
    Instant updatedAt;

    Instant deletedAt;

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        if (status == null) status = CommentStatus.VISIBLE;
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }
}
