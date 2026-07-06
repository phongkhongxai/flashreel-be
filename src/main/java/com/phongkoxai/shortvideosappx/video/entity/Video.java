package com.phongkoxai.shortvideosappx.video.entity;

import com.phongkoxai.shortvideosappx.auth.entity.User;
import com.phongkoxai.shortvideosappx.video.enums.VideoStatus;
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
        name = "videos",
        indexes = {
                @Index(name = "idx_videos_status", columnList = "status"),
                @Index(name = "idx_videos_author_id", columnList = "author_id"),
                @Index(name = "idx_videos_approved_at", columnList = "approved_at"),
                @Index(name = "idx_videos_created_at", columnList = "created_at"),
                @Index(name = "idx_videos_latest_cursor", columnList = "status, approved_at, created_at, id"),
                @Index(name = "idx_videos_my_cursor", columnList = "author_id, status, created_at, id")
        }
)
public class Video {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36)
    String id;

    @Column(nullable = false, length = 60)
    String title;

    @Column(nullable = false, length = 1000)
    String videoUrl;

    @Column(length = 1000)
    String coverUrl;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    User author;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    VideoStatus status;

    @Column(length = 500)
    String rejectReason;

    @Column(nullable = false)
    Long likeCount;

    @Column(nullable = false)
    Long viewCount;

    @Column(nullable = false, columnDefinition = "bigint default 0")
    Long commentCount;

    Instant publishedAt;

    Instant approvedAt;

    @Column(nullable = false, updatable = false)
    Instant createdAt;

    @Column(nullable = false)
    Instant updatedAt;

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        if (status == null) status = VideoStatus.REVIEWING;
        if (likeCount == null) likeCount = 0L;
        if (viewCount == null) viewCount = 0L;
        if (commentCount == null) commentCount = 0L;
        if (publishedAt == null) publishedAt = now;
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        if (commentCount == null) commentCount = 0L;
        updatedAt = Instant.now();
    }
}
