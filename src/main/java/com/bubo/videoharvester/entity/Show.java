package com.bubo.videoharvester.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@Entity
@Table(name = "shows")
public class Show {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String url;

    private String path;

    private String provider;

    private Boolean isEnabled;

    @OneToMany(mappedBy = "show", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Video> videos;
}
