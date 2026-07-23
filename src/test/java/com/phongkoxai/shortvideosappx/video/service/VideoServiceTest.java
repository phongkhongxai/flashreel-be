package com.phongkoxai.shortvideosappx.video.service;

import com.phongkoxai.shortvideosappx.auth.entity.AccountStatus;
import com.phongkoxai.shortvideosappx.auth.entity.User;
import com.phongkoxai.shortvideosappx.auth.repository.UserRepository;
import com.phongkoxai.shortvideosappx.video.entity.Video;
import com.phongkoxai.shortvideosappx.video.enums.VideoStatus;
import com.phongkoxai.shortvideosappx.video.mapper.VideoMapper;
import com.phongkoxai.shortvideosappx.video.repository.UserFollowRepository;
import com.phongkoxai.shortvideosappx.video.repository.VideoLikeRepository;
import com.phongkoxai.shortvideosappx.video.config.VideoRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Instant;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VideoServiceTest {
    @Mock
    VideoRepository videoRepository;
    @Mock
    VideoLikeRepository videoLikeRepository;
    @Mock
    UserFollowRepository userFollowRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    StorageService storageService;
    @Mock
    VideoMetadataService videoMetadataService;
    @Mock
    VideoMapper videoMapper;

    @InjectMocks
    VideoService videoService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void likeAlreadyLikedVideoDoesNotIncrementCounter() {
        User user = activeUser("user-id", "user");
        Video video = approvedVideo(user);
        authenticateAs("user");
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        when(videoRepository.findWithAuthorById("video-id")).thenReturn(Optional.of(video));
        when(videoLikeRepository.existsByUserIdAndVideoId("user-id", "video-id")).thenReturn(true);

        videoService.like("video-id");

        verify(videoLikeRepository, never()).save(any());
        verify(videoRepository, never()).incrementLikeCount("video-id");
    }

    @Test
    void unlikeNotLikedVideoDoesNotDecrementCounter() {
        User user = activeUser("user-id", "user");
        Video video = approvedVideo(user);
        authenticateAs("user");
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        when(videoRepository.findWithAuthorById("video-id")).thenReturn(Optional.of(video));
        when(videoLikeRepository.findByUserIdAndVideoId("user-id", "video-id")).thenReturn(Optional.empty());

        videoService.unlike("video-id");

        verify(videoLikeRepository, never()).delete(any());
        verify(videoRepository, never()).decrementLikeCount("video-id");
    }

    private void authenticateAs(String username) {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(username);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    private User activeUser(String id, String username) {
        return User.builder()
                .id(id)
                .username(username)
                .nickname(username)
                .email(username + "@example.com")
                .accountStatus(AccountStatus.ACTIVE)
                .enabled(true)
                .build();
    }

    private Video approvedVideo(User author) {
        return Video.builder()
                .id("video-id")
                .title("Video")
                .videoUrl("/uploads/videos/video.mp4")
                .coverUrl("/uploads/videos/video.mp4")
                .author(author)
                .status(VideoStatus.APPROVED)
                .likeCount(0L)
                .viewCount(0L)
                .publishedAt(Instant.now())
                .approvedAt(Instant.now())
                .build();
    }
}
