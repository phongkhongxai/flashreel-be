package com.phongkoxai.shortvideosappx.account.service;

import com.phongkoxai.shortvideosappx.account.dto.request.AccountCancellationConfirmRequest;
import com.phongkoxai.shortvideosappx.auth.entity.AccountStatus;
import com.phongkoxai.shortvideosappx.auth.entity.User;
import com.phongkoxai.shortvideosappx.auth.repository.UserRepository;
import com.phongkoxai.shortvideosappx.common.exception.AppException;
import com.phongkoxai.shortvideosappx.common.exception.ErrorCode;
import com.phongkoxai.shortvideosappx.video.config.VideoRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AccountService {
    UserRepository userRepository;
    VideoRepository videoRepository;
    StringRedisTemplate redisTemplate;

    public String requestCancellation() {
        User user = getCurrentUser();
        if (user.getAccountStatus() == AccountStatus.CANCELLED) throw new AppException(ErrorCode.ACCOUNT_CANCELLED);
        redisTemplate.opsForValue().set(cancellationKey(user.getId()), "requested", 10, TimeUnit.MINUTES);
        return "Account cancellation requested. Please confirm within 10 minutes.";
    }

    @Transactional
    public String confirmCancellation(AccountCancellationConfirmRequest request) {
        if (request == null || !Boolean.TRUE.equals(request.getConfirmed()))
            throw new AppException(ErrorCode.CONFIRMATION_REQUIRED);

        User user = getCurrentUser();
        if (!redisTemplate.hasKey(cancellationKey(user.getId()))) throw new AppException(ErrorCode.CONFIRMATION_REQUIRED);

        String suffix = UUID.randomUUID().toString();
        user.setNickname("Deleted User");
        user.setDisplayName("Deleted User");
        user.setAvatarUrl(null);
        user.setBio(null);
        user.setEmail("deleted-" + suffix + "@deleted.local");
        user.setUsername("deleted-" + suffix);
        user.setEnabled(false);
        user.setAccountStatus(AccountStatus.CANCELLED);
        userRepository.save(user);
        videoRepository.takeDownByAuthorId(user.getId());
        redisTemplate.delete(cancellationKey(user.getId()));
        return "Account has been cancelled.";
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }

    private String cancellationKey(String userId) {
        return "account-cancellation:" + userId;
    }
}
