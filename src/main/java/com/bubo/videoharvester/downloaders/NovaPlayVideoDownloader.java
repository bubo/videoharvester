package com.bubo.videoharvester.downloaders;

import com.bubo.videoharvester.entity.Video;
import com.bubo.videoharvester.repository.VideoRepository;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component(NovaPlayVideoDownloader.TITLE)
public class NovaPlayVideoDownloader extends BaseVideoDownloader {

    public static final String TITLE = "novaPlay";

    private static final Logger LOGGER = LoggerFactory.getLogger(NovaPlayVideoDownloader.class);

    private final VideoRepository videoRepository;

    public NovaPlayVideoDownloader(VideoRepository videoRepository) {
        this.videoRepository = videoRepository;
    }

    @Override
    protected Logger getLogger() {

        return LOGGER;
    }

    @Override
    protected String getCssQuery() {

        return "div[data-sentry-component=ShowThumb]";
    }

    @Override
    protected String extractTitle(Element videoElement) {

        String title =
                Optional.ofNullable(videoElement.selectFirst("h3[class^=cards-shared__VideoTitle]")).map(Element::text)
                        .orElse("");

        String subtitle = Optional.ofNullable(videoElement.selectFirst("h4[class^=cards-shared__VideoSubTitle]"))
                .map(Element::text).orElse("");

        String[] parts = title.split("\\(");
        String formattedTitle = parts[0].trim();
        String date = parts.length > 1
                      ? parts[1].replace(")", "").trim()
                      : "";

        if (!date.isEmpty()) {
            return formattedTitle + " " + subtitle + " (" + date + ")";
        } else {
            return formattedTitle + " " + subtitle;
        }
    }

    protected void  saveVideo(Video video) {
        videoRepository.save(video);
    }

}