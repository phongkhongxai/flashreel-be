package com.phongkoxai.shortvideosappx.auth.mapper;

import com.phongkoxai.shortvideosappx.auth.dto.request.RoleRequest;
import com.phongkoxai.shortvideosappx.auth.dto.response.RoleResponse;
import com.phongkoxai.shortvideosappx.auth.entity.Role;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    Role toRole(RoleRequest request);

    RoleResponse toRoleResponse(Role role);
}
