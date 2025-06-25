package com.bubo.videoharvester.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
    private LocalDateTime downloadTimestamp;
    private String show;

    public Video() {
    }

    public Video(String title, String url, String show) {
        this.title = title;
        this.url = url;
        this.show = show;
    }
}
