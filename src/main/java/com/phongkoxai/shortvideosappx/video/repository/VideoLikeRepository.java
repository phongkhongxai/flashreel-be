package com.phongkoxai.shortvideosappx.video.repository;

import com.phongkoxai.shortvideosappx.video.entity.VideoLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VideoLikeRepository extends JpaRepository<VideoLike, String> {
    boolean existsByUserIdAndVideoId(String userId, String videoId);

    Optional<VideoLike> findByUserIdAndVideoId(String userId, String videoId);
}
