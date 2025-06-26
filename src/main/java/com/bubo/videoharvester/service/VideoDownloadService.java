package com.bubo.videoharvester.service;

import com.bubo.videoharvester.HomeAssistantNotifier;
import com.bubo.videoharvester.downloaders.BaseVideoDownloader;
import com.bubo.videoharvester.entity.Show;
import com.bubo.videoharvester.entity.Video;
import com.bubo.videoharvester.repository.ShowRepository;
import com.bubo.videoharvester.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.bubo.videoharvester.entity.Video.Status.DOWNLOADING;

@Service
public class VideoDownloadService {

    private final Map<String, BaseVideoDownloader> downloaderMap;

    private final VideoRepository videoRepository;

    private final ShowRepository showRepository;

    private final HomeAssistantNotifier homeAssistantNotifier;

    @Value("${videoharvester.video.max.retry.counter:10}")
    protected long maxRetryCounter;

    @Autowired
    public VideoDownloadService(Map<String, BaseVideoDownloader> downloaderMap, VideoRepository videoRepository,
                                ShowRepository showRepository, HomeAssistantNotifier homeAssistantNotifier) {

        this.downloaderMap = downloaderMap;
        this.videoRepository = videoRepository;
        this.showRepository = showRepository;
        this.homeAssistantNotifier = homeAssistantNotifier;
    }

    @Scheduled(cron = "${videoharvester.cron}")
    public void processVideos() {

        List<Show> shows = showRepository.findAllByEnabledTrue();

        for (Show show : shows) {

            List<Video> allVideosToProcess = new ArrayList<>();
            allVideosToProcess.addAll(getNewVideos(show));
            allVideosToProcess.addAll(
                    videoRepository.findByShowAndStatusAndNextRetryTimestampBefore(show, Video.Status.FAILED,
                                                                                   LocalDateTime.now()));

            for (Video video : allVideosToProcess) {

                video.setStatus(DOWNLOADING);
                videoRepository.save(video);
                homeAssistantNotifier.sendNotification("Starting download: {}", video.getTitle());

                if (downloaderMap.get(show.getProvider()).downloadVideo(video, show)) {

                    video.setStatus(Video.Status.DOWNLOADED);
                    homeAssistantNotifier.sendNotification("{} is downloaded", video.getTitle());
                } else {

                    processFailedVideo(video);
                    homeAssistantNotifier.sendNotification("{} failed downloading", video.getTitle());
                }
                videoRepository.save(video);
            }
        }
    }

    private void processFailedVideo(Video video) {

        video.incrementRetryCount();

        if (video.getRetryCount() >= maxRetryCounter) {

            video.setStatus(Video.Status.FAILED_PERMANENTLY);

        } else {
            long delayMinutes = (long) Math.pow(2, video.getRetryCount());
            video.setNextRetryTimestamp(LocalDateTime.now().plusMinutes(delayMinutes));
            video.setStatus(Video.Status.FAILED);
        }
    }

    private List<Video> getNewVideos(Show show) {

        List<Video> videos = downloaderMap.get(show.getProvider()).getVideos(show);

        Set<String> urlsToCheck = videos.stream().map(Video::getUrl).collect(Collectors.toSet());

        Set<Video> existingVideos = new HashSet<>(videoRepository.findByUrlIn(urlsToCheck));

        Set<String> existingUrls = existingVideos.stream().map(Video::getUrl).collect(Collectors.toSet());

        return videos.stream().filter(v -> !existingUrls.contains(v.getUrl())).toList();
    }
}
