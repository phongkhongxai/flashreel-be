package com.phongkoxai.shortvideosappx.auth.service;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.phongkoxai.shortvideosappx.auth.dto.request.*;
import com.phongkoxai.shortvideosappx.auth.dto.response.AuthenticationResponse;
import com.phongkoxai.shortvideosappx.auth.dto.response.ExchangeTokenResponse;
import com.phongkoxai.shortvideosappx.auth.dto.response.IntrospectResponse;
import com.phongkoxai.shortvideosappx.auth.entity.InvalidatedToken;
import com.phongkoxai.shortvideosappx.auth.entity.AccountStatus;
import com.phongkoxai.shortvideosappx.auth.entity.Role;
import com.phongkoxai.shortvideosappx.auth.entity.User;
import com.phongkoxai.shortvideosappx.auth.repository.InvalidatedTokenRepository;
import com.phongkoxai.shortvideosappx.auth.repository.UserRepository;
import com.phongkoxai.shortvideosappx.auth.repository.httpClient.OutboundIdentityClient;
import com.phongkoxai.shortvideosappx.auth.repository.httpClient.OutboundUserClient;
import com.phongkoxai.shortvideosappx.common.constant.PredefinedRole;
import com.phongkoxai.shortvideosappx.common.exception.AppException;
import com.phongkoxai.shortvideosappx.common.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationService {
    UserRepository userRepository;
    InvalidatedTokenRepository invalidatedTokenRepository;
    OutboundIdentityClient outboundIdentityClient;
    OutboundUserClient outboundUserClient;
    StringRedisTemplate redisTemplate;

    @NonFinal
    @Value("${jwt.signerKey}")
    protected String SIGNER_KEY;

    @NonFinal
    @Value("${jwt.valid-duration}")
    protected long VALID_DURATION;

    @NonFinal
    @Value("${jwt.refreshable-duration}")
    protected long REFRESHABLE_DURATION;
    @NonFinal
    @Value("${outbound.identity.client-id}")
    protected String CLIENT_ID;
    @NonFinal
    @Value("${outbound.identity.client-secret}")
    protected String CLIENT_SECRET;
    @NonFinal
    @Value("${outbound.identity.redirect-uri}")
    protected String REDIRECT_URI;
    @NonFinal
    protected String GRANT_TYPE = "authorization_code";

    public IntrospectResponse introspect(IntrospectRequest request) throws JOSEException, ParseException {
        var token = request.getToken();
        boolean isValid = true;

        try {
            verifyToken(token, false);
        } catch (AppException e) {
            isValid = false;
        }

        return IntrospectResponse.builder().valid(isValid).build();
    }



    public AuthenticationResponse outboundAuthenticate(String code, String codeVerifier){
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("code", code);
        form.add("code_verifier", codeVerifier);
        form.add("client_id", CLIENT_ID);
        form.add("client_secret", CLIENT_SECRET);
        form.add("redirect_uri", REDIRECT_URI);
        form.add("grant_type", GRANT_TYPE);

        ExchangeTokenResponse response =
                outboundIdentityClient.exchangeToken(form);
        log.info("TOKEN RESPONSE {}", response);

        // Get user info
        var userInfo = outboundUserClient.getUserInfo("json", response.getAccessToken());

        log.info("User Info {}", userInfo);

        Set<Role> roles = new HashSet<>();
        roles.add(Role.builder().name(PredefinedRole.USER_ROLE).build());

        // Onboard user
        var user = userRepository.findByEmail(userInfo.getEmail()).orElseGet(
                () ->{
                    PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
                    return userRepository.save(User.builder()
                                .email(userInfo.getEmail())
                                .username(generateUniqueUsername(userInfo.getEmail()))
                                .firstName(userInfo.getGivenName())
                                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                                .lastName(userInfo.getFamilyName())
                                .displayName(userInfo.getName())
                                .avatarUrl(userInfo.getPicture())
                                .emailVerified(true)
                                .roles(roles)
                        .build());
                });

        // Generate token
        var token = generateToken(user);

        return AuthenticationResponse.builder()
                .token(token)
                .username(user.getUsername())
                .build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        var user = userRepository
                .findByUsername(request.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        boolean authenticated = passwordEncoder.matches(request.getPassword(), user.getPassword());

        if (!authenticated) throw new AppException(ErrorCode.UNAUTHENTICATED);
        if (user.getEmailVerified().equals(Boolean.FALSE)) throw new AppException(ErrorCode.EMAIL_NOT_VERIFIED);

        if (user.getAccountStatus() == AccountStatus.CANCELLED || Boolean.FALSE.equals(user.getEnabled()))
            throw new AppException(ErrorCode.ACCOUNT_CANCELLED);

        var token = generateToken(user);

        return AuthenticationResponse.builder()
                .token(token)
                .username(user.getUsername())
                .authenticated(true).build();
    }

    public void logout(LogoutRequest request) throws ParseException, JOSEException {
        try {
            var signToken = verifyToken(request.getToken(), true);

            String jit = signToken.getJWTClaimsSet().getJWTID();
            Date expiryTime = signToken.getJWTClaimsSet().getExpirationTime();

            InvalidatedToken invalidatedToken =
                InvalidatedToken.builder().id(jit).expiryTime(expiryTime).build();

            invalidatedTokenRepository.save(invalidatedToken);
        } catch (AppException exception){
            log.info("Token already expired");
        }
    }

    public AuthenticationResponse refreshToken(RefreshRequest request) throws ParseException, JOSEException {
        var signedJWT = verifyToken(request.getToken(), true);

        var jit = signedJWT.getJWTClaimsSet().getJWTID();
        var expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        InvalidatedToken invalidatedToken =
                InvalidatedToken.builder().id(jit).expiryTime(expiryTime).build();

        invalidatedTokenRepository.save(invalidatedToken);

        var username = signedJWT.getJWTClaimsSet().getSubject();

        var user =
                userRepository.findByUsername(username).orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));

        if (user.getAccountStatus() == AccountStatus.CANCELLED || Boolean.FALSE.equals(user.getEnabled()))
            throw new AppException(ErrorCode.ACCOUNT_CANCELLED);

        var token = generateToken(user);

        return AuthenticationResponse.builder()
                .token(token)
                .username(username)
                .authenticated(true).build();
    }

    public String verifyEmailCode(String email, String otp) {
        String redisKey = "otp-verify:" + email;
        String savedOtp = redisTemplate.opsForValue().get(redisKey);
        if (savedOtp == null) {
            throw new AppException(ErrorCode.OTP_INVALID); // OTP timed out (10 mins)
        }
        if (!savedOtp.equals(otp)) {
            throw new AppException(ErrorCode.OTP_INVALID); // Wrong code
        }
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        user.setEmailVerified(true);
        userRepository.save(user);
        redisTemplate.delete(redisKey);
        return "Email verified successfully!";
    }

    private String generateToken(User user) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getUsername())
                .claim("userId", user.getId())
                .issuer("phongkoxai.vn")
                .issueTime(new Date())
                .expirationTime(new Date(
                        Instant.now().plus(VALID_DURATION, ChronoUnit.SECONDS).toEpochMilli()
                ))
                .jwtID(UUID.randomUUID().toString())
                .claim("scope", buildScope(user))
                .build();
        Payload payload = new Payload(jwtClaimsSet.toJSONObject());

        JWSObject jwsObject = new JWSObject(header, payload);

        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Cannot create token", e);
            throw new RuntimeException(e);
        }
    }

    private SignedJWT verifyToken(String token, boolean isRefresh) throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());

        SignedJWT signedJWT = SignedJWT.parse(token);

        Date expiryTime = (isRefresh)
                ? new Date(signedJWT.getJWTClaimsSet().getIssueTime()
                    .toInstant().plus(REFRESHABLE_DURATION, ChronoUnit.SECONDS).toEpochMilli())
                : signedJWT.getJWTClaimsSet().getExpirationTime();

        var verified = signedJWT.verify(verifier);

        if (!(verified && expiryTime.after(new Date()))) throw new AppException(ErrorCode.UNAUTHENTICATED);

        if (invalidatedTokenRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID()))
            throw new AppException(ErrorCode.UNAUTHENTICATED);

        return signedJWT;
    }

    private String buildScope(User user) {
        StringJoiner stringJoiner = new StringJoiner(" ");
        if (!CollectionUtils.isEmpty(user.getRoles()))
            user.getRoles().forEach(role -> {
                stringJoiner.add("ROLE_" + role.getName());
            });

        return stringJoiner.toString();
    }

    private String generateUniqueUsername(String email) {
        String username = email.split("@")[0];
        while (userRepository.existsByUsername(username)) {
            username = username + System.currentTimeMillis();
        }
        return username;
    }
}
