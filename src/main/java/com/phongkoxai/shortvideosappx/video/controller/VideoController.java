package com.phongkoxai.shortvideosappx.video.controller;

import com.phongkoxai.shortvideosappx.common.response.ApiResponse;
import com.phongkoxai.shortvideosappx.common.response.CursorResponse;
import com.phongkoxai.shortvideosappx.common.response.PageResponse;
import com.phongkoxai.shortvideosappx.video.dto.request.VideoPublishRequest;
import com.phongkoxai.shortvideosappx.video.dto.request.VideoResubmitRequest;
import com.phongkoxai.shortvideosappx.video.dto.response.VideoLikeResponse;
import com.phongkoxai.shortvideosappx.video.dto.response.VideoPlaybackResponse;
import com.phongkoxai.shortvideosappx.video.dto.response.VideoResponse;
import com.phongkoxai.shortvideosappx.video.enums.VideoStatus;
import com.phongkoxai.shortvideosappx.video.service.VideoService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/videos")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VideoController {
    VideoService videoService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ApiResponse<VideoResponse> publish(@ModelAttribute @Valid VideoPublishRequest request) {
        return ApiResponse.<VideoResponse>builder()
                .result(videoService.publish(request))
                .build();
    }

    @GetMapping("/{id}")
    ApiResponse<VideoResponse> getVideo(@PathVariable String id) {
        return ApiResponse.<VideoResponse>builder()
                .result(videoService.getVideo(id))
                .build();
    }

    @GetMapping("/feed")
    ApiResponse<PageResponse<VideoResponse>> getFeed(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.<PageResponse<VideoResponse>>builder()
                .result(videoService.getFeed(page, size))
                .build();
    }

    @GetMapping("/feed/cursor")
    ApiResponse<CursorResponse<VideoResponse>> getFeedByCursor(
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.<CursorResponse<VideoResponse>>builder()
                .result(videoService.getFeed(cursor, size))
                .build();
    }

    @GetMapping("/latest")
    ApiResponse<PageResponse<VideoResponse>> getLatestVideos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.<PageResponse<VideoResponse>>builder()
                .result(videoService.getLatestVideos(page, size))
                .build();
    }

    @GetMapping("/latest/cursor")
    ApiResponse<CursorResponse<VideoResponse>> getLatestVideosByCursor(
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.<CursorResponse<VideoResponse>>builder()
                .result(videoService.getLatestVideos(cursor, size))
                .build();
    }

    @PostMapping("/{id}/view")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void recordView(@PathVariable String id) {
        videoService.recordView(id);
    }

    @GetMapping("/{id}/playback")
    ApiResponse<VideoPlaybackResponse> getPlayback(@PathVariable String id) {
        return ApiResponse.<VideoPlaybackResponse>builder()
                .result(videoService.getPlayback(id))
                .build();
    }

    @PostMapping("/{id}/like")
    ApiResponse<VideoLikeResponse> like(@PathVariable String id) {
        return ApiResponse.<VideoLikeResponse>builder()
                .result(videoService.like(id))
                .build();
    }

    @DeleteMapping("/{id}/like")
    ApiResponse<VideoLikeResponse> unlike(@PathVariable String id) {
        return ApiResponse.<VideoLikeResponse>builder()
                .result(videoService.unlike(id))
                .build();
    }

    @GetMapping("/my")
    ApiResponse<PageResponse<VideoResponse>> getMyVideos(
            @RequestParam(required = false) VideoStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.<PageResponse<VideoResponse>>builder()
                .result(videoService.getMyVideos(status, page, size))
                .build();
    }

    @GetMapping("/my/cursor")
    ApiResponse<CursorResponse<VideoResponse>> getMyVideosByCursor(
            @RequestParam(required = false) VideoStatus status,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.<CursorResponse<VideoResponse>>builder()
                .result(videoService.getMyVideos(status, cursor, size))
                .build();
    }

    @PostMapping(value = "/{id}/resubmit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ApiResponse<VideoResponse> resubmit(@PathVariable String id, @ModelAttribute @Valid VideoResubmitRequest request) {
        return ApiResponse.<VideoResponse>builder()
                .result(videoService.resubmit(id, request))
                .build();
    }
}
