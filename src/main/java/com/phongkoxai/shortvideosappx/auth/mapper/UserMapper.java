package com.phongkoxai.shortvideosappx.auth.mapper;


import com.phongkoxai.shortvideosappx.auth.dto.request.UserCreationRequest;
import com.phongkoxai.shortvideosappx.auth.dto.request.UserUpdateRequest;
import com.phongkoxai.shortvideosappx.auth.dto.response.UserResponse;
import com.phongkoxai.shortvideosappx.auth.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toUser(UserCreationRequest request);

    UserResponse toUserResponse(User user);

    @Mapping(target = "roles", ignore = true)
    void updateUser(@MappingTarget User user, UserUpdateRequest request);
}
