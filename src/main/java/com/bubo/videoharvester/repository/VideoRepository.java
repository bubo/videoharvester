package com.bubo.videoharvester.repository;

import com.bubo.videoharvester.entity.Show;
import com.bubo.videoharvester.entity.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public interface VideoRepository extends JpaRepository<Video, Long> {

    Video findByUrl(String url);

    boolean existsByUrl(String url);

    List<Video> findAllByShow(String show);

    long countByShow(Show show);

    @Query("SELECT DISTINCT v.show FROM Video v")
    List<String> findDistinctShowValues();

    Set<Video> findByUrlIn(Set<String> urls);

    List<Video> findByShowAndStatusAndNextRetryTimestampBefore(Show show, Video.Status status, LocalDateTime now);
}