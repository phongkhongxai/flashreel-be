package com.phongkoxai.shortvideosappx.account.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AccountCancellationConfirmRequest {
    Boolean confirmed;
}
