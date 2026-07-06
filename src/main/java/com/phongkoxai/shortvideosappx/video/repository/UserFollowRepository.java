package com.phongkoxai.shortvideosappx.video.repository;

import com.phongkoxai.shortvideosappx.video.entity.UserFollow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserFollowRepository extends JpaRepository<UserFollow, String> {
    boolean existsByFollowerIdAndAuthorId(String followerId, String authorId);

    Optional<UserFollow> findByFollowerIdAndAuthorId(String followerId, String authorId);

    @Modifying
    @Query("update User u set u.followerCount = u.followerCount + 1 where u.id = :authorId")
    int incrementFollowerCount(@Param("authorId") String authorId);

    @Modifying
    @Query("update User u set u.followerCount = case when u.followerCount > 0 then u.followerCount - 1 else 0 end where u.id = :authorId")
    int decrementFollowerCount(@Param("authorId") String authorId);

    @Modifying
    @Query("update User u set u.followingCount = u.followingCount + 1 where u.id = :followerId")
    int incrementFollowingCount(@Param("followerId") String followerId);

    @Modifying
    @Query("update User u set u.followingCount = case when u.followingCount > 0 then u.followingCount - 1 else 0 end where u.id = :followerId")
    int decrementFollowingCount(@Param("followerId") String followerId);
}
