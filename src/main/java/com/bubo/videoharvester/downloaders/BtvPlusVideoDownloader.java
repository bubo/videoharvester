package com.bubo.videoharvester.downloaders;

import com.bubo.videoharvester.entity.Video;
import com.bubo.videoharvester.repository.VideoRepository;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


@Component(BtvPlusVideoDownloader.TITLE)
public class BtvPlusVideoDownloader extends BaseVideoDownloader {

    public static final String TITLE = "btvPlus";

    private static final Logger LOGGER = LoggerFactory.getLogger(BtvPlusVideoDownloader.class);

    private final VideoRepository videoRepository;

    public BtvPlusVideoDownloader( VideoRepository videoRepository) {
        this.videoRepository = videoRepository;
    }

    @Override
    protected Logger getLogger() {

        return LOGGER;
    }

    @Override
    protected String getCssQuery() {

        return "div.episode:not([class*='bweb-gpt'])";
    }

    @Override
    protected String extractTitle(Element videoElement) {

        return videoElement.selectFirst(".title").text();
    }

    protected void  saveVideo(Video video) {
        videoRepository.save(video);
    }
}
