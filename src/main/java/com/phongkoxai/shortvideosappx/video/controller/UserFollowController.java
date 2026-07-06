package com.phongkoxai.shortvideosappx.video.controller;

import com.phongkoxai.shortvideosappx.common.response.ApiResponse;
import com.phongkoxai.shortvideosappx.video.service.VideoService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserFollowController {
    VideoService videoService;

    @PostMapping("/{authorId}/follow")
    ApiResponse<Void> follow(@PathVariable String authorId) {
        videoService.follow(authorId);
        return ApiResponse.<Void>builder().build();
    }

    @DeleteMapping("/{authorId}/follow")
    ApiResponse<Void> unfollow(@PathVariable String authorId) {
        videoService.unfollow(authorId);
        return ApiResponse.<Void>builder().build();
    }
}
