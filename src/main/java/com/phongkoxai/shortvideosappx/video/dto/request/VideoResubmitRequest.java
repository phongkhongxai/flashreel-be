package com.phongkoxai.shortvideosappx.video.dto.request;

import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VideoResubmitRequest {
    @Size(min = 1, max = 60, message = "INVALID_VIDEO_TITLE")
    String title;

    MultipartFile videoFile;
}
