package com.bubo.videoharvester;

import com.bubo.videoharvester.entity.Video;
import com.bubo.videoharvester.entity.VideoRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

@Component public class IgriNaVoliataDownloader {

    private static final String DIV_SHOW_THUMBNAIL_SHOW_WRAPPER_SC_VAU_9_Q_9_0_HK_ZS_KS =
            "div.ShowThumbnail__ShowWrapper-sc-vau9q9-0.hkZsKs";

    private static final String SHOW_NAME = "Igrite";

    private static final Logger logger = LoggerFactory.getLogger(IgriNaVoliataDownloader.class);

    @Autowired
    private VideoRepository videoRepository;

    @Autowired
    private HomeAssistantNotifier homeAssistantNotifier;

    @Value("${videoharvester.video.location.igri}")
    private String path;

    @Value("${videoharvester.video.download.script}")
    private String downloadScript;

    @Scheduled(fixedDelay = 60000)
    public void checkForNewVideo() {
        logger.info("Checking for new videos...");
        try {
            Document doc =
                    Jsoup.connect("https://play.nova.bg/tvshow/igri-na-volyata-balgariya/46").get();

            initializeDatabaseIfEmpty(doc);

            for (Element videoElement : doc.select(
                    DIV_SHOW_THUMBNAIL_SHOW_WRAPPER_SC_VAU_9_Q_9_0_HK_ZS_KS)) {

                Video video = parseVideo(videoElement);

                if (!videoRepository.existsByUrl(video.getUrl())) {
                    downloadVideo(video);
                }
            }

        } catch (IOException e) {
            logger.error("Error message: ", e);
        }
    }

    private void initializeDatabaseIfEmpty(Document doc) {
        if (videoRepository.countByShow(SHOW_NAME) == 0) {
            for (Element videoElement : doc.select(
                    DIV_SHOW_THUMBNAIL_SHOW_WRAPPER_SC_VAU_9_Q_9_0_HK_ZS_KS)) {

                Video video = parseVideo(videoElement);
                video.setDownloadTimestamp(LocalDateTime.MIN);
                videoRepository.save(video);
            }
        }
    }

    private void downloadVideo(Video video) {
        try {
            logger.info("Downloading video with url: {}", video.getUrl());
            String file = path + "/" + video.getTitle() + ".%(ext)s";
            ProcessBuilder processBuilder =
                    new ProcessBuilder("/bin/bash", downloadScript,
                            "-o", file,
                            video.getUrl());
            processBuilder.inheritIO();
            homeAssistantNotifier.sendNotification("Starting download: {}", video.getTitle());
            Process process = processBuilder.start();
            process.waitFor();
            process.onExit().thenAccept(p -> {
                try {
                    int exitCode = p.exitValue();
                    if (exitCode == 0) {
                        logger.info("Downloaded {} in {}", video.getTitle(), path);
                        video.setDownloadTimestamp(LocalDateTime.now());
                        videoRepository.save(video);
                        homeAssistantNotifier.sendNotification("{} is " + "downloaded",
                                video.getTitle());
                    } else {
                        logger.error("Download failed with code {}", exitCode);
                    }
                } catch (IllegalThreadStateException e) {
                    logger.error("Error message: ", e);
                }
            });
        } catch (IOException | InterruptedException e) {
            logger.error("Error message: ", e);
        }
    }

    private String extractTitle(Element videoElement) {
        String title = Optional.ofNullable(
                        videoElement.selectFirst("p.cards-shared__VideoTitle-sc-o5cgdb-0.eNzUXb"))
                .map(Element::text).orElse("");
        String subtitle = Optional.ofNullable(
                        videoElement.selectFirst("p.cards-shared__VideoSubTitle-sc-o5cgdb-1.eACHzS"))
                .map(Element::text).orElse("");

        String[] parts = title.split("\\(");
        String formattedTitle = parts[0].trim();
        String date = parts.length > 1 ? parts[1].replace(")", "").trim() : "";

        return formattedTitle + " " + subtitle + " (" + date + ")";
    }

    private Video parseVideo(Element videoElement) {
        Element linkElement = videoElement.selectFirst("a");
        String videoUrl = "";
        if (linkElement != null) {
            videoUrl = "https://play.nova.bg" + linkElement.attr("href");
        }

        String title = extractTitle(videoElement);
        return new Video(title, videoUrl, SHOW_NAME);
    }

}
