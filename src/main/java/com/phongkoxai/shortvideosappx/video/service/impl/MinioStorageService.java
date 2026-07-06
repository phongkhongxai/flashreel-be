package com.phongkoxai.shortvideosappx.video.service.impl;

import com.phongkoxai.shortvideosappx.common.exception.AppException;
import com.phongkoxai.shortvideosappx.common.exception.ErrorCode;
import com.phongkoxai.shortvideosappx.video.config.MinioProperties;
import com.phongkoxai.shortvideosappx.video.service.StorageService;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MinioStorageService implements StorageService {
    MinioClient minioClient;
    MinioProperties minioProperties;

    @Override
    public String uploadAvatar(MultipartFile file) {
        return upload("avatars", file);
    }

    @Override
    public String uploadVideo(MultipartFile file) {
        return upload("videos", file);
    }

    @Override
    public String uploadThumbnail(MultipartFile file) {
        return upload("thumbnails", file);
    }

    @Override
    public void delete(String objectKeyOrUrl) {
        if (objectKeyOrUrl == null || objectKeyOrUrl.isBlank()) return;
        try {
            String objectKey = resolveObjectKey(objectKeyOrUrl);
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(minioProperties.getBucket())
                    .object(objectKey)
                    .build());
        } catch (Exception e) {
            throw new AppException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    private String upload(String directory, MultipartFile file) {
        if (file == null || file.isEmpty()) throw new AppException(ErrorCode.FILE_UPLOAD_FAILED);
        try {
            String objectKey = directory + "/" + UUID.randomUUID() + "-" + sanitizeFilename(file.getOriginalFilename());
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(minioProperties.getBucket())
                    .object(objectKey)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(resolveContentType(file))
                    .build());
            return publicUrl(objectKey);
        } catch (Exception e) {
            throw new AppException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    private String sanitizeFilename(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) return "file";
        return originalFilename.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private String resolveContentType(MultipartFile file) {
        return file.getContentType() == null || file.getContentType().isBlank()
                ? "application/octet-stream"
                : file.getContentType();
    }

    private String publicUrl(String objectKey) {
        return trimTrailingSlash(minioProperties.getPublicUrl()) + "/"
                + minioProperties.getBucket() + "/"
                + objectKey;
    }

    private String resolveObjectKey(String objectKeyOrUrl) throws URISyntaxException {
        if (!objectKeyOrUrl.startsWith("http://") && !objectKeyOrUrl.startsWith("https://")) {
            return trimLeadingSlash(objectKeyOrUrl);
        }

        URI uri = new URI(objectKeyOrUrl);
        String path = trimLeadingSlash(uri.getPath());
        String bucketPrefix = minioProperties.getBucket() + "/";
        return path.startsWith(bucketPrefix) ? path.substring(bucketPrefix.length()) : path;
    }

    private String trimTrailingSlash(String value) {
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    private String trimLeadingSlash(String value) {
        return value.startsWith("/") ? value.substring(1) : value;
    }
}
