package com.phongkoxai.shortvideosappx.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(1001, "Uncategorized error", HttpStatus.BAD_REQUEST),
    USER_EXISTED(1002, "User existed", HttpStatus.BAD_REQUEST),
    USERNAME_INVALID(1003, "Username must be at least {min} characters", HttpStatus.UNPROCESSABLE_ENTITY),
    INVALID_PASSWORD(1004, "Password must be at least {min} characters", HttpStatus.UNPROCESSABLE_ENTITY),
    USER_NOT_EXISTED(1005, "User not existed", HttpStatus.NOT_FOUND),
    UNAUTHENTICATED(1006, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1007, "You do not have permission", HttpStatus.FORBIDDEN),
    INVALID_DOB(1008, "Your age must be at least {min}", HttpStatus.UNPROCESSABLE_ENTITY),
    EMAIL_NOT_VERIFIED(1009, "Email is not verified.", HttpStatus.FORBIDDEN),
    INVALID_REFRESH_TOKEN(1010, "Invalid refresh token", HttpStatus.BAD_REQUEST),
    TOKEN_NOT_FOUND(1011, "Token not found.", HttpStatus.NOT_FOUND),
    CANNOT_CREATE_PROFILE(1012, "Cannot create user profile", HttpStatus.INTERNAL_SERVER_ERROR),
    USER_WAS_BANNED(1013, "User was banned.", HttpStatus.FORBIDDEN),
    EMAIL_ALREADY_VERIFIED(1014, "Email already verified.", HttpStatus.BAD_REQUEST),
    OTP_EXPIRED(1015, "OTP is exprired.", HttpStatus.BAD_REQUEST),
    INVALID_OTP(1016, "OTP invalid.", HttpStatus.BAD_REQUEST),
    SENT_OTP_TOO_FAST(1017, "Send OTP too fast. Wait 1 minute please.", HttpStatus.BAD_REQUEST),
    ROLE_NOT_FOUND(1018,"Role not found: ",HttpStatus.NOT_FOUND),
    PERMISSION_NOT_VALID(1019,"Permission not valid.",HttpStatus.NOT_FOUND),
    CANNOT_SEND_EMAIL(1020, "Cannot send email.", HttpStatus.BAD_REQUEST),
    INVALID_NICKNAME(1021, "Nickname must not be blank and must not exceed 100 characters", HttpStatus.BAD_REQUEST),
    OTP_INVALID(1022,"OTP invalid.", HttpStatus.BAD_REQUEST),
    VIDEO_NOT_EXISTED(2001, "Video not existed", HttpStatus.NOT_FOUND),
    INVALID_VIDEO_TITLE(2002, "Video title must be between 1 and 60 characters", HttpStatus.BAD_REQUEST),
    INVALID_VIDEO_FORMAT(2003, "Video format must be MP4 or MOV", HttpStatus.BAD_REQUEST),
    INVALID_VIDEO_DURATION(2004, "Video duration must be between 5 seconds and 3 minutes", HttpStatus.BAD_REQUEST),
    VIDEO_FILE_TOO_LARGE(2005, "Video file must not exceed 300 MB", HttpStatus.BAD_REQUEST),
    VIDEO_NOT_APPROVED(2006, "Video is not approved", HttpStatus.BAD_REQUEST),
    VIDEO_ACCESS_DENIED(2007, "You cannot access this video", HttpStatus.FORBIDDEN),
    INVALID_REJECT_REASON(2008, "Reject reason is required", HttpStatus.BAD_REQUEST),
    INVALID_VIDEO_STATUS(2009, "Invalid video status", HttpStatus.BAD_REQUEST),
    CANNOT_FOLLOW_SELF(2010, "You cannot follow yourself", HttpStatus.BAD_REQUEST),
    ACCOUNT_CANCELLED(2011, "Account has been cancelled", HttpStatus.FORBIDDEN),
    CONFIRMATION_REQUIRED(2012, "Confirmation is required", HttpStatus.BAD_REQUEST),
    CANNOT_RESUBMIT_VIDEO(2013, "Only rejected videos can be resubmitted", HttpStatus.BAD_REQUEST),
    FILE_UPLOAD_FAILED(2014, "Cannot upload file", HttpStatus.INTERNAL_SERVER_ERROR),
    THUMBNAIL_EXTRACTION_FAILED(2015, "Cannot extract video thumbnail. Please check FFmpeg configuration.", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_CURSOR(2016, "Invalid cursor", HttpStatus.BAD_REQUEST),
    COMMENT_NOT_EXISTED(2101, "Comment not existed", HttpStatus.NOT_FOUND),
    INVALID_COMMENT_CONTENT(2102, "Comment content must not be blank and must not exceed 500 characters", HttpStatus.BAD_REQUEST),
    COMMENT_ACCESS_DENIED(2103, "You cannot access this comment", HttpStatus.FORBIDDEN),

    ;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;
}
