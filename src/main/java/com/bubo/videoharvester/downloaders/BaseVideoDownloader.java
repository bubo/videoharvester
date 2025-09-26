package com.bubo.videoharvester.downloaders;

import com.bubo.videoharvester.entity.Show;
import com.bubo.videoharvester.entity.Video;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class BaseVideoDownloader {

    @Value("${videoharvester.video.download.script}")
    protected String downloadScript;

    private static final Pattern SEASON_PATTERN = Pattern.compile("[Сс][Ее][Зз][Оо][Нн]\\s*[-:]*\\s*(\\d+)", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    private static final Pattern EPISODE_PATTERN = Pattern.compile("[Ее][Пп][Ии][Зз][Оо][Дд]\\s*[-:]*\\s*(\\d+)", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    private static final Pattern PART_PATTERN = Pattern.compile("[Чч][Аа][Сс][Тт]\\s*[-:]*\\s*(\\d+)", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    private static final Pattern progressPattern = Pattern.compile("\\[download\\]\\s*(\\d+\\.\\d+)%");

    protected abstract String getCssQuery();

    protected abstract String extractTitle(Element videoElement);

    protected abstract Logger getLogger();

    protected abstract void saveVideo(Video video);

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
            ProcessBuilder processBuilder = new ProcessBuilder("/bin/bash", downloadScript, "-o", file, video.getUrl(), "--no-part");

            getLogger().info("Executing command: {}", processBuilder.command());

            Process process = processBuilder.start();

            ExecutorService executor = Executors.newFixedThreadPool(2);
            executor.submit(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (!line.contains("[download]") || line.contains("100%")) {
                            getLogger().info("[Process stdout] {}", line);
                        } else {
                            getLogger().debug("[Process stdout] {}", line);
                        }

                        Matcher matcher = progressPattern.matcher(line);
                        if (matcher.find()) {
                            double currentProgress = Double.parseDouble(matcher.group(1));
                            if (currentProgress - video.getProgress() >= 3.0 || line.contains("100%")) {
                                video.setProgress(currentProgress);
                                saveVideo(video);
                                getLogger().info("[Process stdout] Progress updated: {}%", currentProgress);
                            }
                        }
                    }
                } catch (IOException e) {
                    getLogger().error("Error reading stdout: ", e);
                }
            });

            executor.submit(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        getLogger().error("[Process stderr] {}", line); // stderr обикновено се логва като грешка
                    }
                } catch (IOException e) {
                    getLogger().error("Error reading stderr: ", e);
                }
            });

            int exitCode = process.waitFor();
            executor.shutdown();

            if (exitCode == 0) {
                getLogger().info("Downloaded {} in {}", video.getTitle(), show.getPath());
                video.setDownloadTimestamp(LocalDateTime.now());
                video.setProgress(100.0);
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
            if (show.getDownloadOnlyEpisodes()) {
                return null;
            } else {
                return new Video(title, videoUrl, show);
            }
        } else {
            int season = Integer.parseInt(seasonMatcher.group(1));
            int episode = Integer.parseInt(episodeMatcher.group(1));
            String part = partMatcher.find() ? ".part" + partMatcher.group(1) : "";
            return new Video(show.getTitle() + String.format(".S%02dE%02d", season, episode) + part, videoUrl, show);
        }
    }
}
