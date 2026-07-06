package com.phongkoxai.shortvideosappx.video.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CommentResponse {
    String id;
    String videoId;
    String authorId;
    String authorNickname;
    String authorAvatar;
    String content;
    Instant createdAt;
    Instant updatedAt;
}
