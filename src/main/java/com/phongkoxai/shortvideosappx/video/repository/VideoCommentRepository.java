package com.phongkoxai.shortvideosappx.video.repository;

import com.phongkoxai.shortvideosappx.video.entity.VideoComment;
import com.phongkoxai.shortvideosappx.video.enums.CommentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VideoCommentRepository extends JpaRepository<VideoComment, String> {
    @EntityGraph(attributePaths = {"author"})
    Page<VideoComment> findByVideoIdAndStatusOrderByCreatedAtDesc(String videoId, CommentStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"author", "video"})
    Optional<VideoComment> findWithAuthorAndVideoById(String id);
}
