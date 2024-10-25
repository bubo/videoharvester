package com.bubo.videoharvester;

import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OstrovaDownloader extends VideoDownloader {

    private static final Logger logger = LoggerFactory.getLogger(OstrovaDownloader.class);

    @Value("${videoharvester.video.check.delay.ostrova:60000}")
    protected long fixedDelay;

    @Value("${videoharvester.video.location.ostrova}")
    private String path;

    @Scheduled(cron = "${videoharvester.ostrova.cron}")
    @Override
    public void checkForNewVideo() {
        super.checkForNewVideo();
    }

    @Override
    protected Logger getLogger() {
        return logger;
    }

    @Override
    protected String getShowName() {
        return "Ostrova";
    }

    @Override
    protected String getPath() {
        return path;
    }

    @Override
    protected String getUrl() {
        return "https://btvplus.bg/produkt/predavaniya/59496/ostrovat-na-100-te-grivni";
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