package com.bubo.videoharvester;

import com.bubo.videoharvester.entity.Show;
import com.bubo.videoharvester.entity.Video;
import com.bubo.videoharvester.repository.VideoRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.time.LocalDateTime;

public abstract class VideoDownloader {

    private final HomeAssistantNotifier homeAssistantNotifier;
    private final VideoRepository videoRepository;

    @Value("${videoharvester.video.download.script}")
    protected String downloadScript;

    @Autowired
    protected VideoDownloader(HomeAssistantNotifier homeAssistantNotifier, VideoRepository videoRepository) {
        this.homeAssistantNotifier = homeAssistantNotifier;
        this.videoRepository = videoRepository;
    }

    protected abstract String getShowName();

    protected abstract String getPath();

    protected abstract String getUrl();

    protected abstract String getCssQuery();

    protected abstract String extractTitle(Element videoElement);

    protected abstract Logger getLogger();

    public void checkForNewVideo() {
        getLogger().info("Checking for new videos...");
        try {
            Document doc = Jsoup.connect(getUrl()).get();
            initializeDatabaseIfEmpty(doc);
            for (Element videoElement : doc.select(getCssQuery())) {
                Video video = parseVideo(videoElement);
                if (!videoRepository.existsByUrl(video.getUrl())) {
                    downloadVideo(video);
                }
            }
        } catch (IOException e) {
            getLogger().error("Error message: ", e);
        }
    }

    private void initializeDatabaseIfEmpty(Document doc) {
        if (videoRepository.countByShow(getShowName()) == 0) {
            for (Element videoElement : doc.select(getCssQuery())) {
                Video video = parseVideo(videoElement);
                video.setDownloadTimestamp(LocalDateTime.of(1970, 1, 1, 0, 0));
                videoRepository.save(video);
            }
        }
    }

    private void downloadVideo(Video video) {
        try {
            getLogger().info("Downloading video with url: {}", video.getUrl());
            String file = getPath() + "/" + video.getTitle() + ".%(ext)s";
            ProcessBuilder processBuilder = new ProcessBuilder("/bin/bash", downloadScript, "-o", file, video.getUrl());
            processBuilder.inheritIO();
            homeAssistantNotifier.sendNotification("Starting download: {}", video.getTitle());
            Process process = processBuilder.start();
            process.waitFor();
            process.onExit().thenAccept(p -> {
                int exitCode = p.exitValue();
                if (exitCode == 0) {
                    getLogger().info("Downloaded {} in {}", video.getTitle(), getPath());
                    video.setDownloadTimestamp(LocalDateTime.now());
                    videoRepository.save(video);
                    homeAssistantNotifier.sendNotification("{} is downloaded", video.getTitle());
                } else {
                    getLogger().error("Download failed with code {}", exitCode);
                }
            });
        } catch (IOException | InterruptedException e) {
            getLogger().error("Error message: ", e);
        }
    }

    private Video parseVideo(Element videoElement) {
        Element linkElement = videoElement.selectFirst("a");
        String videoUrl = linkElement != null ? linkElement.absUrl("href") : "";
        String title = extractTitle(videoElement);
        return new Video(title, videoUrl, new Show());
    }
}