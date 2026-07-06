package com.phongkoxai.shortvideosappx.video.dto.request;

import com.phongkoxai.shortvideosappx.video.enums.RejectReasonType;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VideoRejectRequest {
    RejectReasonType reasonType;
    String customReason;
}
