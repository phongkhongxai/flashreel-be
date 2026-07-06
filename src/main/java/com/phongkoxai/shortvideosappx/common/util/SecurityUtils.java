package com.phongkoxai.shortvideosappx.common.util;

import com.phongkoxai.shortvideosappx.common.exception.AppException;
import com.phongkoxai.shortvideosappx.common.exception.ErrorCode;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.UUID;

public class SecurityUtils {
    public static String getCurrentUserId0() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            return jwt.getClaim("userId");
        }
        throw new AppException(ErrorCode.UNAUTHENTICATED);
    }

//    public static UUID getCurrentUserId() {
//
//        ServletRequestAttributes attrs =
//                (ServletRequestAttributes)
//                        RequestContextHolder.currentRequestAttributes();
//
//        String userId =
//                attrs.getRequest().getHeader("X-User-Id");
//
//        return UUID.fromString(userId);
//    }
}
