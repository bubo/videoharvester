package com.bubo.videoharvester.downloaders;

import com.bubo.videoharvester.entity.Show;
import com.bubo.videoharvester.entity.Video;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

public abstract class BaseVideoDownloader {

    @Value("${videoharvester.video.download.script}")
    protected String downloadScript;

    protected abstract String getCssQuery();

    protected abstract String extractTitle(Element videoElement);

    protected abstract Logger getLogger();

    public List<Video> getVideos(Show show) {

        getLogger().info("Getting all videos...");
        try {
            Document doc = Jsoup.connect(show.getUrl()).get();

            return doc.select(getCssQuery()).stream().map(element -> parseVideo(element, show)).toList();

        } catch (IOException e) {
            getLogger().error("Error message: ", e);
        }
        return List.of();
    }

    public boolean downloadVideo(Video video, Show show) {

        try {
            getLogger().info("Downloading video with url: {}", video.getUrl());
            String file = show.getPath() + "/" + video.getTitle() + ".%(ext)s";
            ProcessBuilder processBuilder = new ProcessBuilder("/bin/bash", downloadScript, "-o", file, video.getUrl());
            processBuilder.inheritIO();
            Process process = processBuilder.start();
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                getLogger().info("Downloaded {} in {}", video.getTitle(), show.getPath());
                video.setDownloadTimestamp(LocalDateTime.now());
                video.setFilePath(file);
                return true;
            } else {
                getLogger().error("Download failed with code {}", exitCode);
                return false;
            }
        } catch (IOException | InterruptedException e) {
            getLogger().error("Error message: ", e);
            return false;
        }
    }

    private Video parseVideo(Element videoElement, Show show) {

        Element linkElement = videoElement.selectFirst("a");
        String videoUrl = linkElement != null
                          ? linkElement.absUrl("href")
                          : "";
        String title = extractTitle(videoElement);
        return new Video(title, videoUrl, show);
    }
}
