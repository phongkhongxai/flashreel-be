package com.phongkoxai.shortvideosappx.video.repository;

import com.phongkoxai.shortvideosappx.video.entity.Video;
import com.phongkoxai.shortvideosappx.video.entity.VideoComment;
import com.phongkoxai.shortvideosappx.video.enums.CommentStatus;
import com.phongkoxai.shortvideosappx.video.enums.VideoStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface VideoCommentRepository extends JpaRepository<VideoComment, String> {
    @EntityGraph(attributePaths = {"author"})
    Page<VideoComment> findByVideoIdAndStatusOrderByCreatedAtDesc(String videoId, CommentStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"author", "video"})
    Optional<VideoComment> findWithAuthorAndVideoById(String id);

    @Query("""
            select c from VideoComment c
            where c.video.id = :videoId
             and (
                    :cursorCreatedAt is null
                    or c.createdAt < :cursorCreatedAt
                    or (
                        c.createdAt = :cursorCreatedAt
                        and c.id < :cursorId
                    )
              )
            order by c.createdAt desc,c.id desc
            """)
    List<VideoComment> findCommentsCursor(
            @Param("videoId") String videoId,
            @Param("cursorCreatedAt") Instant cursorCreatedAt,
            @Param("cursorId") String cursorId,
            Pageable pageable
    );
}
