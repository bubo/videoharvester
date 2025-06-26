package com.bubo.videoharvester.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

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

    private LocalDateTime nextRetryTimestamp;

    @ManyToOne
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
        this.status = Status.PENDING;
    }

    public void incrementRetryCount() {

        this.retryCount++;
    }

    public enum Status {
        PENDING, DOWNLOADING, DOWNLOADED, FAILED, FAILED_PERMANENTLY, DELETED
    }
}
