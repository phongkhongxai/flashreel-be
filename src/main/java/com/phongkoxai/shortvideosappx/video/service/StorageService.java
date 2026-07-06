package com.phongkoxai.shortvideosappx.video.service;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    String uploadAvatar(MultipartFile file);
    String uploadVideo(MultipartFile file);
    String uploadThumbnail(MultipartFile file);
    void delete(String objectKey);
}
