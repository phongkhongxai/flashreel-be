package com.phongkoxai.shortvideosappx.video.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VideoLikeResponse {
    String videoId;
    Boolean liked;
    Long likeCount;
}
