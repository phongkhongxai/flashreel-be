package com.phongkoxai.shortvideosappx.common.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CursorResponse<T> {
    private List<T> data;
    private String nextCursor;
    private boolean hasNext;
}