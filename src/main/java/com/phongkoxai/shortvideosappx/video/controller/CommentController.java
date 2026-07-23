package com.phongkoxai.shortvideosappx.video.controller;

import com.phongkoxai.shortvideosappx.common.response.ApiResponse;
import com.phongkoxai.shortvideosappx.common.response.CursorResponse;
import com.phongkoxai.shortvideosappx.common.response.PageResponse;
import com.phongkoxai.shortvideosappx.video.dto.request.CommentCreationRequest;
import com.phongkoxai.shortvideosappx.video.dto.response.CommentResponse;
import com.phongkoxai.shortvideosappx.video.dto.response.VideoResponse;
import com.phongkoxai.shortvideosappx.video.enums.VideoStatus;
import com.phongkoxai.shortvideosappx.video.service.CommentService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/videos/{videoId}/comments")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CommentController {
    CommentService commentService;

    @GetMapping
    ApiResponse<PageResponse<CommentResponse>> getComments(
            @PathVariable String videoId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.<PageResponse<CommentResponse>>builder()
                .result(commentService.getComments(videoId, page, size))
                .build();
    }
    @GetMapping("/cursor")
    ApiResponse<CursorResponse<CommentResponse>> getCommentsByCursor(
            @PathVariable String videoId,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.<CursorResponse<CommentResponse>>builder()
                .result(commentService.getCommentsCursor(videoId, cursor, size))
                .build();
    }

    @PostMapping
    ApiResponse<CommentResponse> createComment(
            @PathVariable String videoId,
            @RequestBody @Valid CommentCreationRequest request) {
        return ApiResponse.<CommentResponse>builder()
                .result(commentService.createComment(videoId, request))
                .build();
    }

    @DeleteMapping("/{commentId}")
    ApiResponse<Void> deleteComment(@PathVariable String videoId, @PathVariable String commentId) {
        commentService.deleteComment(videoId, commentId);
        return ApiResponse.<Void>builder().build();
    }
}
