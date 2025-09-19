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
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class BaseVideoDownloader {

    @Value("${videoharvester.video.download.script}")
    protected String downloadScript;

    private static final Pattern SEASON_PATTERN = Pattern.compile("[Сс][Ее][Зз][Оо][Нн]\\s*[-:]*\\s*(\\d+)", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    private static final Pattern EPISODE_PATTERN = Pattern.compile("[Ее][Пп][Ии][Зз][Оо][Дд]\\s*[-:]*\\s*(\\d+)", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    private static final Pattern PART_PATTERN = Pattern.compile("[Чч][Аа][Сс][Тт]\\s*[-:]*\\s*(\\d+)", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

    protected abstract String getCssQuery();

    protected abstract String extractTitle(Element videoElement);

    protected abstract Logger getLogger();

    public List<Video> getVideos(Show show) {

        getLogger().info("Getting all videos...");
        try {
            Document doc = Jsoup.connect(show.getUrl()).get();

            return doc.select(getCssQuery()).stream().filter(element -> !element.children().isEmpty())
                    .map(element -> parseVideo(element, show)).filter(Objects::nonNull).toList();

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

        Matcher seasonMatcher = SEASON_PATTERN.matcher(title);
        Matcher episodeMatcher = EPISODE_PATTERN.matcher(title);
        Matcher partMatcher = PART_PATTERN.matcher(title);

        if (!seasonMatcher.find() || !episodeMatcher.find()) {
            return null;
        }
        int season = Integer.parseInt(seasonMatcher.group(1));
        int episode = Integer.parseInt(episodeMatcher.group(1));
        String part = partMatcher.find() ? ".part" + partMatcher.group(1) : "";
        return new Video(show.getTitle() + String.format(".S%02dE%02d", season, episode) + part, videoUrl, show);
    }
}
