package com.phongkoxai.shortvideosappx.auth.dto.request;

import com.nimbusds.jose.JWEObjectJSON;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SendEmailRequest {
    String recipient;
    String subject;
}