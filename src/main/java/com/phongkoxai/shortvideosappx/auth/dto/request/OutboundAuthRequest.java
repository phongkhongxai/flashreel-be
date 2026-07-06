package com.phongkoxai.shortvideosappx.auth.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class OutboundAuthRequest {
    private String code;
    @JsonProperty("code_verifier")
    private String codeVerifier;
}

