package com.phongkoxai.shortvideosappx.common.config;


import com.phongkoxai.shortvideosappx.auth.entity.Role;
import com.phongkoxai.shortvideosappx.auth.entity.User;
import com.phongkoxai.shortvideosappx.auth.repository.RoleRepository;
import com.phongkoxai.shortvideosappx.auth.repository.UserRepository;
import com.phongkoxai.shortvideosappx.common.constant.PredefinedRole;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ApplicationInitConfig {

    PasswordEncoder passwordEncoder;

    @NonFinal
    static final String ADMIN_USER_NAME = "admin";

    @NonFinal
    static final String ADMIN_PASSWORD = "admin";

    @Bean
    ApplicationRunner applicationRunner(UserRepository userRepository, RoleRepository roleRepository) {
        log.info("Initializing application.....");
        return args -> {
            roleRepository.findById(PredefinedRole.USER_ROLE).orElseGet(() -> roleRepository.save(Role.builder()
                    .name(PredefinedRole.USER_ROLE)
                    .build()));
            roleRepository.findById(PredefinedRole.ADMIN_ROLE).orElseGet(() -> roleRepository.save(Role.builder()
                    .name(PredefinedRole.ADMIN_ROLE)
                    .build()));
            roleRepository.findById(PredefinedRole.REVIEWER_ROLE).orElseGet(() -> roleRepository.save(Role.builder()
                    .name(PredefinedRole.REVIEWER_ROLE)
                    .build()));

            if (userRepository.findByUsername(ADMIN_USER_NAME).isEmpty()) {
                Role adminRole = roleRepository.findById(PredefinedRole.ADMIN_ROLE)
                        .orElseThrow(() -> new IllegalStateException("Admin role not initialized"));
                var roles = new HashSet<Role>();
                roles.add(adminRole);
                User user = User.builder()
                        .username(ADMIN_USER_NAME)
                        .password(passwordEncoder.encode(ADMIN_PASSWORD))
                        .displayName("Admin")
                        .firstName("Admin")
                        .lastName("Hehe")
                        .email("admin@gmail.com")
                        .dob(LocalDate.of(1990, 1, 1))
                        .roles(roles)
                        .build();

                userRepository.save(user);
                log.warn("admin user has been created with default password: admin, please change it");
            }
            log.info("Application initialization completed .....");
        };
    }
}
