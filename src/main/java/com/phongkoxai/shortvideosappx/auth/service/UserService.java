package com.phongkoxai.shortvideosappx.auth.service;

import com.phongkoxai.shortvideosappx.auth.dto.request.UserCreationRequest;
import com.phongkoxai.shortvideosappx.auth.dto.request.MyProfileUpdateRequest;
import com.phongkoxai.shortvideosappx.auth.dto.request.UserUpdateRequest;
import com.phongkoxai.shortvideosappx.auth.dto.response.UserResponse;
import com.phongkoxai.shortvideosappx.auth.entity.Role;
import com.phongkoxai.shortvideosappx.auth.entity.User;
import com.phongkoxai.shortvideosappx.auth.mapper.UserMapper;
import com.phongkoxai.shortvideosappx.auth.repository.RoleRepository;
import com.phongkoxai.shortvideosappx.auth.repository.UserRepository;
import com.phongkoxai.shortvideosappx.common.constant.PredefinedRole;
import com.phongkoxai.shortvideosappx.common.exception.AppException;
import com.phongkoxai.shortvideosappx.common.exception.ErrorCode;
import com.phongkoxai.shortvideosappx.video.service.StorageService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserService {
    UserRepository userRepository;
    RoleRepository roleRepository;
    UserMapper userMapper;
    EmailService emailService;
    PasswordEncoder passwordEncoder;
    StringRedisTemplate redisTemplate;
    StorageService storageService;

    @Transactional
    public String createUser(UserCreationRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) throw new AppException(ErrorCode.USER_EXISTED);

        var user = userMapper.toUser(request);

        Optional<User> existingUserOpt = userRepository.findByEmail(request.getEmail());

        if (existingUserOpt.isPresent()) {
            User existingUser = existingUserOpt.get();
            if (existingUser.getEmailVerified().equals(Boolean.TRUE)) {
                throw new AppException(ErrorCode.EMAIL_ALREADY_VERIFIED);
            } else {
                existingUser.setDisplayName(request.getDisplayName());
                existingUser.setPassword(passwordEncoder.encode(request.getPassword()));
                existingUser.setAvatarUrl("https://api.dicebear.com/7.x/initials/svg?seed=" + existingUser.getUsername());
                userRepository.save(existingUser);
                sendVerificationEmail(existingUser);
                return "Verification code has been resent to your email. Please check your inbox.";
            }
        }

        user.setPassword(passwordEncoder.encode(request.getPassword()));
        HashSet<Role> roles = new HashSet<>();
        roleRepository.findById(PredefinedRole.USER_ROLE).ifPresent(roles::add);

        user.setRoles(roles);
        String avatarUrl = "https://api.dicebear.com/7.x/initials/svg?seed=" + request.getUsername();
        user.setAvatarUrl(avatarUrl);
        user = userRepository.save(user);
        sendVerificationEmail(user);
        return "Account registered successfully! Please check your email for the verification code.";
    }

    public UserResponse getMyInfo() {
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();

        User user = userRepository.findByUsername(name).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        return userMapper.toUserResponse(user);
    }

    @Transactional(readOnly = true)
    public UserResponse getMyProfile() {
        return getMyInfo();
    }

    @Transactional
    public UserResponse updateMyProfile(MyProfileUpdateRequest request) {
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();
        User user = userRepository.findByUsername(name).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        user.setNickname(request.getNickname().trim());
        user.setDisplayName(request.getNickname().trim());

        if (request.getAvatarFile() != null && !request.getAvatarFile().isEmpty()) {
            user.setAvatarUrl(storageService.uploadAvatar(request.getAvatarFile()));
        } else if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl());
        }

        return userMapper.toUserResponse(userRepository.save(user));
    }

    public UserResponse updateUser(String userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        userMapper.updateUser(user, request);
        return userMapper.toUserResponse(userRepository.save(user));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUser(String userId) {
        userRepository.deleteById(userId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> getUsers() {
        log.info("In method get Users");
        return userRepository.findAll().stream().map(userMapper::toUserResponse).toList();
    }

    public UserResponse getUser(String id) {
        return userMapper.toUserResponse(
                userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED)));
    }

    private void sendVerificationEmail(User user) {
        String otp = String.format("%06d", new Random().nextInt(999999));
        redisTemplate.opsForValue().set("otp-verify:" + user.getEmail(), otp, 10, TimeUnit.MINUTES);
        emailService.sendEmailVerify(user, otp);
    }
}
