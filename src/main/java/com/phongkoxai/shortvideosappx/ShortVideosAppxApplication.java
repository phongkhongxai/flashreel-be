package com.phongkoxai.shortvideosappx;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ShortVideosAppxApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShortVideosAppxApplication.class, args);
    }

}
