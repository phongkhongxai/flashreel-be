package com.phongkoxai.shortvideosappx.video.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CommentCreationRequest {
    @NotBlank(message = "INVALID_COMMENT_CONTENT")
    @Size(max = 500, message = "INVALID_COMMENT_CONTENT")
    String content;
}
