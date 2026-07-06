package com.phongkoxai.shortvideosappx.video.controller;

import com.phongkoxai.shortvideosappx.common.response.ApiResponse;
import com.phongkoxai.shortvideosappx.common.response.PageResponse;
import com.phongkoxai.shortvideosappx.video.dto.request.VideoRejectRequest;
import com.phongkoxai.shortvideosappx.video.dto.response.VideoResponse;
import com.phongkoxai.shortvideosappx.video.dto.response.VideoReviewResponse;
import com.phongkoxai.shortvideosappx.video.enums.VideoStatus;
import com.phongkoxai.shortvideosappx.video.service.VideoService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/videos")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@PreAuthorize("hasAnyRole('ADMIN', 'REVIEWER')")
public class AdminVideoController {
    VideoService videoService;

    @GetMapping
    ApiResponse<PageResponse<VideoReviewResponse>> getVideos(
            @RequestParam(required = false) VideoStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.<PageResponse<VideoReviewResponse>>builder()
                .result(videoService.getAdminVideos(status, page, size))
                .build();
    }

    @PostMapping("/{id}/approve")
    ApiResponse<VideoResponse> approve(@PathVariable String id) {
        return ApiResponse.<VideoResponse>builder()
                .result(videoService.approve(id))
                .build();
    }
    @PostMapping("/{id}/reject")
    ApiResponse<VideoResponse> reject(@PathVariable String id, @RequestBody VideoRejectRequest request) {
        return ApiResponse.<VideoResponse>builder()
                .result(videoService.reject(id, request))
                .build();
    }
    @PostMapping("/{id}/take-down")
    ApiResponse<VideoResponse> takeDown(@PathVariable String id) {
        return ApiResponse.<VideoResponse>builder()
                .result(videoService.takeDownVideo(id))
                .build();
    }
}
