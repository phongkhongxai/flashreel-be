package com.phongkoxai.shortvideosappx.account.controller;

import com.phongkoxai.shortvideosappx.account.dto.request.AccountCancellationConfirmRequest;
import com.phongkoxai.shortvideosappx.account.service.AccountService;
import com.phongkoxai.shortvideosappx.common.response.ApiResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/account")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AccountController {
    AccountService accountService;

    @PostMapping("/cancellation/request")
    ApiResponse<String> requestCancellation() {
        return ApiResponse.<String>builder().result(accountService.requestCancellation()).build();
    }

    @PostMapping("/cancellation/confirm")
    ApiResponse<String> confirmCancellation(@RequestBody AccountCancellationConfirmRequest request) {
        return ApiResponse.<String>builder().result(accountService.confirmCancellation(request)).build();
    }
}
