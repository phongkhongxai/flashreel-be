package com.phongkoxai.shortvideosappx.video.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VideoPlaybackResponse {
    String id;
    String title;
    String videoUrl;
    String coverUrl;
    String authorId;
    String authorAvatar;
    String authorNickname;
    Long likeCount;
    Long viewCount;
    Long commentCount;
    Boolean liked;
    Boolean followingAuthor;
}
