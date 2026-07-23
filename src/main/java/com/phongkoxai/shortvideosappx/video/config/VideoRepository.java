package com.phongkoxai.shortvideosappx.video.config;

import com.phongkoxai.shortvideosappx.video.entity.Video;
import com.phongkoxai.shortvideosappx.video.enums.VideoStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface VideoRepository extends JpaRepository<Video, String> {
    Page<Video> findByStatus(VideoStatus status, Pageable pageable);

    Page<Video> findByAuthorId(String authorId, Pageable pageable);

    Page<Video> findByAuthorIdAndStatus(String authorId, VideoStatus status, Pageable pageable);

    Page<Video> findByStatusOrderByApprovedAtDescCreatedAtDesc(VideoStatus status, Pageable pageable);

    @Query("select v from Video v join fetch v.author where v.id = :id")
    Optional<Video> findWithAuthorById(@Param("id") String id);

    @Query(
            value = """
                    select * from videos v
                    where v.status = 'APPROVED'
                    order by (v.like_count + v.view_count - (timestampdiff(HOUR, v.approved_at, current_timestamp) * :decayFactor)) desc,
                             v.approved_at desc
                    """,
            countQuery = "select count(*) from videos v where v.status = 'APPROVED'",
            nativeQuery = true
    )
    Page<Video> findApprovedFeed(@Param("decayFactor") double decayFactor, Pageable pageable);

    @Query(
            value = """
                    select * from videos v
                    where v.status = 'APPROVED'
                      and (
                            :cursorScore is null
                            or (v.like_count + v.view_count - (timestampdiff(HOUR, v.approved_at, :asOf) * :decayFactor)) < :cursorScore
                            or (
                                (v.like_count + v.view_count - (timestampdiff(HOUR, v.approved_at, :asOf) * :decayFactor)) = :cursorScore
                                and v.approved_at < :cursorApprovedAt
                            )
                            or (
                                (v.like_count + v.view_count - (timestampdiff(HOUR, v.approved_at, :asOf) * :decayFactor)) = :cursorScore
                                and v.approved_at = :cursorApprovedAt
                                and v.id < :cursorId
                            )
                      )
                    order by (v.like_count + v.view_count - (timestampdiff(HOUR, v.approved_at, :asOf) * :decayFactor)) desc,
                             v.approved_at desc,
                             v.id desc
                    limit :limit
                    """,
            nativeQuery = true
    )
    List<Video> findApprovedFeedCursor(
            @Param("decayFactor") double decayFactor,
            @Param("asOf") Instant asOf,
            @Param("cursorScore") Double cursorScore,
            @Param("cursorApprovedAt") Instant cursorApprovedAt,
            @Param("cursorId") String cursorId,
            @Param("limit") int limit
    );

    @Query("""
            select v from Video v
            where v.status = :status
              and (
                    :cursorApprovedAt is null
                    or v.approvedAt < :cursorApprovedAt
                    or (
                        v.approvedAt = :cursorApprovedAt
                        and v.createdAt < :cursorCreatedAt
                    )
                    or (
                        v.approvedAt = :cursorApprovedAt
                        and v.createdAt = :cursorCreatedAt
                        and v.id < :cursorId
                    )
              )
            order by v.approvedAt desc, v.createdAt desc, v.id desc
            """)
    List<Video> findLatestVideosCursor(
            @Param("status") VideoStatus status,
            @Param("cursorApprovedAt") Instant cursorApprovedAt,
            @Param("cursorCreatedAt") Instant cursorCreatedAt,
            @Param("cursorId") String cursorId,
            Pageable pageable
    );

    @Query("""
            select v from Video v
            where v.author.id = :authorId
              and (:status is null or v.status = :status)
              and (
                    :cursorCreatedAt is null
                    or v.createdAt < :cursorCreatedAt
                    or (
                        v.createdAt = :cursorCreatedAt
                        and v.id < :cursorId
                    )
              )
            order by v.createdAt desc, v.id desc
            """)
    List<Video> findMyVideosCursor(
            @Param("authorId") String authorId,
            @Param("status") VideoStatus status,
            @Param("cursorCreatedAt") Instant cursorCreatedAt,
            @Param("cursorId") String cursorId,
            Pageable pageable
    );

    @Modifying
    @Query("update Video v set v.likeCount = v.likeCount + 1 where v.id = :videoId")
    int incrementLikeCount(@Param("videoId") String videoId);

    @Modifying
    @Query("update Video v set v.likeCount = case when v.likeCount > 0 then v.likeCount - 1 else 0 end where v.id = :videoId")
    int decrementLikeCount(@Param("videoId") String videoId);

    @Modifying
    @Query("update Video v set v.viewCount = v.viewCount + 1 where v.id = :videoId and v.status = com.phongkoxai.shortvideosappx.video.enums.VideoStatus.APPROVED")
    int incrementViewCountForApproved(@Param("videoId") String videoId);

    @Modifying
    @Query("update Video v set v.commentCount = coalesce(v.commentCount, 0) + 1 where v.id = :videoId")
    int incrementCommentCount(@Param("videoId") String videoId);

    @Modifying
    @Query("update Video v set v.commentCount = case when coalesce(v.commentCount, 0) > 0 then v.commentCount - 1 else 0 end where v.id = :videoId")
    int decrementCommentCount(@Param("videoId") String videoId);

    @Modifying
    @Query("update User u set u.videoCount = u.videoCount + 1 where u.id = :authorId")
    int incrementVideoCount(@Param("authorId") String authorId);

    @Modifying
    @Query("update Video v set v.status = com.phongkoxai.shortvideosappx.video.enums.VideoStatus.TAKEN_DOWN where v.author.id = :authorId")
    int takeDownByAuthorId(@Param("authorId") String authorId);
}
