package com.phongkoxai.shortvideosappx.auth.dto.request;

import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserUpdateRequest {

    @Size(min = 2, max = 50,
            message = "Display name must be between 2 and 50 characters")
    String displayName;

    @Size(min = 1, max = 100,
            message = "Nickname must be between 1 and 100 characters")
    String nickname;

    @Size(max = 500,
            message = "Bio must not exceed 500 characters")
    String bio;

    String avatarUrl;
}
