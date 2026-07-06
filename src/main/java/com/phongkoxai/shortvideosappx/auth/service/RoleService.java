package com.phongkoxai.shortvideosappx.auth.service;


import com.phongkoxai.shortvideosappx.auth.dto.request.RoleRequest;
import com.phongkoxai.shortvideosappx.auth.dto.response.RoleResponse;
import com.phongkoxai.shortvideosappx.auth.mapper.RoleMapper;
import com.phongkoxai.shortvideosappx.auth.repository.RoleRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoleService {
    RoleRepository roleRepository;
    RoleMapper roleMapper;

    public RoleResponse create(RoleRequest request) {
        var role = roleMapper.toRole(request);
        role = roleRepository.save(role);
        return roleMapper.toRoleResponse(role);
    }

    public List<RoleResponse> getAll() {
        return roleRepository.findAll().stream().map(roleMapper::toRoleResponse).toList();
    }

//    public void delete(String role) {
//        roleRepository.deleteById(role);
//    }
}
