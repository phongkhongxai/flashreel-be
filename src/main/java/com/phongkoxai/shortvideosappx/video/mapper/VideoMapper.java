package com.phongkoxai.shortvideosappx.video.mapper;

import com.phongkoxai.shortvideosappx.video.dto.response.VideoPlaybackResponse;
import com.phongkoxai.shortvideosappx.video.dto.response.VideoResponse;
import com.phongkoxai.shortvideosappx.video.dto.response.VideoReviewResponse;
import com.phongkoxai.shortvideosappx.video.entity.Video;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface VideoMapper {
    @Mapping(target = "authorId", source = "author.id")
    @Mapping(target = "authorNickname", source = "author.nickname")
    VideoResponse toVideoResponse(Video video);

    @Mapping(target = "uploadTime", source = "createdAt")
    @Mapping(target = "videoPreviewUrl", source = "videoUrl")
    @Mapping(target = "authorNickname", source = "author.nickname")
    @Mapping(target = "authorEmail", source = "author.email")
    @Mapping(target = "authorAvatar", source = "author.avatarUrl")
    VideoReviewResponse toReviewResponse(Video video);

    @Mapping(target = "authorId", source = "author.id")
    @Mapping(target = "authorAvatar", source = "author.avatarUrl")
    @Mapping(target = "authorNickname", source = "author.nickname")
    @Mapping(target = "liked", ignore = true)
    @Mapping(target = "followingAuthor", ignore = true)
    VideoPlaybackResponse toPlaybackResponse(Video video);
}
