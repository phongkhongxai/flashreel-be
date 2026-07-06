package com.phongkoxai.shortvideosappx.video.service;

import com.phongkoxai.shortvideosappx.auth.entity.AccountStatus;
import com.phongkoxai.shortvideosappx.auth.entity.User;
import com.phongkoxai.shortvideosappx.auth.repository.UserRepository;
import com.phongkoxai.shortvideosappx.common.exception.AppException;
import com.phongkoxai.shortvideosappx.common.exception.ErrorCode;
import com.phongkoxai.shortvideosappx.common.response.CursorResponse;
import com.phongkoxai.shortvideosappx.common.response.PageResponse;
import com.phongkoxai.shortvideosappx.video.dto.request.VideoPublishRequest;
import com.phongkoxai.shortvideosappx.video.dto.request.VideoRejectRequest;
import com.phongkoxai.shortvideosappx.video.dto.request.VideoResubmitRequest;
import com.phongkoxai.shortvideosappx.video.dto.response.VideoPlaybackResponse;
import com.phongkoxai.shortvideosappx.video.dto.response.VideoLikeResponse;
import com.phongkoxai.shortvideosappx.video.dto.response.VideoResponse;
import com.phongkoxai.shortvideosappx.video.dto.response.VideoReviewResponse;
import com.phongkoxai.shortvideosappx.video.entity.UserFollow;
import com.phongkoxai.shortvideosappx.video.entity.Video;
import com.phongkoxai.shortvideosappx.video.entity.VideoLike;
import com.phongkoxai.shortvideosappx.video.enums.VideoStatus;
import com.phongkoxai.shortvideosappx.video.mapper.VideoMapper;
import com.phongkoxai.shortvideosappx.video.repository.UserFollowRepository;
import com.phongkoxai.shortvideosappx.video.repository.VideoLikeRepository;
import com.phongkoxai.shortvideosappx.video.repository.VideoRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VideoService {
    static long MAX_VIDEO_SIZE = 300L * 1024L * 1024L;
    static Duration MIN_DURATION = Duration.ofSeconds(5);
    static Duration MAX_DURATION = Duration.ofMinutes(3);
    static double FEED_DECAY_FACTOR = 0.1D;

    VideoRepository videoRepository;
    VideoLikeRepository videoLikeRepository;
    UserFollowRepository userFollowRepository;
    UserRepository userRepository;
    StorageService storageService;
    VideoMetadataService videoMetadataService;
    VideoMapper videoMapper;

    @Transactional
    public VideoResponse publish(VideoPublishRequest request) {
        User author = getCurrentActiveUser();
        MultipartFile videoFile = request.getVideoFile();
        Duration duration = validateVideoFile(videoFile);

        String videoUrl = storageService.uploadVideo(videoFile);
        String coverUrl = uploadThumbnail(videoFile, duration);

        Video video = Video.builder()
                .title(request.getTitle().trim())
                .videoUrl(videoUrl)
                .coverUrl(coverUrl)
                .author(author)
                .status(VideoStatus.REVIEWING)
                .likeCount(0L)
                .viewCount(0L)
                .commentCount(0L)
                .publishedAt(Instant.now())
                .build();

        video = videoRepository.save(video);
        videoRepository.incrementVideoCount(author.getId());
        return videoMapper.toVideoResponse(video);
    }

    @Transactional(readOnly = true)
    public VideoResponse getVideo(String id) {
        Video video = videoRepository.findWithAuthorById(id)
                .orElseThrow(() -> new AppException(ErrorCode.VIDEO_NOT_EXISTED));
        return videoMapper.toVideoResponse(video);
    }

    @Transactional(readOnly = true)
    public PageResponse<VideoReviewResponse> getAdminVideos(VideoStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        var result = (status == null ? videoRepository.findAll(pageable) : videoRepository.findByStatus(status, pageable))
                .map(videoMapper::toReviewResponse);
        return PageResponse.from(result);
    }

    @Transactional
    public VideoResponse approve(String id) {
        Video video = videoRepository.findWithAuthorById(id)
                .orElseThrow(() -> new AppException(ErrorCode.VIDEO_NOT_EXISTED));
        video.setStatus(VideoStatus.APPROVED);
        video.setApprovedAt(Instant.now());
        video.setRejectReason(null);
        return videoMapper.toVideoResponse(videoRepository.save(video));
    }

    @Transactional
    public VideoResponse reject(String id, VideoRejectRequest request) {
        Video video = videoRepository.findWithAuthorById(id)
                .orElseThrow(() -> new AppException(ErrorCode.VIDEO_NOT_EXISTED));
        String rejectReason = buildRejectReason(request);
        video.setStatus(VideoStatus.REJECTED);
        video.setRejectReason(rejectReason);
        video.setApprovedAt(null);
        return videoMapper.toVideoResponse(videoRepository.save(video));
    }

    public VideoResponse takeDownVideo(String id) {
        Video video = videoRepository.findWithAuthorById(id)
                .orElseThrow(() -> new AppException(ErrorCode.VIDEO_NOT_EXISTED));
        video.setStatus(VideoStatus.TAKEN_DOWN);
        return videoMapper.toVideoResponse(videoRepository.save(video));
    }

    @Transactional(readOnly = true)
    public PageResponse<VideoResponse> getFeed(int page, int size) {
        return PageResponse.from(videoRepository.findApprovedFeed(FEED_DECAY_FACTOR, PageRequest.of(page, size))
                .map(videoMapper::toVideoResponse));
    }

    @Transactional(readOnly = true)
    public PageResponse<VideoResponse> getLatestVideos(int page, int size) {
        return PageResponse.from(videoRepository.findByStatusOrderByApprovedAtDescCreatedAtDesc(
                        VideoStatus.APPROVED,
                        PageRequest.of(page, size))
                .map(videoMapper::toVideoResponse));
    }

    @Transactional
    public VideoResponse recordView(String id) {
        Video video = videoRepository.findWithAuthorById(id)
                .orElseThrow(() -> new AppException(ErrorCode.VIDEO_NOT_EXISTED));
        if (video.getStatus() != VideoStatus.APPROVED) throw new AppException(ErrorCode.VIDEO_NOT_APPROVED);
        videoRepository.incrementViewCountForApproved(id);
        video.setViewCount(video.getViewCount() + 1);
        return videoMapper.toVideoResponse(video);
    }

    @Transactional(readOnly = true)
    public VideoPlaybackResponse getPlayback(String id) {
        User currentUser = getCurrentUser();
        Video video = videoRepository.findWithAuthorById(id)
                .orElseThrow(() -> new AppException(ErrorCode.VIDEO_NOT_EXISTED));
        if (video.getStatus() != VideoStatus.APPROVED && !Objects.equals(video.getAuthor().getId(), currentUser.getId()))
            throw new AppException(ErrorCode.VIDEO_NOT_APPROVED);

        VideoPlaybackResponse response = videoMapper.toPlaybackResponse(video);
        response.setLiked(videoLikeRepository.existsByUserIdAndVideoId(currentUser.getId(), id));
        response.setFollowingAuthor(userFollowRepository.existsByFollowerIdAndAuthorId(currentUser.getId(), video.getAuthor().getId()));
        return response;
    }

    @Transactional
    public VideoLikeResponse like(String id) {
        User user = getCurrentActiveUser();
        Video video = videoRepository.findWithAuthorById(id)
                .orElseThrow(() -> new AppException(ErrorCode.VIDEO_NOT_EXISTED));
        if (video.getStatus() != VideoStatus.APPROVED) throw new AppException(ErrorCode.VIDEO_NOT_APPROVED);
        if (!videoLikeRepository.existsByUserIdAndVideoId(user.getId(), id)) {
            videoLikeRepository.save(VideoLike.builder().user(user).video(video).build());
            videoRepository.incrementLikeCount(id);
            video.setLikeCount(video.getLikeCount() + 1);
        }
        return VideoLikeResponse.builder()
                .videoId(video.getId())
                .liked(true)
                .likeCount(video.getLikeCount())
                .build();
    }

    @Transactional
    public VideoLikeResponse unlike(String id) {
        User user = getCurrentActiveUser();
        Video video = videoRepository.findWithAuthorById(id)
                .orElseThrow(() -> new AppException(ErrorCode.VIDEO_NOT_EXISTED));
        videoLikeRepository.findByUserIdAndVideoId(user.getId(), id).ifPresent(videoLike -> {
            videoLikeRepository.delete(videoLike);
            videoRepository.decrementLikeCount(id);
            video.setLikeCount(Math.max(0L, video.getLikeCount() - 1));
        });
        return VideoLikeResponse.builder()
                .videoId(video.getId())
                .liked(false)
                .likeCount(video.getLikeCount())
                .build();
    }

    @Transactional
    public void follow(String authorId) {
        User follower = getCurrentActiveUser();
        if (Objects.equals(follower.getId(), authorId)) throw new AppException(ErrorCode.CANNOT_FOLLOW_SELF);
        User author = userRepository.findById(authorId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        if (!userFollowRepository.existsByFollowerIdAndAuthorId(follower.getId(), authorId)) {
            userFollowRepository.save(UserFollow.builder().follower(follower).author(author).build());
            userFollowRepository.incrementFollowerCount(authorId);
            userFollowRepository.incrementFollowingCount(follower.getId());
        }
    }

    @Transactional
    public void unfollow(String authorId) {
        User follower = getCurrentActiveUser();
        userFollowRepository.findByFollowerIdAndAuthorId(follower.getId(), authorId).ifPresent(userFollow -> {
            userFollowRepository.delete(userFollow);
            userFollowRepository.decrementFollowerCount(authorId);
            userFollowRepository.decrementFollowingCount(follower.getId());
        });
    }

    @Transactional(readOnly = true)
    public PageResponse<VideoResponse> getMyVideos(VideoStatus status, int page, int size) {
        User user = getCurrentUser();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        var videos = status == null
                ? videoRepository.findByAuthorId(user.getId(), pageable)
                : videoRepository.findByAuthorIdAndStatus(user.getId(), status, pageable);
        return PageResponse.from(videos.map(videoMapper::toVideoResponse));
    }

    @Transactional
    public VideoResponse resubmit(String id, VideoResubmitRequest request) {
        User user = getCurrentActiveUser();
        Video oldVideo = videoRepository.findWithAuthorById(id)
                .orElseThrow(() -> new AppException(ErrorCode.VIDEO_NOT_EXISTED));
        if (!Objects.equals(oldVideo.getAuthor().getId(), user.getId())) throw new AppException(ErrorCode.VIDEO_ACCESS_DENIED);
        if (oldVideo.getStatus() != VideoStatus.REJECTED) throw new AppException(ErrorCode.CANNOT_RESUBMIT_VIDEO);

        MultipartFile videoFile = request.getVideoFile();
        String videoUrl = oldVideo.getVideoUrl();
        String coverUrl = oldVideo.getCoverUrl();
        String oldVideoUrl = oldVideo.getVideoUrl();
        String oldCoverUrl = oldVideo.getCoverUrl();
        boolean replaceVideoFile = videoFile != null && !videoFile.isEmpty();

        if (replaceVideoFile) {
            Duration duration = validateVideoFile(videoFile);
            videoUrl = storageService.uploadVideo(videoFile);
            coverUrl = uploadThumbnail(videoFile, duration);
        }

        String title = request.getTitle() != null && !request.getTitle().isBlank()
                ? request.getTitle().trim()
                : oldVideo.getTitle();

        oldVideo.setTitle(title);
        oldVideo.setVideoUrl(videoUrl);
        oldVideo.setCoverUrl(coverUrl);
        oldVideo.setStatus(VideoStatus.REVIEWING);
        oldVideo.setRejectReason(null);
        oldVideo.setApprovedAt(null);
        oldVideo.setLikeCount(0L);
        oldVideo.setViewCount(0L);
        oldVideo.setCommentCount(0L);
        oldVideo.setPublishedAt(Instant.now());

        Video updatedVideo = videoRepository.save(oldVideo);
        if (replaceVideoFile) {
            deleteOldMediaAfterCommit(oldVideoUrl, oldCoverUrl);
        }
        return videoMapper.toVideoResponse(updatedVideo);
    }

    @Transactional(readOnly = true)
    public CursorResponse<VideoResponse> getFeed(String cursor, int size) {
        FeedCursor feedCursor = decodeFeedCursor(cursor);
        Instant asOf = feedCursor == null ? Instant.now() : feedCursor.asOf();

        List<Video> videos = videoRepository.findApprovedFeedCursor(
                FEED_DECAY_FACTOR,
                asOf,
                feedCursor == null ? null : feedCursor.score(),
                feedCursor == null ? null : feedCursor.approvedAt(),
                feedCursor == null ? null : feedCursor.id(),
                size + 1
        );

        boolean hasNext = videos.size() > size;
        List<Video> pageItems = hasNext ? videos.subList(0, size) : videos;

        String nextCursor = null;
        if (hasNext && !pageItems.isEmpty()) {
            Video last = pageItems.get(pageItems.size() - 1);
            double score = calculateFeedScore(last, asOf);
            nextCursor = encodeFeedCursor(new FeedCursor(asOf, score, last.getApprovedAt(), last.getId()));
        }

        return CursorResponse.<VideoResponse>builder()
                .data(pageItems.stream().map(videoMapper::toVideoResponse).toList())
                .nextCursor(nextCursor)
                .hasNext(hasNext)
                .build();
    }

    @Transactional(readOnly = true)
    public CursorResponse<VideoResponse> getLatestVideos(String cursor, int size) {
        LatestVideoCursor latestCursor = decodeLatestVideoCursor(cursor);

        List<Video> videos = videoRepository.findLatestVideosCursor(
                VideoStatus.APPROVED,
                latestCursor == null ? null : latestCursor.approvedAt(),
                latestCursor == null ? null : latestCursor.createdAt(),
                latestCursor == null ? null : latestCursor.id(),
                PageRequest.of(0, size + 1)
        );

        boolean hasNext = videos.size() > size;
        List<Video> pageItems = hasNext ? videos.subList(0, size) : videos;

        String nextCursor = null;
        if (hasNext && !pageItems.isEmpty()) {
            Video last = pageItems.get(pageItems.size() - 1);
            nextCursor = encodeLatestVideoCursor(new LatestVideoCursor(
                    last.getApprovedAt(),
                    last.getCreatedAt(),
                    last.getId()
            ));
        }

        return CursorResponse.<VideoResponse>builder()
                .data(pageItems.stream().map(videoMapper::toVideoResponse).toList())
                .nextCursor(nextCursor)
                .hasNext(hasNext)
                .build();
    }

    @Transactional(readOnly = true)
    public CursorResponse<VideoResponse> getMyVideos(VideoStatus status, String cursor, int size) {
        User user = getCurrentUser();
        MyVideoCursor myVideoCursor = decodeMyVideoCursor(cursor);

        List<Video> videos = videoRepository.findMyVideosCursor(
                user.getId(),
                status,
                myVideoCursor == null ? null : myVideoCursor.createdAt(),
                myVideoCursor == null ? null : myVideoCursor.id(),
                PageRequest.of(0, size + 1)
        );

        boolean hasNext = videos.size() > size;
        List<Video> pageItems = hasNext ? videos.subList(0, size) : videos;

        String nextCursor = null;
        if (hasNext && !pageItems.isEmpty()) {
            Video last = pageItems.get(pageItems.size() - 1);
            nextCursor = encodeMyVideoCursor(new MyVideoCursor(last.getCreatedAt(), last.getId()));
        }

        return CursorResponse.<VideoResponse>builder()
                .data(pageItems.stream().map(videoMapper::toVideoResponse).toList())
                .nextCursor(nextCursor)
                .hasNext(hasNext)
                .build();
    }

    private Duration validateVideoFile(MultipartFile file) {
        if (file == null || file.isEmpty()) throw new AppException(ErrorCode.INVALID_VIDEO_FORMAT);
        if (file.getSize() > MAX_VIDEO_SIZE) throw new AppException(ErrorCode.VIDEO_FILE_TOO_LARGE);
        String filename = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase(Locale.ROOT);
        String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase(Locale.ROOT);
        boolean validFormat = filename.endsWith(".mp4") || filename.endsWith(".mov")
                || contentType.equals("video/mp4") || contentType.equals("video/quicktime");
        if (!validFormat) throw new AppException(ErrorCode.INVALID_VIDEO_FORMAT);

        Duration duration = videoMetadataService.getDuration(file);
        if (duration.compareTo(MIN_DURATION) < 0 || duration.compareTo(MAX_DURATION) > 0)
            throw new AppException(ErrorCode.INVALID_VIDEO_DURATION);
        return duration;
    }

    private String uploadThumbnail(MultipartFile videoFile, Duration duration) {
        return videoMetadataService.extractRandomThumbnail(videoFile, duration)
                .map(storageService::uploadThumbnail)
                .orElse(null);
    }

    private void deleteOldMediaAfterCommit(String oldVideoUrl, String oldCoverUrl) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                storageService.delete(oldVideoUrl);
                if (oldCoverUrl != null && !Objects.equals(oldCoverUrl, oldVideoUrl)) {
                    storageService.delete(oldCoverUrl);
                }
            }
        });
    }

    private String buildRejectReason(VideoRejectRequest request) {
        if (request == null) throw new AppException(ErrorCode.INVALID_REJECT_REASON);
        String customReason = request.getCustomReason() == null ? "" : request.getCustomReason().trim();
        if (!customReason.isBlank()) return customReason;
        if (request.getReasonType() != null) return request.getReasonType().name();
        throw new AppException(ErrorCode.INVALID_REJECT_REASON);
    }

    private User getCurrentActiveUser() {
        User user = getCurrentUser();
        if (user.getAccountStatus() == AccountStatus.CANCELLED || Boolean.FALSE.equals(user.getEnabled()))
            throw new AppException(ErrorCode.ACCOUNT_CANCELLED);
        return user;
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }

    private double calculateFeedScore(Video video, Instant asOf) {
        long hoursSinceApproval = Duration.between(video.getApprovedAt(), asOf).toHours();
        return video.getLikeCount() + video.getViewCount() - (hoursSinceApproval * FEED_DECAY_FACTOR);
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

    private String encodeFeedCursor(FeedCursor cursor) {
        return encode(cursor.asOf() + "|" + cursor.score() + "|" + cursor.approvedAt() + "|" + cursor.id());
    }

    private FeedCursor decodeFeedCursor(String cursor) {
        String decoded = decode(cursor);
        if (decoded == null) return null;

        try {
            String[] parts = decoded.split("\\|");
            if (parts.length != 4) throw new AppException(ErrorCode.INVALID_CURSOR);

            return new FeedCursor(
                    Instant.parse(parts[0]),
                    Double.parseDouble(parts[1]),
                    Instant.parse(parts[2]),
                    parts[3]
            );
        } catch (IllegalArgumentException e) {
            throw new AppException(ErrorCode.INVALID_CURSOR);
        }
    }

    private String encodeLatestVideoCursor(LatestVideoCursor cursor) {
        return encode(cursor.approvedAt() + "|" + cursor.createdAt() + "|" + cursor.id());
    }

    private LatestVideoCursor decodeLatestVideoCursor(String cursor) {
        String decoded = decode(cursor);
        if (decoded == null) return null;

        try {
            String[] parts = decoded.split("\\|");
            if (parts.length != 3) throw new AppException(ErrorCode.INVALID_CURSOR);

            return new LatestVideoCursor(
                    Instant.parse(parts[0]),
                    Instant.parse(parts[1]),
                    parts[2]
            );
        } catch (IllegalArgumentException e) {
            throw new AppException(ErrorCode.INVALID_CURSOR);
        }
    }

    private String encodeMyVideoCursor(MyVideoCursor cursor) {
        return encode(cursor.createdAt() + "|" + cursor.id());
    }

    private MyVideoCursor decodeMyVideoCursor(String cursor) {
        String decoded = decode(cursor);
        if (decoded == null) return null;

        try {
            String[] parts = decoded.split("\\|");
            if (parts.length != 2) throw new AppException(ErrorCode.INVALID_CURSOR);

            return new MyVideoCursor(
                    Instant.parse(parts[0]),
                    parts[1]
            );
        } catch (IllegalArgumentException e) {
            throw new AppException(ErrorCode.INVALID_CURSOR);
        }
    }

    private record FeedCursor(
            Instant asOf,
            double score,
            Instant approvedAt,
            String id
    ) {
    }

    private record LatestVideoCursor(
            Instant approvedAt,
            Instant createdAt,
            String id
    ) {
    }

    private record MyVideoCursor(
            Instant createdAt,
            String id
    ) {
    }

}
