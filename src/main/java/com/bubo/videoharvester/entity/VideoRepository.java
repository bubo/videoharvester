package com.bubo.videoharvester.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface VideoRepository extends JpaRepository<Video, Long> {

    Video findByUrl(String url);

    boolean existsByUrl(String url);

    List<Video> findAllByShow(String show);

    @Query("SELECT COUNT(v) FROM Video v WHERE v.show = :show")
    long countByShow(@Param("show") String show);

    @Query("SELECT DISTINCT v.show FROM Video v")
    List<String> findDistinctShowValues();
}