package com.phongkoxai.shortvideosappx.auth.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponse {

    String id;

    String username;

    String email;

    String displayName;

    String nickname;

    String bio;

    String avatarUrl;

    Long followerCount;

    Long followingCount;

    Long videoCount;

    Boolean enabled;

    Instant createdAt;
}
