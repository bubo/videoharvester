package com.bubo.videoharvester.repository;

import com.bubo.videoharvester.entity.Show;
import com.bubo.videoharvester.entity.Video;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public interface VideoRepository extends JpaRepository<Video, Long> {

    long countByShow(Show show);

    Set<Video> findByUrlIn(Set<String> urls);

    List<Video> findByShowAndStatusAndNextRetryTimestampBefore(Show show, Video.Status status, LocalDateTime now);

    List<Video> findByShowId(Long showId);
}