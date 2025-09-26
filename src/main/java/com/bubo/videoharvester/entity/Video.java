package com.bubo.videoharvester.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Lazy;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
public class Video {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String url;

    private String filePath;

    private LocalDateTime downloadTimestamp;

    private int retryCount = 0;

    private Double progress = 0.0;

    private LocalDateTime nextRetryTimestamp;

    private Double fileSize = 0.0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "show_id", nullable = false)
    private Show show;

    @Enumerated(EnumType.STRING)
    private Status status;

    public Video() {

    }

    public Video(String title, String url, Show show) {

        this.title = title;
        this.url = url;
        this.show = show;
    }

    public void incrementRetryCount() {

        this.retryCount++;
    }

    public enum Status {
        DOWNLOADING, DOWNLOADED, FAILED, FAILED_PERMANENTLY, SKIPPED, DELETED
    }
}
