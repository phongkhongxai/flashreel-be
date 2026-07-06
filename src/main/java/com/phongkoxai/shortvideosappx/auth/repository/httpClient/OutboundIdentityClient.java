package com.phongkoxai.shortvideosappx.auth.repository.httpClient;

import com.phongkoxai.shortvideosappx.auth.dto.response.ExchangeTokenResponse;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange(
        url = "https://oauth2.googleapis.com",
        accept = MediaType.APPLICATION_JSON_VALUE
)
public interface OutboundIdentityClient {

    @PostExchange(
            url = "/token",
            contentType = MediaType.APPLICATION_FORM_URLENCODED_VALUE
    )
    ExchangeTokenResponse exchangeToken(
            @RequestBody MultiValueMap<String, String> form
    );

}