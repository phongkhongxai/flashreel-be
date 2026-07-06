package com.phongkoxai.shortvideosappx.auth.repository.httpClient;

import com.phongkoxai.shortvideosappx.auth.dto.response.OutboundUserResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange(
        url = "https://www.googleapis.com",
        accept = MediaType.APPLICATION_JSON_VALUE
)
public interface OutboundUserClient {

    @GetExchange("/oauth2/v1/userinfo")
    OutboundUserResponse getUserInfo(
            @RequestParam("alt") String alt,
            @RequestParam("access_token") String accessToken
    );

}