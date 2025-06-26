package com.bubo.videoharvester.downloaders;

import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


@Component(BtvPlusVideoDownloader.TITLE)
public class BtvPlusVideoDownloader extends BaseVideoDownloader {

    public static final String TITLE = "btvPlus";

    private static final Logger LOGGER = LoggerFactory.getLogger(BtvPlusVideoDownloader.class);

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
}
