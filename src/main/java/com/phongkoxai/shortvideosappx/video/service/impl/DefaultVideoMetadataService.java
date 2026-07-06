package com.phongkoxai.shortvideosappx.video.service.impl;

import com.phongkoxai.shortvideosappx.video.service.VideoMetadataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class DefaultVideoMetadataService implements VideoMetadataService {
    @Value("${app.video.ffmpeg-path:ffmpeg}")
    String ffmpegPath;

    @Value("${app.video.thumbnail-timeout-seconds:15}")
    long thumbnailTimeoutSeconds;

    @Override
    public Duration getDuration(MultipartFile file) {
        // TODO Integrate FFprobe/FFmpeg or cloud media metadata provider.
        // Until that integration exists, return a valid MVP duration so request flow remains complete.
        return Duration.ofSeconds(5);
    }

    @Override
    public Optional<MultipartFile> extractRandomThumbnail(MultipartFile file, Duration duration) {
        if (file == null || file.isEmpty()) return Optional.empty();

        Path inputPath = null;
        Path thumbnailPath = null;
        try {
            inputPath = Files.createTempFile("flashreel-video-", extensionOf(file.getOriginalFilename()));
            thumbnailPath = Files.createTempFile("flashreel-thumbnail-", ".jpg");
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, inputPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }

            long captureSecond = randomCaptureSecond(duration);
            Process process = new ProcessBuilder(
                    ffmpegPath,
                    "-y",
                    "-ss", String.valueOf(captureSecond),
                    "-i", inputPath.toAbsolutePath().toString(),
                    "-frames:v", "1",
                    "-q:v", "2",
                    thumbnailPath.toAbsolutePath().toString()
            )
                    .redirectErrorStream(true)
                    .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                    .start();

            boolean finished = process.waitFor(thumbnailTimeoutSeconds, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                log.warn("FFmpeg thumbnail extraction timed out");
                return Optional.empty();
            }

            if (process.exitValue() != 0 || Files.size(thumbnailPath) == 0) {
                log.warn("FFmpeg thumbnail extraction failed with exit code {}", process.exitValue());
                return Optional.empty();
            }

            byte[] thumbnailBytes = Files.readAllBytes(thumbnailPath);
            return Optional.of(new ByteArrayMultipartFile(
                    "thumbnail",
                    UUID.randomUUID() + ".jpg",
                    "image/jpeg",
                    thumbnailBytes
            ));
        } catch (Exception e) {
            log.warn("Cannot extract video thumbnail. Configure app.video.ffmpeg-path if FFmpeg is not on PATH.", e);
            return Optional.empty();
        } finally {
            deleteQuietly(inputPath);
            deleteQuietly(thumbnailPath);
        }
    }

    private long randomCaptureSecond(Duration duration) {
        long seconds = duration == null ? 1 : Math.max(1, duration.toSeconds());

        if (seconds <= 2) return 0;

        return ThreadLocalRandom.current().nextLong(0, seconds - 1);
    }

    private String extensionOf(String filename) {
        if (filename == null || filename.isBlank() || !filename.contains(".")) return ".mp4";
        return filename.substring(filename.lastIndexOf("."));
    }

    private void deleteQuietly(Path path) {
        if (path == null) return;
        try {
            Files.deleteIfExists(path);
        } catch (Exception ignored) {
        }
    }
}
