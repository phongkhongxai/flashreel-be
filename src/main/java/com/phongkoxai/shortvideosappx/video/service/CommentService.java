package com.phongkoxai.shortvideosappx.video.service;

import com.phongkoxai.shortvideosappx.auth.repository.UserRepository;
import com.phongkoxai.shortvideosappx.common.exception.AppException;
import com.phongkoxai.shortvideosappx.common.exception.ErrorCode;
import com.phongkoxai.shortvideosappx.common.response.CursorResponse;
import com.phongkoxai.shortvideosappx.common.response.PageResponse;
import com.phongkoxai.shortvideosappx.common.util.SecurityUtils;
import com.phongkoxai.shortvideosappx.video.dto.request.CommentCreationRequest;
import com.phongkoxai.shortvideosappx.video.dto.response.CommentResponse;
import com.phongkoxai.shortvideosappx.video.dto.response.VideoResponse;
import com.phongkoxai.shortvideosappx.video.entity.Video;
import com.phongkoxai.shortvideosappx.video.entity.VideoComment;
import com.phongkoxai.shortvideosappx.video.enums.CommentStatus;
import com.phongkoxai.shortvideosappx.video.enums.VideoStatus;
import com.phongkoxai.shortvideosappx.video.mapper.CommentMapper;
import com.phongkoxai.shortvideosappx.video.repository.VideoCommentRepository;
import com.phongkoxai.shortvideosappx.video.config.VideoRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CommentService {
    VideoCommentRepository videoCommentRepository;
    VideoRepository videoRepository;
    UserRepository userRepository;
    CommentMapper commentMapper;

    @Transactional(readOnly = true)
    public PageResponse<CommentResponse> getComments(String videoId, int page, int size) {
        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new AppException(ErrorCode.VIDEO_NOT_EXISTED));
        if (video.getStatus() != VideoStatus.APPROVED) throw new AppException(ErrorCode.VIDEO_NOT_APPROVED);

        return PageResponse.from(videoCommentRepository.findByVideoIdAndStatusOrderByCreatedAtDesc(
                        videoId,
                        CommentStatus.VISIBLE,
                        PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")))
                .map(commentMapper::toCommentResponse));
    }

    @Transactional(readOnly = true)
    public CursorResponse<CommentResponse> getCommentsCursor(String videoId, String cursor, int size) {
        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new AppException(ErrorCode.VIDEO_NOT_EXISTED));
        if (video.getStatus() != VideoStatus.APPROVED) throw new AppException(ErrorCode.VIDEO_NOT_APPROVED);
        CommentCursor commentCursor = decodeCommentCursor(cursor);

        List<VideoComment> videoComments = videoCommentRepository.findCommentsCursor(
                videoId,
                commentCursor == null ? null : commentCursor.createdAt(),
                commentCursor == null ? null : commentCursor.id(),
                PageRequest.of(0, size + 1)
        );

        boolean hasNext = videoComments.size() > size;
        List<VideoComment> pageItems = hasNext ? videoComments.subList(0, size) : videoComments;

        String nextCursor = null;
        if (hasNext && !pageItems.isEmpty()) {
            VideoComment last = pageItems.get(pageItems.size() - 1);
            nextCursor = encodeCommentCursor(new CommentCursor(
                    last.getCreatedAt(),
                    last.getId()
            ));
        }

        return CursorResponse.<CommentResponse>builder()
                .data(pageItems.stream().map(commentMapper::toCommentResponse).toList())
                .nextCursor(nextCursor)
                .hasNext(hasNext)
                .build();
    }

    @Transactional
    public CommentResponse createComment(String videoId, CommentCreationRequest request) {
//        User author = getCurrentActiveUser();
        String userId = SecurityUtils.getCurrentUserId0();
        var author = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        Video video = videoRepository.findWithAuthorById(videoId)
                .orElseThrow(() -> new AppException(ErrorCode.VIDEO_NOT_EXISTED));
        if (video.getStatus() != VideoStatus.APPROVED) throw new AppException(ErrorCode.VIDEO_NOT_APPROVED);

        VideoComment comment = VideoComment.builder()
                .video(video)
                .author(author)
                .content(request.getContent().trim())
                .status(CommentStatus.VISIBLE)
                .build();

        comment = videoCommentRepository.save(comment);
        videoRepository.incrementCommentCount(videoId);
        video.setCommentCount((video.getCommentCount() == null ? 0L : video.getCommentCount()) + 1);
        return commentMapper.toCommentResponse(comment);
    }

    @Transactional
    public void deleteComment(String videoId, String commentId) {
        String userId = SecurityUtils.getCurrentUserId0();
        VideoComment comment = videoCommentRepository.findWithAuthorAndVideoById(commentId)
                .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_EXISTED));
        if (!Objects.equals(comment.getVideo().getId(), videoId)) throw new AppException(ErrorCode.COMMENT_NOT_EXISTED);
        if (!Objects.equals(comment.getAuthor().getId(), userId) && !hasReviewerAuthority())
            throw new AppException(ErrorCode.COMMENT_ACCESS_DENIED);

        if (comment.getStatus() == CommentStatus.DELETED) return;

        comment.setStatus(CommentStatus.DELETED);
        comment.setDeletedAt(Instant.now());
        videoCommentRepository.save(comment);
        videoRepository.decrementCommentCount(videoId);
    }

//    private User getCurrentActiveUser() {
//        User user = getCurrentUser();
//        if (user.getAccountStatus() == AccountStatus.CANCELLED || Boolean.FALSE.equals(user.getEnabled()))
//            throw new AppException(ErrorCode.ACCOUNT_CANCELLED);
//        return user;
//    }

//    private User getCurrentUser() {
//        String username = SecurityContextHolder.getContext().getAuthentication().getName();
//        return userRepository.findByUsername(username).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
//    }

    private boolean hasReviewerAuthority() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) return false;
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> Objects.equals(authority.getAuthority(), "ROLE_ADMIN")
                        || Objects.equals(authority.getAuthority(), "ROLE_REVIEWER"));
    }

    private String encode(String value) {
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private String decode(String cursor) {
        if (cursor == null || cursor.isBlank()) return null;
        try {
            return new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            throw new AppException(ErrorCode.INVALID_CURSOR);
        }
    }

    private CommentCursor decodeCommentCursor(String cursor) {
        String decoded = decode(cursor);
        if (decoded == null) return null;

        try {
            String[] parts = decoded.split("\\|");
            if (parts.length != 2) throw new AppException(ErrorCode.INVALID_CURSOR);

            return new CommentCursor(
                    Instant.parse(parts[0]),
                    parts[1]
            );
        } catch (IllegalArgumentException e) {
            throw new AppException(ErrorCode.INVALID_CURSOR);
        }
    }

    private String encodeCommentCursor(CommentCursor cursor) {
        return encode(cursor.createdAt() + "|" + cursor.id());
    }
    private record CommentCursor(
            Instant createdAt,
            String id
    ) {
    }
}
