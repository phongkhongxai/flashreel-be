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
public class VideoReviewResponse {
    String id;
    String title;
    String authorNickname;
    String authorEmail;
    String authorAvatar;
    Instant uploadTime;
    String videoPreviewUrl;
    String coverUrl;
    Long likeCount;
    Long viewCount;
    Long commentCount;
    VideoStatus status;
}
