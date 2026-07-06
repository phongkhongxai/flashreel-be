package com.phongkoxai.shortvideosappx.auth.controller;

import com.nimbusds.jose.JOSEException;
import com.phongkoxai.shortvideosappx.auth.dto.request.AuthenticationRequest;
import com.phongkoxai.shortvideosappx.auth.dto.request.IntrospectRequest;
import com.phongkoxai.shortvideosappx.auth.dto.request.LogoutRequest;
import com.phongkoxai.shortvideosappx.auth.dto.request.RefreshRequest;
import com.phongkoxai.shortvideosappx.auth.dto.response.AuthenticationResponse;
import com.phongkoxai.shortvideosappx.auth.dto.response.IntrospectResponse;
import com.phongkoxai.shortvideosappx.auth.service.AuthenticationService;
import com.phongkoxai.shortvideosappx.common.response.ApiResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationController {
    AuthenticationService authenticationService;


    @PostMapping("/login")
    ApiResponse<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
        var result = authenticationService.authenticate(request);
        return ApiResponse.<AuthenticationResponse>builder().result(result).build();
    }

    @PostMapping("/outbound/authentication")
    ApiResponse<AuthenticationResponse> outboundAuthenticate(
            @RequestParam("code") String code,
            @RequestParam("codeVerifier") String codeVerifier
    ){
        var result = authenticationService.outboundAuthenticate(code, codeVerifier);
        return ApiResponse.<AuthenticationResponse>builder().result(result).build();
    }
    @PostMapping("/verify-code")
    public ResponseEntity<String> verifyEmailCode(@RequestParam("email") String email,
                                                  @RequestParam("code") String code) {
        String response = authenticationService.verifyEmailCode(email, code);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/introspect")
    ApiResponse<IntrospectResponse> authenticate(@RequestBody IntrospectRequest request)
            throws ParseException, JOSEException {
        var result = authenticationService.introspect(request);
        return ApiResponse.<IntrospectResponse>builder().result(result).build();
    }

    @PostMapping("/refresh")
    ApiResponse<AuthenticationResponse> authenticate(@RequestBody RefreshRequest request)
            throws ParseException, JOSEException {
        var result = authenticationService.refreshToken(request);
        return ApiResponse.<AuthenticationResponse>builder().result(result).build();
    }

    @PostMapping("/logout")
    ApiResponse<Void> logout(@RequestBody LogoutRequest request) throws ParseException, JOSEException {
        authenticationService.logout(request);
        return ApiResponse.<Void>builder().build();
    }
}
