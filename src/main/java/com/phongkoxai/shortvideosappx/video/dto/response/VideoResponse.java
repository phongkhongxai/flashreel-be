package com.phongkoxai.shortvideosappx.video.dto.response;

import com.phongkoxai.shortvideosappx.video.enums.VideoStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VideoResponse {
    String id;
    String title;
    String videoUrl;
    String coverUrl;
    String authorId;
    String authorNickname;
    VideoStatus status;
    String rejectReason;
    Long likeCount;
    Long viewCount;
    Long commentCount;
    Instant publishedAt;
    Instant approvedAt;
    Instant createdAt;
    Instant updatedAt;
}
