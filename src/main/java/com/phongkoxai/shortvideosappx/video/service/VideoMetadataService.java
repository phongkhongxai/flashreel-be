package com.phongkoxai.shortvideosappx.video.service;

import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.util.Optional;

public interface VideoMetadataService {
    Duration getDuration(MultipartFile file);

    Optional<MultipartFile> extractRandomThumbnail(MultipartFile file, Duration duration);
}
