package com.phongkoxai.shortvideosappx.auth.repository;

import com.phongkoxai.shortvideosappx.auth.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, String> {}
