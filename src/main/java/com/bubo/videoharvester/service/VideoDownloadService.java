package com.bubo.videoharvester.service;

import com.bubo.videoharvester.downloaders.BaseVideoDownloader;
import com.bubo.videoharvester.entity.Show;
import com.bubo.videoharvester.entity.Video;
import com.bubo.videoharvester.notifications.HomeAssistantNotifier;
import com.bubo.videoharvester.notifications.NotificationService;
import com.bubo.videoharvester.repository.ShowRepository;
import com.bubo.videoharvester.repository.VideoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.bubo.videoharvester.entity.Video.Status.DOWNLOADING;

@Service
public class VideoDownloadService {

    private static final Logger LOGGER = LoggerFactory.getLogger(VideoDownloadService.class);

    private final Map<String, BaseVideoDownloader> downloaderMap;

    private final VideoRepository videoRepository;

    private final ShowRepository showRepository;

    private final NotificationService notificationService;

    @Value("${videoharvester.video.max.retry.counter:10}")
    protected long maxRetryCounter;

    public VideoDownloadService(Map<String, BaseVideoDownloader> downloaderMap, VideoRepository videoRepository,
                                ShowRepository showRepository, HomeAssistantNotifier notificationService) {

        this.downloaderMap = downloaderMap;
        this.videoRepository = videoRepository;
        this.showRepository = showRepository;
        this.notificationService = notificationService;
    }

    public List<String> getProviders() {

        return downloaderMap.keySet().stream().sorted().toList();
    }

    @Async
    public void forceProcessVideosAsync() {

        processVideos();
    }

    public Long deleteVideoFile(Long videoId) {

        Video video = videoRepository.findById(videoId).orElseThrow();
        if (video.getFilePath() != null) {
            File myObj = new File(video.getFilePath());
            if (myObj.delete()) {
                video.setStatus(Video.Status.DELETED);
                video.setFilePath(null);
                videoRepository.save(video);
                LOGGER.info("Deleted video file: {}", video.getFilePath());
            } else {
                LOGGER.error("Failed to delete video file: {}", video.getFilePath());
            }
        }
        return video.getShow().getId();
    }

    @Scheduled(cron = "${videoharvester.cron}")
    public void processVideos() {

        List<Show> shows = showRepository.findAllByIsEnabledTrue();

        for (Show show : shows) {

            List<Video> allVideosToProcess = new ArrayList<>();
            List<Video> newVideos = getNewVideos(show);
            if (initialiseDatabase(show, newVideos)) {
                continue;
            }
            allVideosToProcess.addAll(newVideos);
            allVideosToProcess.addAll(
                    videoRepository.findByShowAndStatusAndNextRetryTimestampBefore(show, Video.Status.FAILED,
                            LocalDateTime.now()));

            for (Video video : allVideosToProcess) {

                video.setStatus(DOWNLOADING);
                videoRepository.save(video);
                notificationService.sendNotification("Starting download: {}", video.getTitle());

                if (downloaderMap.get(show.getProvider()).downloadVideo(video, show)) {

                    video.setStatus(Video.Status.DOWNLOADED);
                    notificationService.sendNotification("{} is downloaded", video.getTitle());
                } else {

                    processFailedVideo(video);
                    notificationService.sendNotification("{} failed downloading", video.getTitle());
                }
                videoRepository.save(video);
            }
        }
    }

    //package private for testing purposes
    boolean initialiseDatabase(Show show, List<Video> newVideos) {

        if (videoRepository.countByShow(show) == 0) {

            for (Video video : newVideos) {

                video.setStatus(Video.Status.SKIPPED);
                video.setDownloadTimestamp(LocalDateTime.of(1970, 1, 1, 0, 0));
            }
            videoRepository.saveAll(newVideos);
            return true;
        }
        return false;
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
