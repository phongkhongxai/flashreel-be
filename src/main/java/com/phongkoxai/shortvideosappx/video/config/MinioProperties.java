package com.phongkoxai.shortvideosappx.video.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.minio")
public class MinioProperties {
    String endpoint;
    String publicUrl;
    String accessKey;
    String secretKey;
    String bucket;
    boolean createBucket = true;
    boolean publicRead = true;
}
