package com.bubo.videoharvester.dto;

import com.bubo.videoharvester.entity.Video;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class VideoDTO {

    private Long id;

    private String title;

    private String url;

    private String filePath;

    private LocalDateTime downloadTimestamp;

    private int retryCount = 0;

    private Double progress = 0.0;

    private LocalDateTime nextRetryTimestamp;

    private Double fileSize = 0.0;

    private Video.Status status;

    public static VideoDTO fromEntity(Video video) {
        return new VideoDTO(
                video.getId(),
                video.getTitle(),
                video.getUrl(),
                video.getFilePath(),
                video.getDownloadTimestamp(),
                video.getRetryCount(),
                video.getProgress(),
                video.getNextRetryTimestamp(),
                video.getFileSize(),
                video.getStatus()
        );
    }
}
