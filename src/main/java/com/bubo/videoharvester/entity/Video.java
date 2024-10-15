package com.bubo.videoharvester.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.LocalDateTime;

@Entity
public class Video {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String url;
    private LocalDateTime downloadTimestamp;
    private String show;

    public Video() {
    }

    public Video(String title, String url, String show) {
        this.title = title;
        this.url = url;
        this.show = show;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public LocalDateTime getDownloadTimestamp() {
        return downloadTimestamp;
    }

    public void setDownloadTimestamp(LocalDateTime downloadTimestamp) {
        this.downloadTimestamp = downloadTimestamp;
    }

    public String getShow() {
        return show;
    }

    public void setShow(String show) {
        this.show = show;
    }

}
