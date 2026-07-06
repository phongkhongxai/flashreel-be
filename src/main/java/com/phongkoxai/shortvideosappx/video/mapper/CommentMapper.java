package com.phongkoxai.shortvideosappx.video.mapper;

import com.phongkoxai.shortvideosappx.video.dto.response.CommentResponse;
import com.phongkoxai.shortvideosappx.video.entity.VideoComment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CommentMapper {
    @Mapping(target = "videoId", source = "video.id")
    @Mapping(target = "authorId", source = "author.id")
    @Mapping(target = "authorNickname", source = "author.nickname")
    @Mapping(target = "authorAvatar", source = "author.avatarUrl")
    CommentResponse toCommentResponse(VideoComment comment);
}
