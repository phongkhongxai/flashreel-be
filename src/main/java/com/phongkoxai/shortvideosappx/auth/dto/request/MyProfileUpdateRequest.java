package com.phongkoxai.shortvideosappx.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
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
public class MyProfileUpdateRequest {
    @NotBlank(message = "INVALID_NICKNAME")
    @Size(max = 100, message = "INVALID_NICKNAME")
    String nickname;

    MultipartFile avatarFile;

    String avatarUrl;
}
